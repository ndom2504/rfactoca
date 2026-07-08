package com.rfacto.shipping.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfacto.shipping.data.model.Colis
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MyParcelsView(viewModel: MainViewModel) {
    val parcels by viewModel.clientColis.collectAsState()
    var searchFilter by remember { mutableStateOf("") }

    val filteredParcels = parcels.filter {
        it.numero.contains(searchFilter, ignoreCase = true) ||
        it.description.contains(searchFilter, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mes expéditions", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        OutlinedTextField(
            value = searchFilter,
            onValueChange = { searchFilter = it },
            placeholder = { Text("Rechercher par n° de colis, description...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (filteredParcels.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucun colis correspondant.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(filteredParcels) { colis ->
                    Column {
                        ColisListItem(
                            colis = colis,
                            onClick = {
                                viewModel.selectColis(colis)
                                viewModel.setRoute("colis_detail")
                            }
                        )
                        
                        // Management Actions
                        if (colis.statut == "CREE" || colis.statut == "PAIEMENT_VALIDE") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (colis.statut == "CREE") {
                                    TextButton(
                                        onClick = {
                                            viewModel.selectColis(colis)
                                            viewModel.setRoute("declaration_payment")
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = RFactoPrimary)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Payer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                TextButton(
                                    onClick = { viewModel.deleteOrCancelColis(colis) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (colis.statut == "PAIEMENT_VALIDE") "Annuler (-20%)" else "Supprimer",
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
}

@Composable
fun PaymentsScreenView(viewModel: MainViewModel) {
    val parcels by viewModel.clientColis.collectAsState()
    val unpaidParcels = parcels.filter { it.statut == "RECU_CANADA" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Règlement des frais", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "Retrouvez ci-dessous vos colis pesés au Canada en attente de paiement pour expédition internationale.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (unpaidParcels.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusDelivered, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tous vos colis pesés sont réglés !", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(unpaidParcels) { colis ->
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
}

@Composable
fun HistoryView(viewModel: MainViewModel) {
    val parcels by viewModel.clientColis.collectAsState()
    val deliveredParcels = parcels.filter { it.statut == "LIVRE" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mon Historique", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "Archives de vos colis livrés et téléchargement de vos reçus/factures.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (deliveredParcels.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucun colis archivé.", color = Color.Gray)
                    }
                }
            } else {
                items(deliveredParcels) { colis ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(colis.numero, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(StatusDelivered.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("LIVRÉ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusDelivered)
                                }
                            }
                            Text(colis.description, fontSize = 13.sp, color = Color.Gray)
                            Text("Arrivé à : ${colis.ville}, ${colis.paysDestination}", fontSize = 11.sp, color = RFactoPrimary)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.addNotification("Téléchargement simulé de la facture PDF pour ${colis.numero}")
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Facture PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportView(viewModel: MainViewModel) {
    var faq1Expanded by remember { mutableStateOf(false) }
    var faq2Expanded by remember { mutableStateOf(false) }
    var faq3Expanded by remember { mutableStateOf(false) }
    var faq4Expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Support RFacto", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "Besoin d'aide avec un colis ou d'informations sur l'adresse de transit ? Nos équipes sont disponibles pour vous guider.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Contact grid
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ContactCard(
                title = "WhatsApp",
                desc = "+1 581 443 9464",
                icon = Icons.Default.Chat,
                color = Color(0xFF25D366),
                modifier = Modifier.weight(1f),
                onClick = { viewModel.addNotification("WhatsApp Support : +1 581 443 9464") }
            )
            ContactCard(
                title = "Téléphone",
                desc = "+1 581 443 9464",
                icon = Icons.Default.Phone,
                color = RFactoPrimary,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.addNotification("Appel vers le service client : +1 581 443 9464") }
            )
        }

        ContactCard(
            title = "Email Support",
            desc = "info@misterdil.ca - Réponse sous 24h",
            icon = Icons.Default.Email,
            color = RFactoSecondary,
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.addNotification("Ouverture du client mail vers info@misterdil.ca") }
        )

        Text("Foire Aux Questions (FAQ)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        // Accordeons
        FaqAccordionItem(
            question = "Comment fonctionne l'adresse de transit au Canada ?",
            answer = "Une fois votre colis déclaré sur l'application, vous recevez une adresse de transit avec votre numéro client RFacto unique. Vous expédiez votre colis vers cette adresse à Montréal. Dès réception, nous l'enregistrons, le pesons et l'expédions vers votre pays.",
            expanded = faq1Expanded,
            onClick = { faq1Expanded = !faq1Expanded }
        )

        FaqAccordionItem(
            question = "Quels sont les délais d'expédition ?",
            answer = "En moyenne, comptez 5 à 7 jours ouvrés pour l'expédition aérienne entre Montréal et nos relais en Afrique de l'Ouest ou en Europe, après validation de votre paiement.",
            expanded = faq2Expanded,
            onClick = { faq2Expanded = !faq2Expanded }
        )

        FaqAccordionItem(
            question = "Comment payer les frais d'expédition ?",
            answer = "RFacto propose de nombreux modes de paiement sécurisés directement depuis le détail de votre colis : Carte bancaire, Mobile Money (Orange, MTN, Moov), PayPal ou Virement Interac au Canada.",
            expanded = faq3Expanded,
            onClick = { faq3Expanded = !faq3Expanded }
        )

        FaqAccordionItem(
            question = "Mes colis sont-ils assurés ?",
            answer = "Oui, par défaut tous les colis sont couverts à hauteur d'une valeur forfaitaire. Pour les colis de valeur supérieure à 200 $ CAD, une option d'assurance optionnelle est calculée automatiquement à hauteur de 5 $ pour une sécurité optimale.",
            expanded = faq4Expanded,
            onClick = { faq4Expanded = !faq4Expanded }
        )
    }
}

@Composable
fun ContactCard(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(desc, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun FaqAccordionItem(question: String, answer: String, expanded: Boolean, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(question, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = answer,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun NotificationsView(viewModel: MainViewModel) {
    val logs by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = "Retrouvez l'historique des alertes SMS, Push et Emails concernant vos expéditions RFacto.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (logs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucune notification reçue.", color = Color.Gray)
                    }
                }
            } else {
                items(logs) { message ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(RFactoPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = RFactoPrimary, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(message, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text("À l'instant", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(viewModel: MainViewModel) {
    val nom by viewModel.profileNom.collectAsState()
    val prenom by viewModel.profilePrenom.collectAsState()
    val phone by viewModel.profilePhone.collectAsState()
    val email by viewModel.profileEmail.collectAsState()
    val pays by viewModel.profilePays.collectAsState()
    val currentPhoto by viewModel.profilePhoto.collectAsState()
    val ville by viewModel.profileVille.collectAsState()
    val adresse by viewModel.profileAdresse.collectAsState()
    val prefLangue by viewModel.profilePrefLangue.collectAsState()
    val prefNotifSms by viewModel.profilePrefNotifSms.collectAsState()
    val prefNotifEmail by viewModel.profilePrefNotifEmail.collectAsState()
    val prefNotifPush by viewModel.profilePrefNotifPush.collectAsState()

    val error by viewModel.profileError.collectAsState()
    val success by viewModel.profileSuccess.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            // Simulated interactive upload with progress
            coroutineScope.launch {
                isUploading = true
                uploadProgress = 0f
                while (uploadProgress < 1.0f) {
                    kotlinx.coroutines.delay(100)
                    uploadProgress += 0.1f
                }
                viewModel.uploadAndSetProfilePhoto(uri.toString())
                isUploading = false
            }
        }
    }

    val photoSource = when {
        currentPhoto == null -> null
        currentPhoto == "preset_1" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200"
        currentPhoto == "preset_2" -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200"
        currentPhoto == "preset_3" -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200"
        currentPhoto == "preset_4" -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200"
        else -> currentPhoto
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mon Profil & Paramètres", fontWeight = FontWeight.Bold, fontSize = 20.sp)

        if (error != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                Text(error ?: "", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }

        if (success != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)), modifier = Modifier.fillMaxWidth()) {
                Text(success ?: "", color = Color(0xFF065F46), modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // PROFILE PHOTO SECTION
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Large Avatar Preview
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF60A5FA), Color(0xFF6366F1))
                            )
                        ),
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
                        val initials = ((prenom.take(1)) + (nom.take(1))).uppercase()
                        Text(
                            text = initials.ifEmpty { "JN" },
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Upload & Simulator actions
                Text("Modifier votre photo de profil", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galerie", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isUploading = true
                            uploadProgress = 0f
                            coroutineScope.launch {
                                while (uploadProgress < 1f) {
                                    delay(150)
                                    uploadProgress += 0.15f
                                }
                                uploadProgress = 1f
                                delay(300)
                                isUploading = false
                                val randomPreset = listOf("preset_1", "preset_2", "preset_3", "preset_4").random()
                                viewModel.updateProfilePhoto(randomPreset)
                                viewModel.addNotification("Photo de profil téléversée avec succès sur le serveur Rfacto Cloud !")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f),
                        enabled = !isUploading
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Téléverser Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Real-time coroutine-based progress indicator
                if (isUploading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            color = RFactoPrimary,
                            trackColor = Color.LightGray.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Téléversement vers rfacto.com/api/profile/upload ... ${ (uploadProgress * 100).toInt() }%",
                            fontSize = 10.sp,
                            color = RFactoPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Preset options grid
                Text("Ou choisissez un avatar prédéfini :", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val presets = listOf("preset_1", "preset_2", "preset_3", "preset_4")
                    val presetUrls = listOf(
                        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=200",
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
                        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
                        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&q=80&w=200"
                    )

                    presets.forEachIndexed { idx, preset ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (currentPhoto == preset) 3.dp else 1.dp,
                                    color = if (currentPhoto == preset) RFactoPrimary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.updateProfilePhoto(preset) }
                        ) {
                            AsyncImage(
                                model = presetUrls[idx],
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // PROFILE FIELDS
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = prenom,
                onValueChange = { viewModel.profilePrenom.value = it },
                label = { Text("Prénom *") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = nom,
                onValueChange = { viewModel.profileNom.value = it },
                label = { Text("Nom *") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.profilePhone.value = it },
                label = { Text("Téléphone *") },
                singleLine = true,
                modifier = Modifier.weight(1.1f),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                readOnly = true,
                singleLine = true,
                modifier = Modifier.weight(0.9f),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = pays,
                onValueChange = { viewModel.profilePays.value = it },
                label = { Text("Pays de résidence") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = ville,
                onValueChange = { viewModel.profileVille.value = it },
                label = { Text("Ville") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
        }

        OutlinedTextField(
            value = adresse,
            onValueChange = { viewModel.profileAdresse.value = it },
            label = { Text("Adresse Complète (Livraison & Retrait)") },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        // PARAMÈTRES DE L'APPLICATION
        Text("Paramètres de l'application", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 4.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Language Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = RFactoPrimary, modifier = Modifier.size(20.dp))
                        Text("Langue préférée", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Français", "English").forEach { lang ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (prefLangue == lang) RFactoPrimary else Color.LightGray.copy(alpha = 0.2f)
                                    )
                                    .clickable { viewModel.profilePrefLangue.value = lang }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (prefLangue == lang) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(vertical = 4.dp))

                // Notification Prefs Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = RFactoSecondary, modifier = Modifier.size(20.dp))
                    Text("Canaux de notification", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // SMS Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alertes par SMS", fontSize = 12.sp)
                    Switch(
                        checked = prefNotifSms,
                        onCheckedChange = { viewModel.profilePrefNotifSms.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RFactoPrimary, checkedTrackColor = RFactoPrimary.copy(alpha = 0.4f))
                    )
                }

                // Email Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alertes par Courriel (Email)", fontSize = 12.sp)
                    Switch(
                        checked = prefNotifEmail,
                        onCheckedChange = { viewModel.profilePrefNotifEmail.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RFactoPrimary, checkedTrackColor = RFactoPrimary.copy(alpha = 0.4f))
                    )
                }

                // Push Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications Push Instantanées", fontSize = 12.sp)
                    Switch(
                        checked = prefNotifPush,
                        onCheckedChange = { viewModel.profilePrefNotifPush.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = RFactoPrimary, checkedTrackColor = RFactoPrimary.copy(alpha = 0.4f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { viewModel.updateProfile() },
            colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("profile_submit_button")
        ) {
            Text("Enregistrer les modifications", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.handleLogout() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter de l'application", fontWeight = FontWeight.Bold)
        }
    }
}
