package com.rfacto.shipping.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.rfacto.shipping.ui.theme.*
import com.rfacto.shipping.ui.viewmodel.MainViewModel
import com.rfacto.shipping.BuildConfig

val GoogleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12f, 5.04f)
            curveTo(13.88f, 5.04f, 15.56f, 5.68f, 16.89f, 6.95f)
            lineTo(20.2f, 3.64f)
            curveTo(18.19f, 1.77f, 15.35f, 0.63f, 12f, 0.63f)
            curveTo(7.33f, 0.63f, 3.28f, 3.32f, 1.34f, 7.23f)
            lineTo(5.12f, 10.17f)
            curveTo(6.01f, 7.23f, 8.76f, 5.04f, 12f, 5.04f)
            close()
        }
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12f, 18.96f)
            curveTo(8.76f, 18.96f, 6.01f, 16.77f, 5.12f, 13.83f)
            lineTo(1.34f, 16.77f)
            curveTo(3.28f, 20.68f, 7.33f, 23.37f, 12f, 23.37f)
            curveTo(15.29f, 23.37f, 18.25f, 22.25f, 20.28f, 20.31f)
            lineTo(16.79f, 17.6f)
            curveTo(15.52f, 18.47f, 13.91f, 18.96f, 12f, 18.96f)
            close()
        }
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(23.49f, 12.27f)
            curveTo(23.49f, 11.48f, 23.42f, 10.73f, 23.29f, 10f)
            horizontalLineTo(12f)
            verticalLineTo(14.51f)
            horizontalLineTo(18.44f)
            curveTo(18.16f, 16.02f, 17.31f, 17.29f, 16.03f, 18.14f)
            lineTo(19.52f, 20.85f)
            curveTo(21.56f, 18.97f, 23.49f, 15.94f, 23.49f, 12.27f)
            close()
        }
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(5.12f, 13.83f)
            curveTo(4.88f, 13.12f, 4.75f, 12.37f, 4.75f, 11.59f)
            curveTo(4.75f, 10.81f, 4.88f, 10.06f, 5.12f, 9.35f)
            lineTo(1.34f, 6.41f)
            curveTo(0.51f, 8.08f, 0f, 9.96f, 0f, 11.97f)
            curveTo(0f, 13.98f, 0.51f, 15.86f, 1.34f, 17.53f)
            lineTo(5.12f, 13.83f)
            close()
        }
    }.build()

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("google_sign_in_button"),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSystemInDarkTheme()) Color(0xFF1E293B) else Color.White,
            contentColor = if (isSystemInDarkTheme()) Color.White else Color(0xFF1F2937)
        )
    ) {
        Icon(
            imageVector = GoogleIcon,
            contentDescription = "Google Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Continuer avec Google",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1F2937)
        )
    }
}


