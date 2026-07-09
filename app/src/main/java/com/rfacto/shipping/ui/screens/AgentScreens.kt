package com.rfacto.shipping.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.rfacto.shipping.data.model.Colis
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel

@Composable
fun AgentCanadaView(viewModel: MainViewModel) {
    val parcels by viewModel.allColis.collectAsState()
    val declaredParcels = parcels.filter { it.statut == "CREE" }

    var selectedColisToReceive by remember { mutableStateOf<Colis?>(null) }
    var searchNumberInput by remember { mutableStateOf("") }

    val pReel by viewModel.agentPoidsReel.collectAsState()
    val dReelles by viewModel.agentDimReelles.collectAsState()
    val etat by viewModel.agentEtatColis.collectAsState()
    val agentPhoto by viewModel.agentPhoto.collectAsState()
    val isPaymentLoading by viewModel.isPaymentLoading.collectAsState()

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.agentPhoto.value = uri.toString()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Espace Agent Canada 🇨🇦",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Dépôt de transit à Montréal. Réceptionnez, contrôlez, et pesez les colis clients.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Barcode / Tracking Search simulator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Scanner ou rechercher un colis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchNumberInput,
                            onValueChange = { searchNumberInput = it },
                            placeholder = { Text("Numéro RFC-2026-XXXXXX") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("agent_canada_search_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                val match = parcels.firstOrNull { it.numero.trim() == searchNumberInput.trim() }
                                if (match != null) {
                                    selectedColisToReceive = match
                                    viewModel.agentPoidsReel.value = match.poids.toString()
                                    viewModel.agentDimReelles.value = match.dimensions
                                } else {
                                    viewModel.addNotification("Recherche Agent Canada : Colis introuvable.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                }
            }
        }

        if (selectedColisToReceive == null) {
            // List of declared parcels ready to be received
            item {
                Text(
                    text = "Colis en attente de réception (${declaredParcels.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (declaredParcels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun colis déclaré en attente de réception.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(declaredParcels) { colis ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedColisToReceive = colis
                                viewModel.agentPoidsReel.value = colis.poids.toString()
                                viewModel.agentDimReelles.value = colis.dimensions
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(colis.numero, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Client : ${colis.clientName}", fontSize = 12.sp, color = Color.Gray)
                                Text("Dest : ${colis.paysDestination}", fontSize = 11.sp, color = RFactoPrimary, fontWeight = FontWeight.SemiBold)
                            }
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // Receive parcel control panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Réception du colis : ${selectedColisToReceive?.numero}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            IconButton(onClick = { selectedColisToReceive = null }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Divider()

                        DetailRow(label = "Client", value = selectedColisToReceive?.clientName ?: "")
                        DetailRow(label = "Description", value = selectedColisToReceive?.description ?: "")
                        DetailRow(label = "Destination", value = "${selectedColisToReceive?.ville}, ${selectedColisToReceive?.paysDestination}")
                        DetailRow(label = "Estimations client", value = "${selectedColisToReceive?.poids} kg | ${selectedColisToReceive?.dimensions}")

                        Divider()

                        Text("Mesures réelles et inspection", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        // Photo capture/upload
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { photoLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (agentPhoto.startsWith("content://")) {
                                Image(
                                    painter = rememberAsyncImagePainter(agentPhoto),
                                    contentDescription = "Photo inspection",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = RFactoPrimary)
                                    Text("Prendre une photo du colis", fontSize = 12.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = pReel,
                            onValueChange = { viewModel.agentPoidsReel.value = it },
                            label = { Text("Poids Réel Mesuré (kg) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agent_poids_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = dReelles,
                            onValueChange = { viewModel.agentDimReelles.value = it },
                            label = { Text("Dimensions Réelles (L x l x h cm) *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agent_dim_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Condition selector
                        var expandedState by remember { mutableStateOf(false) }
                        val states = listOf("Excellent", "Bon", "Emballage Abîmé", "Reconditionné", "Fragile")
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = etat,
                                onValueChange = {},
                                label = { Text("État de l'emballage / colis") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expandedState = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(
                                expanded = expandedState,
                                onDismissRequest = { expandedState = false }
                            ) {
                                states.forEach { state ->
                                    DropdownMenuItem(
                                        text = { Text(state) },
                                        onClick = {
                                            viewModel.agentEtatColis.value = state
                                            expandedState = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isPaymentLoading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                selectedColisToReceive?.let { colis ->
                                    viewModel.receiveParcelAtCanadaTransit(colis.id)
                                    selectedColisToReceive = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("agent_receive_submit"),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                            enabled = !isPaymentLoading
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enregistrer la réception", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgentLocalView(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val parcels by viewModel.allColis.collectAsState()

    // Filter parcels destined to local Agent's country
    val localCountry = currentUser?.pays ?: "Gabon"
    val localParcels = parcels.filter { it.paysDestination == localCountry && it.statut != "CREE" }

    var selectedColisToManage by remember { mutableStateOf<Colis?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Espace Agent Local ($localCountry) 🌍",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Gérez les arrivées locales, le stockage en point relais et les livraisons à domicile.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Search card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher par n° de colis") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            val match = localParcels.firstOrNull { it.numero.trim() == searchQuery.trim() }
                            if (match != null) {
                                selectedColisToManage = match
                            } else {
                                viewModel.addNotification("Recherche Agent Local: Colis non trouvé dans vos arrivées.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            }
        }

        if (selectedColisToManage == null) {
            item {
                Text(
                    text = "Flux des colis locaux (${localParcels.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (localParcels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun colis pour votre zone pour le moment.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(localParcels) { colis ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedColisToManage = colis }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(colis.numero, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Destinataire: ${colis.adresseDestination}", fontSize = 12.sp, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (colis.modeLivraison == "POINT_RELAIS") RFactoSecondary.copy(alpha = 0.15f)
                                                else RFactoPrimary.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (colis.modeLivraison == "POINT_RELAIS") "Relais" else "Domicile",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (colis.modeLivraison == "POINT_RELAIS") RFactoSecondary else RFactoPrimary
                                        )
                                    }
                                    Text(
                                        text = "Statut: ${colis.statut}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RFactoTertiary
                                    )
                                }
                            }
                            Icon(Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // Manage status panel for selected parcel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gestion : ${selectedColisToManage?.numero}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            IconButton(onClick = { selectedColisToManage = null }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }

                        Divider()

                        DetailRow(label = "Client", value = selectedColisToManage?.clientName ?: "")
                        DetailRow(label = "Contenu", value = selectedColisToManage?.description ?: "")
                        DetailRow(label = "Mode livraison", value = selectedColisToManage?.modeLivraison ?: "")
                        DetailRow(label = "Lieu livraison", value = "${selectedColisToManage?.ville}, ${selectedColisToManage?.paysDestination}")
                        DetailRow(label = "Statut Actuel", value = selectedColisToManage?.statut ?: "")

                        Divider()

                        Text("Mettre à jour le statut du colis", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        val statusSequence = listOf(
                            "PAIEMENT_VALIDE" to "Paiement Validé",
                            "EXPEDIE" to "Expédié (En transit international)",
                            "ARRIVE_PAYS" to "Arrivé au pays de destination",
                            "CENTRE_LOCAL" to "Reçu au centre local / Relais",
                            "EN_LIVRAISON" to "En cours de livraison / Prêt pour retrait",
                            "LIVRE" to "Livré avec confirmation"
                        )

                        statusSequence.forEach { step ->
                            val isCurrent = selectedColisToManage?.statut == step.first
                            Button(
                                onClick = {
                                    selectedColisToManage?.let { colis ->
                                        val cmt = when (step.first) {
                                            "EXPEDIE" -> "Le colis a quitté Montréal à bord du vol cargo RFacto."
                                            "ARRIVE_PAYS" -> "Le colis a atterri et est en cours de traitement douanier local."
                                            "CENTRE_LOCAL" -> "Colis réceptionné à l'agence locale de ${colis.paysDestination}. Prêt pour l'étape finale."
                                            "EN_LIVRAISON" -> {
                                                if (colis.modeLivraison == "POINT_RELAIS") "Le colis est stocké en rayon. Code de retrait généré."
                                                else "Colis confié au livreur local. Livraison domicile en cours."
                                            }
                                            "LIVRE" -> "Colis officiellement remis en main propre. Clôture de l'expédition."
                                            else -> "Mise à jour de transit effectuée."
                                        }
                                        viewModel.advanceParcelStatus(colis.id, step.first, "$localCountry Hub", cmt)
                                        // Update state reference
                                        selectedColisToManage = colis.copy(statut = step.first)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrent) StatusCreated else RFactoPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isCurrent) "Actuel : ${step.second}" else "Passer à : ${step.second}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
