package com.rfacto.shipping.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclareColisView(viewModel: MainViewModel) {
    val nom by viewModel.declNom.collectAsState()
    val desc by viewModel.declDesc.collectAsState()
    val poids by viewModel.declPoidsEst.collectAsState()
    val dim by viewModel.declDim.collectAsState()
    val valeur by viewModel.declValeur.collectAsState()
    val paysDest by viewModel.declPaysDest.collectAsState()
    val ville by viewModel.declVille.collectAsState()
    val adresse by viewModel.declAdresse.collectAsState()
    val modeLivraison by viewModel.declModeLivraison.collectAsState()
    val photoUri by viewModel.declPhotoUri.collectAsState()
    val error by viewModel.declError.collectAsState()
    val isPaymentLoading by viewModel.isPaymentLoading.collectAsState()

    var dropdownCountryExpanded by remember { mutableStateOf(false) }
    val countries = listOf("Canada", "Gabon", "France")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.declPhotoUri.value = uri?.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Déclarer un colis") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setRoute("dashboard") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Formulaire de déclaration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Déclarez le contenu de votre colis afin d'obtenir votre numéro RFacto unique et l'adresse de transit.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

            OutlinedTextField(
                value = nom,
                onValueChange = { viewModel.declNom.value = it },
                label = { Text("Nom du colis (ex: Vêtements, MacBook) *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decl_nom_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { viewModel.declDesc.value = it },
                label = { Text("Description détaillée du contenu *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("decl_desc_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = poids,
                    onValueChange = { viewModel.declPoidsEst.value = it },
                    label = { Text("Poids estimé (kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("decl_poids_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = dim,
                    onValueChange = { viewModel.declDim.value = it },
                    label = { Text("Dimensions L x l x h (cm) *") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("decl_dim_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = valeur,
                onValueChange = { viewModel.declValeur.value = it },
                label = { Text("Valeur déclarée ($ CAD) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("decl_valeur_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Photo Selection
            Text(
                text = "Photo du colis",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Photo du colis",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.declPhotoUri.value = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = Color.White)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = RFactoPrimary, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ajouter une photo du colis", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Country selection
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = paysDest,
                    onValueChange = {},
                    label = { Text("Pays de destination *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { dropdownCountryExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = dropdownCountryExpanded,
                    onDismissRequest = { dropdownCountryExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    countries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country) },
                            onClick = {
                                viewModel.declPaysDest.value = country
                                dropdownCountryExpanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ville,
                    onValueChange = { viewModel.declVille.value = it },
                    label = { Text("Ville de livraison *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = adresse,
                    onValueChange = { viewModel.declAdresse.value = it },
                    label = { Text("Adresse précise *") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Mode de livraison
            Text(
                text = "Mode de livraison",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (modeLivraison == "POINT_RELAIS")
                            RFactoPrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.declModeLivraison.value = "POINT_RELAIS" }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        RadioButton(
                            selected = modeLivraison == "POINT_RELAIS",
                            onClick = { viewModel.declModeLivraison.value = "POINT_RELAIS" }
                        )
                        Text(
                            text = "Point relais",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Économique, retrait rapide",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (modeLivraison == "LIVRAISON_DOMICILE")
                            RFactoPrimary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.declModeLivraison.value = "LIVRAISON_DOMICILE" }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        RadioButton(
                            selected = modeLivraison == "LIVRAISON_DOMICILE",
                            onClick = { viewModel.declModeLivraison.value = "LIVRAISON_DOMICILE" }
                        )
                        Text(
                            text = "Livraison domicile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Remise en mains propres",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isPaymentLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.estimateParcelFees() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("decl_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary),
                enabled = !isPaymentLoading
            ) {
                Text("Voir l'estimation et payer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclarationSuccessView(viewModel: MainViewModel) {
    val colis by viewModel.selectedColis.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Déclaration réussie") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setRoute("dashboard") }) {
                        Icon(Icons.Default.Home, contentDescription = "Accueil")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StatusDelivered.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusDelivered,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Déclaration Validée",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Votre colis a été déclaré dans le système RFacto. Veuillez l'expédier à l'adresse de transit canadienne ci-dessous.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                lineHeight = 18.sp
            )

            // Dynamic generated tracking ID Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VOTRE NUMÉRO DE COLIS UNIQUE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = colis?.numero ?: "RFC-2026-000125",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = RFactoPrimary,
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Indiquez obligatoirement ce numéro sur votre carton d'emballage.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Canadian Transit Address Card
            Text(
                text = "Adresse de transit au Canada",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = RFactoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RFacto Transit Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    Text(
                        text = "Client : ${colis?.clientName ?: "Jean Ndong"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "ID Colis : ${colis?.numero ?: "RFC-2026-000125"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = RFactoPrimary,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "127 rue Talbot",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Laurier-station, Québec",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Canada",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.setRoute("dashboard") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RFactoSecondary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Retour au tableau de bord", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclarationPaymentView(viewModel: MainViewModel) {
    val estimatedPrice by viewModel.declEstimatedPrice.collectAsState()
    val paysDest by viewModel.declPaysDest.collectAsState()
    val modeLivraison by viewModel.declModeLivraison.collectAsState()
    val stripeConfig by viewModel.stripeCustomerConfig.collectAsState()
    val isPaymentLoading by viewModel.isPaymentLoading.collectAsState()

    val paymentSheet = com.stripe.android.paymentsheet.rememberPaymentSheet { result ->
        viewModel.handleStripeResult(result)
    }

    // Effect to trigger Stripe UI when config is received from backend
    LaunchedEffect(stripeConfig) {
        stripeConfig?.let { config ->
            val googlePayConfig = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Test,
                countryCode = "CA"
            )
            
            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret = config.paymentIntentClientSecret,
                configuration = PaymentSheet.Configuration(
                    merchantDisplayName = "RFacto Shipping",
                    customer = PaymentSheet.CustomerConfiguration(
                        id = config.customerId,
                        ephemeralKeySecret = config.ephemeralKeySecret
                    ),
                    googlePay = googlePayConfig,
                    allowsDelayedPaymentMethods = false
                )
            )
            // Reset config to avoid re-triggering on recomposition
            viewModel.stripeCustomerConfig.value = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paiement des frais") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setRoute("declare_colis") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimation des frais",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = RFactoPrimary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MONTANT À RÉGLER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RFactoPrimary)
                    Text("$estimatedPrice $", fontSize = 36.sp, fontWeight = FontWeight.Black, color = RFactoPrimary)
                    Text("Destination : $paysDest", fontSize = 14.sp)
                    Text("Mode : ${if(modeLivraison == "POINT_RELAIS") "Point Relais" else "Domicile"}", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Sélectionnez votre mode de paiement", fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.align(Alignment.Start))
            
            Spacer(modifier = Modifier.height(16.dp))

            PaymentModeItem("Mobile Money (Orange/MTN/Moov)", Icons.Default.Smartphone) {
                viewModel.declareParcelAfterPayment("MOBILE_MONEY")
            }
            Spacer(modifier = Modifier.height(12.dp))
            PaymentModeItem("Carte Bancaire (Stripe)", Icons.Default.CreditCard, enabled = !isPaymentLoading) {
                viewModel.prepareStripePayment()
            }
            if (isPaymentLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            PaymentModeItem("PayPal", Icons.Default.AccountBalanceWallet) {
                viewModel.declareParcelAfterPayment("PAYPAL")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Le numéro de suivi RFC vous sera attribué immédiatement après confirmation du paiement.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PaymentModeItem(
    name: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        onClick = { if (enabled) onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = RFactoPrimary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}