@Composable
fun SplashView(viewModel: MainViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SlateDarkBg, Color(0xFF0F172A), Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Elegant Paper Plane Cargo Box Icon Simulation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(RFactoPrimary, RFactoSecondary)))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "RFacto Logo",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "RFacto",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Transit & Expédition Canada ➔ Monde",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = RFactoSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Rendre l'expédition de colis simple, transparente et traçable de bout en bout.",
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.setRoute("login") },
                colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("splash_login_button")
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Se connecter", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.setRoute("signup") },
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(listOf(RFactoPrimary, RFactoSecondary))
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("splash_signup_button")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = RFactoPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Créer un compte", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(viewModel: MainViewModel) {
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    val rememberMe by viewModel.loginRememberMe.collectAsState()
    val error by viewModel.loginError.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connexion") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setRoute("splash") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bon retour !",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Accédez à votre tableau de bord d'expédition RFacto.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
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
                value = email,
                onValueChange = { viewModel.loginEmail.value = it },
                label = { Text("Email ou Téléphone") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.loginPassword.value = it },
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.loginRememberMe.value = it },
                        modifier = Modifier.testTag("login_remember_checkbox")
                    )
                    Text("Se souvenir de moi", fontSize = 13.sp)
                }

                Text(
                    text = "Mot de passe oublié ?",
                    fontSize = 13.sp,
                    color = RFactoPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { viewModel.setRoute("forgot_password") }
                        .testTag("login_forgot_password")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.handleLogin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary)
            ) {
                Text("Se connecter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = "OU",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Spacer(modifier = Modifier.height(20.dp))

            val googleIsLoading by viewModel.isGoogleLoading.collectAsState()
            val googleLoadingMsg by viewModel.googleLoadingMessage.collectAsState()

            val context = LocalContext.current
            val gso = remember {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .build()
            }
            val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        viewModel.handleGoogleSignInAccount(
                            email = account.email ?: "",
                            fullName = account.displayName ?: "",
                            idToken = account.idToken ?: ""
                        )
                    }
                } catch (e: ApiException) {
                    android.util.Log.e("RFactoAuth", "Google Sign-In failed: ${e.statusCode}")
                    viewModel.loginError.value = "Échec de la connexion Google (${e.statusCode})"
                }
            }

            GoogleSignInButton(
                onClick = { launcher.launch(googleSignInClient.signInIntent) }
            )

            if (googleIsLoading) {
                Dialog(onDismissRequest = {}) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = RFactoPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(googleLoadingMsg)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Nouveau sur RFacto ? ", fontSize = 14.sp)
                Text(
                    text = "Créer un compte",
                    fontSize = 14.sp,
                    color = RFactoPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.setRoute("signup") }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupView(viewModel: MainViewModel) {
    val nom by viewModel.signupNom.collectAsState()
    val prenom by viewModel.signupPrenom.collectAsState()
    val phone by viewModel.signupPhone.collectAsState()
    val email by viewModel.signupEmail.collectAsState()
    val password by viewModel.signupPassword.collectAsState()
    val confirmPassword by viewModel.signupConfirmPassword.collectAsState()
    val pays by viewModel.signupPays.collectAsState()
    val error by viewModel.signupError.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val otpInput by viewModel.otpCodeInput.collectAsState()

    val countries = listOf("Canada", "Gabon", "France")
    var expandedDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (otpSent) "Validation OTP" else "Créer un compte") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (otpSent) viewModel.otpSent.value = false
                        else viewModel.setRoute("splash")
                    }) {
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
            verticalArrangement = Arrangement.Top
        ) {
            if (!otpSent) {
                Text(
                    text = "Rejoignez RFacto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Expédiez de manière simple et traçable en quelques clics.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
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
                    onValueChange = { viewModel.signupNom.value = it },
                    label = { Text("Nom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = prenom,
                    onValueChange = { viewModel.signupPrenom.value = it },
                    label = { Text("Prénom *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { viewModel.signupPhone.value = it },
                    label = { Text("Téléphone (avec indicatif) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.signupEmail.value = it },
                    label = { Text("Email *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Country Selection Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pays,
                        onValueChange = {},
                        label = { Text("Pays de résidence *") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedDropdown = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        countries.forEach { country ->
                            DropdownMenuItem(
                                text = { Text(country) },
                                onClick = {
                                    viewModel.signupPays.value = country
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.signupPassword.value = it },
                    label = { Text("Mot de passe *") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { viewModel.signupConfirmPassword.value = it },
                    label = { Text("Confirmer le mot de passe *") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.handleSignUpInitiate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("signup_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary)
                ) {
                    Text("Créer mon compte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                    Text(
                        text = "OU",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                val googleIsLoading by viewModel.isGoogleLoading.collectAsState()
                val googleLoadingMsg by viewModel.googleLoadingMessage.collectAsState()

                val context = LocalContext.current
                val gso = remember {
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .build()
                }
                val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        if (account != null) {
                            viewModel.handleGoogleSignInAccount(
                                email = account.email ?: "",
                                fullName = account.displayName ?: "",
                                idToken = account.idToken ?: ""
                            )
                        }
                    } catch (e: ApiException) {
                        android.util.Log.e("RFactoAuth", "Google Sign-In failed: ${e.statusCode}")
                        viewModel.signupError.value = "Échec de la connexion Google (${e.statusCode})"
                    }
                }

                GoogleSignInButton(
                    onClick = { launcher.launch(googleSignInClient.signInIntent) }
                )

                if (googleIsLoading) {
                    Dialog(onDismissRequest = {}) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = RFactoPrimary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(googleLoadingMsg)
                            }
                        }
                    }
                }
            } else {
                // OTP View
                Text(
                    text = "Code de validation",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Un code de validation de compte (OTP) a été envoyé au numéro ${phone}. Veuillez le renseigner ci-dessous pour activer votre compte RFacto.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    lineHeight = 20.sp
                )

                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
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
                    value = otpInput,
                    onValueChange = { viewModel.otpCodeInput.value = it },
                    label = { Text("Entrez le code OTP (123456)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.handleSignUpVerify() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("otp_submit_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RFactoSecondary)
                ) {
                    Text("Valider & Activer mon compte", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        viewModel.addNotification("Code de validation renvoyé: 123456")
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Renvoyer le code", color = RFactoPrimary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordView(viewModel: MainViewModel) {
    val email by viewModel.forgotEmail.collectAsState()
    val otpSent by viewModel.forgotOtpSent.collectAsState()
    val otpInput by viewModel.forgotOtpInput.collectAsState()
    val newPassword by viewModel.forgotNewPassword.collectAsState()
    val successMsg by viewModel.forgotSuccessMsg.collectAsState()
    val errorMsg by viewModel.forgotErrorMsg.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mot de passe oublié") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setRoute("login") }) {
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Récupération",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Réinitialisez votre mot de passe en quelques instants.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (errorMsg != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMsg ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

            if (successMsg != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = successMsg ?: "",
                        color = Color(0xFF065F46),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!otpSent) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.forgotEmail.value = it },
                    label = { Text("Adresse Email ou Téléphone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.handleForgotPasswordRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RFactoPrimary)
                ) {
                    Text("Recevoir le code OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { viewModel.forgotOtpInput.value = it },
                    label = { Text("Entrez le code OTP (998877)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { viewModel.forgotNewPassword.value = it },
                    label = { Text("Nouveau mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.handleForgotPasswordReset() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RFactoSecondary)
                ) {
                    Text("Créer le nouveau mot de passe", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
