package com.rfacto.shipping.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfacto.shipping.data.model.Tarifs
import com.rfacto.shipping.data.model.User
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel

@Composable
fun AdminDashboardView(viewModel: MainViewModel) {
    val colisList by viewModel.allColis.collectAsState()
    val usersList by viewModel.allUsers.collectAsState()
    val tariffs by viewModel.allTarifs.collectAsState()
    val payments by viewModel.allPaiements.collectAsState()

    var activeTab by remember { mutableStateOf("STATS") } // "STATS", "CLIENTS", "RATES"

    // Stats calculations
    val totalParcels = colisList.size
    val inTransitCount = colisList.count { it.statut != "CREE" && it.statut != "LIVRE" }
    val deliveredCount = colisList.count { it.statut == "LIVRE" }
    val totalRevenue = payments.filter { it.statut == "PAID" }.sumOf { it.montant }

    Column(modifier = Modifier.fillMaxSize()) {
        // Simple sub-header tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabButton(label = "Analytiques", active = activeTab == "STATS", onClick = { activeTab = "STATS" })
            TabButton(label = "Utilisateurs", active = activeTab == "CLIENTS", onClick = { activeTab = "CLIENTS" })
            TabButton(label = "Tarifs Pays", active = activeTab == "RATES", onClick = { activeTab = "RATES" })
        }

        when (activeTab) {
            "STATS" -> AdminStatsPanel(totalParcels, inTransitCount, deliveredCount, totalRevenue, colisList)
            "CLIENTS" -> AdminClientsPanel(usersList)
            "RATES" -> AdminRatesPanel(tariffs, viewModel)
        }
    }
}

@Composable
fun TabButton(label: String, active: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) RFactoPrimary else Color.Transparent,
            contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        modifier = Modifier.height(36.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminStatsPanel(
    totalParcels: Int,
    inTransit: Int,
    delivered: Int,
    revenue: Double,
    colisList: List<com.rfacto.shipping.data.model.Colis>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Indicateurs de Performance (KPIs)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // Stats grid
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(label = "Colis Totaux", value = "$totalParcels", icon = Icons.Default.Inventory, color = RFactoPrimary, modifier = Modifier.weight(1f))
                AdminStatCard(label = "En transit", value = "$inTransit", icon = Icons.Default.LocalShipping, color = RFactoTertiary, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AdminStatCard(label = "Colis Livrés", value = "$delivered", icon = Icons.Default.CheckCircle, color = StatusDelivered, modifier = Modifier.weight(1f))
                AdminStatCard(label = "Chiffre d'Affaires", value = String.format("%.1f $", revenue), icon = Icons.Default.MonetizationOn, color = RFactoSecondary, modifier = Modifier.weight(1f))
            }
        }

        // Country volume chart
        item {
            Text("Volume d'expédition par pays", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            CountryVolumeChart(colisList)
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }
            Column {
                Text(label, fontSize = 11.sp, color = Color.Gray)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun CountryVolumeChart(colisList: List<com.rfacto.shipping.data.model.Colis>) {
    val countries = listOf("Canada", "Gabon", "France")
    val counts = countries.map { country -> colisList.count { it.paysDestination == country } }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val canvasW = size.width
                val canvasH = size.height
                val barCount = countries.size
                val spaceBetween = 24.dp.toPx()
                val totalSpaces = spaceBetween * (barCount + 1)
                val barW = (canvasW - totalSpaces) / barCount

                // Draw chart baseline
                drawLine(Color.LightGray, Offset(0f, canvasH - 20.dp.toPx()), Offset(canvasW, canvasH - 20.dp.toPx()), strokeWidth = 2f)

                countries.forEachIndexed { index, country ->
                    val count = counts[index]
                    val barH = (canvasH - 40.dp.toPx()) * (count.toFloat() / maxCount.toFloat())
                    val left = spaceBetween + index * (barW + spaceBetween)
                    val top = canvasH - 20.dp.toPx() - barH

                    // Draw bar
                    drawRect(
                        color = if (index % 2 == 0) RFactoPrimary else RFactoSecondary,
                        topLeft = Offset(left, top),
                        size = Size(barW, barH)
                    )

                    // Draw count text above bar
                    // In a raw canvas, simple text requires drawing or native canvas injection,
                    // we can draw simple helper circles as values for high-fidelity visual:
                    drawCircle(Color.White, radius = 5f, center = Offset(left + barW / 2, top))
                }
            }

            // Legend labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                countries.forEachIndexed { index, country ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(country, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("${counts[index]} u.", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminClientsPanel(usersList: List<User>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Utilisateurs & Acteurs de la Plateforme (${usersList.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(usersList) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${user.prenom} ${user.nom}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Email : ${user.email} | Tél : ${user.telephone}", fontSize = 11.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RFactoPrimary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(user.role, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RFactoPrimary)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusDelivered.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(user.pays, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusDelivered)
                            }
                        }
                    }

                    // User active marker
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(StatusDelivered)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminRatesPanel(tariffs: List<Tarifs>, viewModel: MainViewModel) {
    val pays by viewModel.adminPays.collectAsState()
    val prKg by viewModel.adminPrixKg.collectAsState()
    val livLocal by viewModel.adminLivLocal.collectAsState()
    val ass by viewModel.adminAssurance.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configurer les tarifs d'expédition", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Configuration form
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ajouter / Modifier un tarif de pays", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = pays,
                    onValueChange = { viewModel.adminPays.value = it },
                    label = { Text("Pays de destination *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_rate_country"),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = prKg,
                        onValueChange = { viewModel.adminPrixKg.value = it },
                        label = { Text("Prix / kg ($ CAD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = livLocal,
                        onValueChange = { viewModel.adminLivLocal.value = it },
                        label = { Text("Livraison locale ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                OutlinedTextField(
                    value = ass,
                    onValueChange = { viewModel.adminAssurance.value = it },
                    label = { Text("Frais assurance colis précieux ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Button(
                    onClick = { viewModel.addOrUpdateTarif() },
                    colors = ButtonDefaults.buttonColors(containerColor = RFactoSecondary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("admin_rate_submit")
                ) {
                    Text("Enregistrer les tarifs", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Tarifs actuels enregistrés", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Existing tariffs list
        tariffs.forEach { tariff ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(tariff.pays, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Transport : ${tariff.prixParKg} $ / kg", fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Livraison domicile : ${tariff.livraisonLocale} $", fontSize = 11.sp)
                        Text("Assurance : ${tariff.assurance} $", fontSize = 11.sp, color = RFactoTertiary)
                    }
                }
            }
        }
    }
}
