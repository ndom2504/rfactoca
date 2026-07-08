package com.rfacto.shipping.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rfacto.shipping.data.model.Colis
import com.rfacto.shipping.data.model.Paiement
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColisDetailScreenView(viewModel: MainViewModel) {
    val colis by viewModel.selectedColis.collectAsState()
    val trackingList by viewModel.selectedColisSuivi.collectAsState()
    val payments by viewModel.selectedColisPaiements.collectAsState()

    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedPaymentMode by remember { mutableStateOf("MOBILE_MONEY") }

    val stripeLoading by viewModel.stripeLoading.collectAsState()
    val stripeError by viewModel.stripeError.collectAsState()

    var showDemoStripeAlert by remember { mutableStateOf(false) }

    // Initialize Stripe rememberPaymentSheet
    val paymentSheet = com.stripe.android.paymentsheet.rememberPaymentSheet { paymentResult ->
        when (paymentResult) {
            is com.stripe.android.paymentsheet.PaymentSheetResult.Completed -> {
                colis?.let { currentColis ->
                    viewModel.payParcelFees(currentColis.id, "CARTE_BANCAIRE")
                    viewModel.addNotification("Paiement Stripe par carte validé pour le colis ${currentColis.numero}")
                }
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Failed -> {
                viewModel.stripeError.value = "Paiement échoué : ${paymentResult.error.localizedMessage}"
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Canceled -> {
                viewModel.stripeError.value = "Paiement annulé par l'utilisateur."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(colis?.numero ?: "Détails du colis") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setRoute("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        colis?.let { currentColis ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Overview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentColis.description,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(status = currentColis.statut)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(label = "Destination", value = "${currentColis.ville}, ${currentColis.paysDestination}", modifier = Modifier.weight(1f))
                            DetailItem(label = "Poids", value = "${currentColis.poids} kg", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(label = "Dimensions", value = currentColis.dimensions, modifier = Modifier.weight(1f))
                            DetailItem(label = "Valeur déclarée", value = "${currentColis.valeur} $ CAD", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 2. Interactive shipping map (Canvas Flight Tracker)
                Text(
                    text = "Suivi en temps réel de votre vol cargo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                CanvasFlightTracker(status = currentColis.statut, destination = currentColis.paysDestination)

                // 3. Tracking Timeline
                Text(
                    text = "Chronologie d'expédition",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val steps = listOf(
                            Triple("CREE", "Déclaré", "Par le client au Canada"),
                            Triple("RECU_CANADA", "Reçu au Canada", "Au Transit Center Montréal"),
                            Triple("PAIEMENT_VALIDE", "Paiement validé", "Frais d'expédition réglés"),
                            Triple("EXPEDIE", "Expédié", "Vol cargo international"),
                            Triple("ARRIVE_PAYS", "Arrivé au ${currentColis.paysDestination}", "Dédouanement en cours"),
                            Triple("CENTRE_LOCAL", "Centre local / Relais", "Prêt pour retrait ou livraison"),
                            Triple("LIVRE", "Livré", "Remis au destinataire")
                        )

                        val currentStepIndex = steps.indexOfFirst { it.first == currentColis.statut }.let {
                            if (it == -1) {
                                // Fallback states
                                when (currentColis.statut) {
                                    "EN_ATTENTE_RECEPTION" -> 0
                                    "EN_PREPARATION" -> 2
                                    "EN_DOUANE" -> 4
                                    "EN_LIVRAISON" -> 5
                                    else -> 1
                                }
                            } else it
                        }

                        steps.forEachIndexed { index, step ->
                            val isCompleted = index <= currentStepIndex
                            val isActive = index == currentStepIndex
                            val comment = if (isActive && trackingList.isNotEmpty()) {
                                trackingList.last().commentaire
                            } else step.third

                            TimelineStepItem(
                                title = step.second,
                                subtitle = comment,
                                isCompleted = isCompleted,
                                isActive = isActive,
                                isLast = index == steps.size - 1
                            )
                        }
                    }
                }

                // 4. Automatic Billing Card (Calcul automatique du prix)
                val rateMultiplier = when (currentColis.paysDestination) {
                    "Gabon" -> 15.0
                    "France" -> 10.0
                    else -> 12.0
                }
                val localFee = if (currentColis.modeLivraison == "LIVRAISON_DOMICILE") 8.0 else 0.0
                val insuranceFee = if (currentColis.valeur > 200.0) 5.0 else 0.0
                val transportFee = currentColis.poids * rateMultiplier
                val totalFee = transportFee + localFee + insuranceFee

                Text(
                    text = "Détail de la facturation automatique",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Transport (Canada ➔ ${currentColis.paysDestination})", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f $", transportFee), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Livraison locale (${if (currentColis.modeLivraison == "POINT_RELAIS") "Relais" else "Domicile"})", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f $", localFee), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Assurance (Valeur > 200$)", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format("%.2f $", insuranceFee), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Montant total estimé", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(String.format("%.2f $ CAD", totalFee), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RFactoPrimary)
                        }

                        val paymentStatus = if (payments.any { it.statut == "PAID" }) "PAID" else "PENDING"
                        
                        if (paymentStatus == "PAID") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusDelivered.copy(alpha = 0.15f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusDelivered)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Frais d'expédition réglés avec succès !", color = StatusDelivered, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        } else if (currentColis.statut == "CREE") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusCreated.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "En attente de réception et pesée finale au Canada pour valider la facture.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            // Status is RECU_CANADA or further, but payment is PENDING.
                            Button(
                                onClick = { showPaymentSheet = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .testTag("pay_fees_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Payer les frais d'expédition", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // 5. Payment Selection Simulation Sheet
                AnimatedVisibility(visible = showPaymentSheet) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, RFactoPrimary, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Choisir le mode de paiement", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                IconButton(onClick = { showPaymentSheet = false }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }

                            if (stripeError != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stripeError ?: "",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            val modes = listOf(
                                "MOBILE_MONEY" to "Mobile Money (Orange/MTN)",
                                "CARTE_BANCAIRE" to "Carte bancaire (Visa/Mastercard) [Stripe]",
                                "PAYPAL" to "PayPal",
                                "INTERAC" to "Virement Interac (Canada)",
                                "VIREMENT" to "Virement Bancaire"
                            )

                            modes.forEach { mode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (selectedPaymentMode == mode.first) RFactoPrimary.copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable { selectedPaymentMode = mode.first }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedPaymentMode == mode.first,
                                        onClick = { selectedPaymentMode = mode.first }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(mode.second, fontSize = 13.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    val isStripeKeyReal = com.rfacto.shipping.BuildConfig.STRIPE_PUBLISHABLE_KEY.isNotEmpty() &&
                                            !com.rfacto.shipping.BuildConfig.STRIPE_PUBLISHABLE_KEY.contains("PLACEHOLDER") &&
                                            !com.rfacto.shipping.BuildConfig.STRIPE_PUBLISHABLE_KEY.contains("YOUR_STRIPE")

                                    if (selectedPaymentMode == "CARTE_BANCAIRE") {
                                        if (isStripeKeyReal) {
                                            viewModel.initiateStripePayment(currentColis.id, totalFee) { paymentIntentData ->
                                                paymentIntentData?.let { data ->
                                                    paymentSheet.presentWithPaymentIntent(
                                                        data.paymentIntentClientSecret,
                                                        com.stripe.android.paymentsheet.PaymentSheet.Configuration(
                                                            merchantDisplayName = "RFacto Logistique Inc.",
                                                            customer = com.stripe.android.paymentsheet.PaymentSheet.CustomerConfiguration(
                                                                id = data.customerId,
                                                                ephemeralKeySecret = data.ephemeralKeySecret
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            showDemoStripeAlert = true
                                        }
                                    } else {
                                        viewModel.payParcelFees(currentColis.id, selectedPaymentMode)
                                        showPaymentSheet = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered)
                            ) {
                                if (stripeLoading && selectedPaymentMode == "CARTE_BANCAIRE") {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                } else {
                                    Text("Confirmer le paiement de ${String.format("%.2f", totalFee)} $")
                                }
                            }
                        }
                    }
                }

                // 6. Delivery details (Point relais with QR or Domicile with Driver track)
                if (currentColis.statut == "CENTRE_LOCAL" || currentColis.statut == "ARRIVE_PAYS" ||
                    currentColis.statut == "EN_LIVRAISON" || currentColis.statut == "LIVRE"
                ) {
                    Text(
                        text = "Informations de livraison locale",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (currentColis.modeLivraison == "POINT_RELAIS") {
                        // Point Relais Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = RFactoSecondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retrait au Point Relais RFacto", fontWeight = FontWeight.Bold)
                                }

                                HorizontalDivider()

                                Text(
                                    text = "Point Relais : ${currentColis.adresseDestination}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                Text(
                                    text = "Horaires d'ouverture : Lun - Ven (08:00 - 18:00), Sam (09:00 - 15:00)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // QR Code Drawing
                                Text(
                                    text = "QR Code de retrait",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RFactoPrimary
                                )

                                MockQRCodeCanvas(code = "RET-RFC-${currentColis.id}")

                                Text(
                                    text = "Code : RET-RFC-${currentColis.id}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Présentez ce QR Code ou donnez le code ci-dessus à l'agent local pour récupérer votre colis.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Livraison Domicile Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = RFactoSecondary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Livreur RFacto en route", fontWeight = FontWeight.Bold)
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(RFactoSecondary.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = RFactoSecondary)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Moussa Diop", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Livreur certifié RFacto", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.addNotification("Appel simulé du livreur Moussa Diop au +2376778899")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RFactoSecondary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Appeler", fontSize = 12.sp)
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Adresse GPS de livraison", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(currentColis.adresseDestination, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(vertical = 4.dp))
                                        Text("Le livreur se dirige actuellement vers cette position.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDemoStripeAlert) {
        AlertDialog(
            onDismissRequest = { showDemoStripeAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = RFactoPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Intégration Stripe Active", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RFacto est entièrement configuré avec l'intégration officielle de Stripe Android SDK.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pour traiter de vrais paiements de frais d'expédition :\n" +
                               "1. Hébergez votre backend Vercel + Neon db (domaine : rfacto.com)\n" +
                               "2. Configurez votre STRIPE_PUBLISHABLE_KEY dans les secrets d'AI Studio.\n\n" +
                               "Souhaitez-vous simuler un paiement par carte réussi pour finaliser cette commande ?",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        colis?.let { currentColis ->
                            viewModel.payParcelFees(currentColis.id, "CARTE_BANCAIRE (DEMO)")
                        }
                        showDemoStripeAlert = false
                        showPaymentSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered)
                ) {
                    Text("Simuler le paiement", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemoStripeAlert = false }) {
                    Text("Retour", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (text, color) = when (status) {
        "CREE" -> "Déclaré" to StatusCreated
        "RECU_CANADA" -> "Reçu Canada" to StatusPending
        "PAIEMENT_VALIDE" -> "Paiement Validé" to StatusTransit
        "EXPEDIE" -> "En Transit" to StatusTransit
        "ARRIVE_PAYS" -> "Arrivé Pays" to StatusTransit
        "CENTRE_LOCAL" -> "Prêt Retrait" to StatusPending
        "LIVRE" -> "Livré" to StatusDelivered
        else -> status to RFactoPrimary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TimelineStepItem(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) RFactoPrimary
                        else if (isCompleted) StatusDelivered
                        else Color.LightGray.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted && !isActive) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(
                            if (isCompleted) StatusDelivered else Color.LightGray.copy(alpha = 0.5f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isActive) RFactoPrimary else if (isCompleted) MaterialTheme.colorScheme.onSurface else Color.LightGray
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun CanvasFlightTracker(status: String, destination: String) {
    val progressFraction = when (status) {
        "CREE" -> 0.0f
        "RECU_CANADA" -> 0.15f
        "PAIEMENT_VALIDE" -> 0.3f
        "EN_PREPARATION" -> 0.4f
        "EXPEDIE" -> 0.65f
        "EN_DOUANE" -> 0.8f
        "ARRIVE_PAYS" -> 0.9f
        "CENTRE_LOCAL" -> 0.95f
        "LIVRE" -> 1.0f
        else -> 0.5f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SlateDarkBg)
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw grid / lines for styling
            val gridBrush = Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
            drawRect(brush = gridBrush, size = size)

            // Draw latitude/longitude grid lines
            for (i in 1..4) {
                val y = height * i / 5
                drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, y), Offset(width, y), strokeWidth = 1f)
            }
            for (i in 1..8) {
                val x = width * i / 9
                drawLine(Color.White.copy(alpha = 0.05f), Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
            }

            // 2. Nodes locations
            val canadaPos = Offset(width * 0.18f, height * 0.4f)
            val destPos = Offset(width * 0.82f, height * 0.65f)

            // 3. Draw curved flight route path
            val path = Path().apply {
                moveTo(canadaPos.x, canadaPos.y)
                // Draw bezier control points
                cubicTo(
                    width * 0.4f, height * 0.1f,
                    width * 0.6f, height * 0.3f,
                    destPos.x, destPos.y
                )
            }

            // Draw route shadow & main line
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.2f),
                style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f))
            )

            // 4. Calculate airplane animated coordinate on path
            // Simple approximation of Bezier cubic curve for visualization:
            val t = progressFraction
            val xT = (1-t)*(1-t)*(1-t)*canadaPos.x + 3*(1-t)*(1-t)*t*(width*0.4f) + 3*(1-t)*t*t*(width*0.6f) + t*t*t*destPos.x
            val yT = (1-t)*(1-t)*(1-t)*canadaPos.y + 3*(1-t)*(1-t)*t*(height*0.1f) + 3*(1-t)*t*t*(height*0.3f) + t*t*t*destPos.y
            val planePos = Offset(xT, yT)

            // Draw completed route path segment
            val completedPath = Path().apply {
                moveTo(canadaPos.x, canadaPos.y)
                cubicTo(
                    canadaPos.x + (width*0.4f - canadaPos.x) * t, canadaPos.y + (height*0.1f - canadaPos.y) * t,
                    canadaPos.x + (width*0.6f - canadaPos.x) * t, canadaPos.y + (height*0.3f - canadaPos.y) * t,
                    xT, yT
                )
            }
            drawPath(
                path = completedPath,
                color = RFactoPrimary,
                style = Stroke(width = 5f)
            )

            // 5. Draw cities markers
            // Canada (Montreal)
            drawCircle(color = RFactoPrimary, radius = 7f, center = canadaPos)
            drawCircle(color = RFactoPrimary.copy(alpha = 0.3f), radius = 15f, center = canadaPos)

            // Destination Country
            drawCircle(color = RFactoSecondary, radius = 7f, center = destPos)
            drawCircle(color = RFactoSecondary.copy(alpha = 0.3f), radius = 15f, center = destPos)

            // 6. Draw Airplane cargo icon pulsing
            drawCircle(color = Color.White, radius = 8f, center = planePos)
            drawCircle(color = RFactoPrimary.copy(alpha = 0.5f), radius = 14f, center = planePos)
        }

        // Overlay Texts
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = "Montréal, Canada",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            Text(
                text = "Douala/Relais (${destination})",
                color = RFactoSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomEnd)
            )

            if (progressFraction > 0f && progressFraction < 1f) {
                Text(
                    text = String.format("Transit aérien : %.0f%%", progressFraction * 100),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            } else if (progressFraction >= 1.0f) {
                Text(
                    text = "Livré au point de destination",
                    color = StatusDelivered,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            } else {
                Text(
                    text = "En attente de décollage",
                    color = StatusCreated,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
fun MockQRCodeCanvas(code: String) {
    Canvas(
        modifier = Modifier
            .size(130.dp)
            .background(Color.White)
            .padding(8.dp)
            .border(1.dp, Color.LightGray)
    ) {
        val w = size.width
        val h = size.height
        val columns = 8
        val cellW = w / columns
        val cellH = h / columns

        // Draw standard QR Finder squares in corners
        // Top-Left
        drawRect(Color.Black, Offset(0f, 0f), Size(cellW * 2.5f, cellH * 2.5f))
        drawRect(Color.White, Offset(cellW * 0.5f, cellH * 0.5f), Size(cellW * 1.5f, cellH * 1.5f))
        drawRect(Color.Black, Offset(cellW * 0.8f, cellH * 0.8f), Size(cellW * 0.9f, cellH * 0.9f))

        // Top-Right
        drawRect(Color.Black, Offset(w - cellW * 2.5f, 0f), Size(cellW * 2.5f, cellH * 2.5f))
        drawRect(Color.White, Offset(w - cellW * 2f, cellH * 0.5f), Size(cellW * 1.5f, cellH * 1.5f))
        drawRect(Color.Black, Offset(w - cellW * 1.7f, cellH * 0.8f), Size(cellW * 0.9f, cellH * 0.9f))

        // Bottom-Left
        drawRect(Color.Black, Offset(0f, h - cellH * 2.5f), Size(cellW * 2.5f, cellH * 2.5f))
        drawRect(Color.White, Offset(cellW * 0.5f, h - cellH * 2f), Size(cellW * 1.5f, cellH * 1.5f))
        drawRect(Color.Black, Offset(cellW * 0.8f, h - cellH * 1.7f), Size(cellW * 0.9f, cellH * 0.9f))

        // Draw random QR code dots simulation
        val randomBits = intArrayOf(
            0,1,1,0,1,0,0,1,
            1,0,0,1,0,1,1,0,
            0,1,1,0,0,1,0,1,
            1,1,0,1,1,0,1,0,
            0,0,1,0,1,1,0,1,
            1,0,1,1,0,0,1,1,
            0,1,0,0,1,0,1,0,
            1,0,1,0,0,1,0,1
        )

        for (row in 0 until columns) {
            for (col in 0 until columns) {
                // Skip finder square areas
                if (row < 3 && col < 3) continue
                if (row < 3 && col >= columns - 3) continue
                if (row >= columns - 3 && col < 3) continue

                val index = row * columns + col
                if (randomBits[index % randomBits.size] == 1) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(col * cellW, row * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }
        }
    }
}
