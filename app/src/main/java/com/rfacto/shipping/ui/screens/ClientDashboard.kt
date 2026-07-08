package com.rfacto.shipping.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfacto.shipping.data.model.Colis
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel
import coil.compose.AsyncImage

@Composable
fun ClientDashboardView(viewModel: MainViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val parcels by viewModel.clientColis.collectAsState()

    // Status Counters
    val enAttenteCount = parcels.count { it.statut == "CREE" }
    val enTransitCount = parcels.count {
        it.statut == "RECU_CANADA" || it.statut == "PAIEMENT_VALIDE" ||
        it.statut == "EN_PREPARATION" || it.statut == "EXPEDIE" || it.statut == "EN_DOUANE"
    }
    val arrivesCount = parcels.count { it.statut == "ARRIVE_PAYS" || it.statut == "CENTRE_LOCAL" || it.statut == "EN_LIVRAISON" }
    val livresCount = parcels.count { it.statut == "LIVRE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Bonjour ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(currentUser?.prenom ?: "Jean")
                            }
                            append(" 👋")
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    // Profile avatar matching the HTML right side:
                    val photoSource = when {
                        currentUser?.profilePhoto == null -> null
                        currentUser?.profilePhoto == "preset_1" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200"
                        currentUser?.profilePhoto == "preset_2" -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200"
                        currentUser?.profilePhoto == "preset_3" -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200"
                        currentUser?.profilePhoto == "preset_4" -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200"
                        else -> currentUser?.profilePhoto
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF60A5FA), Color(0xFF6366F1))
                                )
                            )
                            .clickable { viewModel.setRoute("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoSource != null) {
                            AsyncImage(
                                model = photoSource,
                                contentDescription = "Photo de profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            val initials = ((currentUser?.prenom?.take(1) ?: "") + (currentUser?.nom?.take(1) ?: "")).uppercase()
                            Text(
                                text = initials.ifEmpty { "JN" },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = "Heureux de vous revoir ! Suivez vos colis en temps réel.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Status Counts Grid (Sleek Horizontal 4-Column Layout)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SleekStatItem(
                    count = enAttenteCount,
                    label = "Attente",
                    bgColor = if (isSystemInDarkTheme()) ColorAttenteBg.copy(alpha = 0.2f) else ColorAttenteBg,
                    textColor = if (isSystemInDarkTheme()) Color.White else ColorAttenteText,
                    modifier = Modifier.weight(1f)
                )
                SleekStatItem(
                    count = enTransitCount,
                    label = "Transit",
                    bgColor = if (isSystemInDarkTheme()) ColorTransitBg.copy(alpha = 0.2f) else ColorTransitBg,
                    textColor = if (isSystemInDarkTheme()) Color.White else ColorTransitText,
                    modifier = Modifier.weight(1f)
                )
                SleekStatItem(
                    count = arrivesCount,
                    label = "Arrivés",
                    bgColor = if (isSystemInDarkTheme()) ColorArrivesBg.copy(alpha = 0.2f) else ColorArrivesBg,
                    textColor = if (isSystemInDarkTheme()) Color.White else ColorArrivesText,
                    modifier = Modifier.weight(1f)
                )
                SleekStatItem(
                    count = livresCount,
                    label = "Livrés",
                    bgColor = if (isSystemInDarkTheme()) ColorLivresBg.copy(alpha = 0.2f) else ColorLivresBg,
                    textColor = if (isSystemInDarkTheme()) Color.White else ColorLivresText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Title: Raccourcis
        item {
            Text(
                text = "Raccourcis rapides",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Quick Actions Grid (Polished to match buttons)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Nouveau colis",
                    description = "Déclarer colis",
                    icon = Icons.Default.Add,
                    color = RFactoPrimary,
                    isDarkStyle = true,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setRoute("declare_colis") }
                )
                QuickActionCard(
                    title = "Payer frais",
                    description = "Frais d'envoi",
                    icon = Icons.Default.CreditCard,
                    color = RFactoPrimary,
                    isDarkStyle = false,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setRoute("payments") }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Mes Colis",
                    description = "Suivi & Liste",
                    icon = Icons.Default.Inventory2,
                    color = RFactoSecondary,
                    isDarkStyle = false,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setRoute("mes_colis") }
                )
                QuickActionCard(
                    title = "Support",
                    description = "Nous contacter",
                    icon = Icons.Default.ContactSupport,
                    color = RFactoSecondary,
                    isDarkStyle = false,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setRoute("support") }
                )
            }
        }

        // Help / Support Banner (Sleek WhatsApp Banner from HTML)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = RFactoSecondary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setRoute("support") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Besoin d'aide ?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD) // blue-200
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Contactez un agent RFacto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ColorWhatsApp)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WhatsApp",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section Title: Colis Récents
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Derniers colis déclarés",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Voir tout",
                    fontSize = 13.sp,
                    color = RFactoPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.setRoute("mes_colis") }
                )
            }
        }

        // List of recent parcels
        val recentParcels = parcels.take(4)
        if (recentParcels.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aucun colis déclaré pour le moment.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentParcels) { colis ->
                ColisListItem(
                    colis = colis,
                    onClick = {
                        viewModel.selectColis(colis)
                        viewModel.setRoute("colis_detail")
                    }
                )
            }
        }
    }
}

@Composable
fun SleekStatItem(
    count: Int,
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%02d", count),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    isDarkStyle: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (isDarkStyle) RFactoPrimary else MaterialTheme.colorScheme.surface
    val contentColor = if (isDarkStyle) Color.White else MaterialTheme.colorScheme.onSurface
    val iconBgColor = if (isDarkStyle) Color.White.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)
    val iconColor = if (isDarkStyle) Color.White else color
    val borderStroke = if (isDarkStyle) null else BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    lineHeight = 15.sp
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = if (isDarkStyle) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

@Composable
fun ColisListItem(colis: Colis, onClick: () -> Unit) {
    val statusColor = when (colis.statut) {
        "CREE" -> StatusCreated
        "RECU_CANADA" -> StatusPending
        "PAIEMENT_VALIDE" -> StatusTransit
        "EXPEDIE" -> StatusTransit
        "ARRIVE_PAYS" -> StatusTransit
        "CENTRE_LOCAL" -> StatusPending
        "LIVRE" -> StatusDelivered
        else -> RFactoPrimary
    }

    val statusLabel = when (colis.statut) {
        "CREE" -> "Déclaré"
        "RECU_CANADA" -> "Reçu au Canada"
        "PAIEMENT_VALIDE" -> "Prêt expédition"
        "EXPEDIE" -> "En Transit"
        "ARRIVE_PAYS" -> "Arrivé au pays"
        "CENTRE_LOCAL" -> "Prêt retrait"
        "LIVRE" -> "Livré"
        else -> colis.statut
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(statusColor)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusLabel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = colis.numero,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                val rateMultiplier = when (colis.paysDestination) {
                    "Gabon" -> 15.0
                    "France" -> 10.0
                    else -> 12.0
                }
                val localFee = if (colis.modeLivraison == "LIVRAISON_DOMICILE") 8.0 else 0.0
                val insuranceFee = if (colis.valeur > 200.0) 5.0 else 0.0
                val transportFee = colis.poids * rateMultiplier
                val totalFee = transportFee + localFee + insuranceFee

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "TOTAL ESTIMÉ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%.2f $ CAD", totalFee),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = RFactoPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Destination: ${colis.paysDestination}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = colis.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
