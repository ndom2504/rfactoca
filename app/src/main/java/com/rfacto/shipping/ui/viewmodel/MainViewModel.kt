package com.rfacto.shipping.ui.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfacto.shipping.data.api.CreatePaymentIntentRequest
import com.rfacto.shipping.data.api.PaymentIntentResponse
import com.rfacto.shipping.data.api.RFactoApi
import com.rfacto.shipping.data.model.*
import com.rfacto.shipping.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Random

class MainViewModel(private val repository: AppRepository, private val contentResolver: ContentResolver? = null) : ViewModel() {

    // Active User & Role
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentRole = MutableStateFlow("CLIENT") // "CLIENT", "AGENT_CANADA", "AGENT_LOCAL", "ADMIN"
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // All database data lists
    val allColis = repository.allColis.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allUsers = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTarifs = repository.allTarifs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPaiements = repository.allPaiements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAgences = repository.allAgences.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered lists
    val clientColis = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getColisForClient(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active selected colis for tracking/details
    private val _selectedColis = MutableStateFlow<Colis?>(null)
    val selectedColis: StateFlow<Colis?> = _selectedColis.asStateFlow()

    val selectedColisSuivi = _selectedColis.flatMapLatest { colis ->
        if (colis == null) flowOf(emptyList())
        else repository.getSuiviForColis(colis.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedColisPaiements = _selectedColis.flatMapLatest { colis ->
        if (colis == null) flowOf(emptyList())
        else repository.getPaiementsForColis(colis.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-app Notification simulation
    private val _notifications = MutableStateFlow<List<String>>(emptyList())
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    // Active screen navigation route state helper
    private val _navigationRoute = MutableStateFlow("splash")
    val navigationRoute: StateFlow<String> = _navigationRoute.asStateFlow()

    // Form inputs: Auth Login
    var loginEmail = MutableStateFlow("")
    var loginPassword = MutableStateFlow("")
    var loginRememberMe = MutableStateFlow(true)
    var loginError = MutableStateFlow<String?>(null)

    // Form inputs: Auth Signup
    var signupNom = MutableStateFlow("")
    var signupPrenom = MutableStateFlow("")
    var signupPhone = MutableStateFlow("")
    var signupEmail = MutableStateFlow("")
    var signupPassword = MutableStateFlow("")
    var signupConfirmPassword = MutableStateFlow("")
    var signupPays = MutableStateFlow("Gabon")
    var signupError = MutableStateFlow<String?>(null)
    var otpSent = MutableStateFlow(false)
    var otpCodeInput = MutableStateFlow("")
    var otpGeneratedCode = "123456" // Simple static code for simulation

    // Form inputs: Forgot Password
    var forgotEmail = MutableStateFlow("")
    var forgotOtpSent = MutableStateFlow(false)
    var forgotOtpInput = MutableStateFlow("")
    var forgotNewPassword = MutableStateFlow("")
    var forgotSuccessMsg = MutableStateFlow<String?>(null)
    var forgotErrorMsg = MutableStateFlow<String?>(null)

    // Google Sign-In States
    var showGoogleSignInSheet = MutableStateFlow(false)
    var isGoogleLoading = MutableStateFlow(false)
    var googleLoadingMessage = MutableStateFlow("")

    // Form inputs: Declare Colis
    var declNom = MutableStateFlow("")
    var declDesc = MutableStateFlow("")
    var declPoidsEst = MutableStateFlow("2.0")
    var declDim = MutableStateFlow("30x20x15")
    var declValeur = MutableStateFlow("100.0")
    var declPaysDest = MutableStateFlow("Gabon")
    var declVille = MutableStateFlow("Libreville")
    var declAdresse = MutableStateFlow("RFacto Libreville Relay")
    var declModeLivraison = MutableStateFlow("POINT_RELAIS") // "POINT_RELAIS", "LIVRAISON_DOMICILE"
    var declPhotoUri = MutableStateFlow<String?>(null)
    var declEstimatedPrice = MutableStateFlow(0.0)
    var declError = MutableStateFlow<String?>(null)
    var newlyCreatedColis = MutableStateFlow<Colis?>(null)

    // Stripe Payment States
    var stripePaymentSheetResult = MutableStateFlow<String?>(null)
    var stripeCustomerConfig = MutableStateFlow<PaymentIntentResponse?>(null)
    var isPaymentLoading = MutableStateFlow(false)

    // Form inputs: Agent Canada Receive
    var agentPoidsReel = MutableStateFlow("")
    var agentDimReelles = MutableStateFlow("")
    var agentEtatColis = MutableStateFlow("Excellent")
    var agentPhoto = MutableStateFlow("photo_standard.jpg")

    // Form inputs: Admin Rates Management
    var adminPays = MutableStateFlow("")
    var adminPrixKg = MutableStateFlow("15.0")
    var adminLivLocal = MutableStateFlow("8.0")
    var adminAssurance = MutableStateFlow("5.0")

    // Profile Settings
    var profileNom = MutableStateFlow("")
    var profilePrenom = MutableStateFlow("")
    var profilePhone = MutableStateFlow("")
    var profileEmail = MutableStateFlow("")
    var profilePays = MutableStateFlow("")
    var profilePhoto = MutableStateFlow<String?>(null)
    var profileVille = MutableStateFlow("")
    var profileAdresse = MutableStateFlow("")
    var profilePrefLangue = MutableStateFlow("Français")
    var profilePrefNotifSms = MutableStateFlow(true)
    var profilePrefNotifEmail = MutableStateFlow(true)
    var profilePrefNotifPush = MutableStateFlow(true)
    var profileError = MutableStateFlow<String?>(null)
    var profileSuccess = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.initializeSeedData()
            // Auto-login to Client "Jean Ndong" for rich first-use experience:
            val user = repository.getUserByEmail("jean@rfacto.com")
            if (user != null) {
                _currentUser.value = user
                _currentRole.value = "CLIENT"
                syncProfileFields(user)
            }
            // Add a few initial default notifications
            _notifications.value = listOf(
                "Bienvenue sur RFacto ! Votre adresse de transit au Canada est prête.",
                "Notification: Colis RFC-2026-000101 livré avec succès au Gabon.",
                "Notification: Colis RFC-2026-000142 arrivé à Laurier-station. Paiement en attente."
            )
        }
    }

    private fun syncProfileFields(user: User) {
        profileNom.value = user.nom
        profilePrenom.value = user.prenom
        profilePhone.value = user.telephone
        profileEmail.value = user.email
        profilePays.value = user.pays
        profilePhoto.value = user.profilePhoto
        profileVille.value = user.ville ?: ""
        profileAdresse.value = user.adresse ?: ""
        profilePrefLangue.value = user.prefLangue
        profilePrefNotifSms.value = user.prefNotifSms
        profilePrefNotifEmail.value = user.prefNotifEmail
        profilePrefNotifPush.value = user.prefNotifPush
    }

    fun setRoute(route: String) {
        _navigationRoute.value = route
    }

    fun selectColis(colis: Colis) {
        _selectedColis.value = colis
    }

    // Role switcher to ease manual review & full flow simulation
    fun switchRole(role: String) {
        _currentRole.value = role
        viewModelScope.launch {
            val email = when (role) {
                "CLIENT" -> "jean@rfacto.com"
                "AGENT_CANADA" -> "canada@rfacto.com"
                "AGENT_LOCAL" -> "local@rfacto.com"
                "ADMIN" -> "admin@rfacto.com"
                else -> "jean@rfacto.com"
            }
            val user = repository.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                syncProfileFields(user)
            }
        }
    }

    // Notifications simulator
    fun addNotification(message: String) {
        _notifications.value = listOf(message) + _notifications.value
    }

    // 1. Auth Flow Actions
    fun handleLogin() {
        loginError.value = null
        val email = loginEmail.value.trim()
        val pwd = loginPassword.value

        if (email.isEmpty() || pwd.isEmpty()) {
            loginError.value = "Veuillez remplir tous les champs."
            return
        }

        viewModelScope.launch {
            val user = repository.getUserByEmail(email) ?: repository.getUserByPhone(email)
            if (user != null && user.motDePasse == pwd) {
                _currentUser.value = user
                _currentRole.value = user.role
                syncProfileFields(user)
                _navigationRoute.value = "dashboard"
                addNotification("Connexion réussie. Bienvenue, ${user.prenom} !")
            } else {
                loginError.value = "Email/téléphone ou mot de passe incorrect."
            }
        }
    }

    fun handleSignUpInitiate() {
        signupError.value = null
        if (signupNom.value.isEmpty() || signupPrenom.value.isEmpty() ||
            signupEmail.value.isEmpty() || signupPhone.value.isEmpty() ||
            signupPassword.value.isEmpty() || signupConfirmPassword.value.isEmpty()
        ) {
            signupError.value = "Veuillez remplir tous les champs obligatoires."
            return
        }

        if (signupPassword.value != signupConfirmPassword.value) {
            signupError.value = "Les mots de passe ne correspondent pas."
            return
        }

        // Send simulated OTP
        otpSent.value = true
        addNotification("Code de validation RFacto envoyé au ${signupPhone.value} : $otpGeneratedCode")
    }

    fun handleSignUpVerify() {
        if (otpCodeInput.value == otpGeneratedCode) {
            viewModelScope.launch {
                val newUser = User(
                    nom = signupNom.value.trim(),
                    prenom = signupPrenom.value.trim(),
                    email = signupEmail.value.trim(),
                    telephone = signupPhone.value.trim(),
                    motDePasse = signupPassword.value,
                    role = "CLIENT",
                    pays = signupPays.value,
                    statut = "ACTIVE"
                )
                val id = repository.insertUser(newUser)
                val userWithId = newUser.copy(id = id.toInt())
                _currentUser.value = userWithId
                _currentRole.value = "CLIENT"
                syncProfileFields(userWithId)
                otpSent.value = false
                _navigationRoute.value = "dashboard"
                addNotification("Votre compte RFacto a été créé et activé avec succès !")
            }
        } else {
            signupError.value = "Code de validation incorrect. Veuillez réessayer."
        }
    }

    fun handleForgotPasswordRequest() {
        forgotErrorMsg.value = null
        forgotSuccessMsg.value = null
        if (forgotEmail.value.isEmpty()) {
            forgotErrorMsg.value = "Veuillez entrer votre adresse email ou téléphone."
            return
        }
        forgotOtpSent.value = true
        addNotification("Code de récupération envoyé à ${forgotEmail.value} : 998877")
    }

    fun handleForgotPasswordReset() {
        forgotErrorMsg.value = null
        if (forgotOtpInput.value != "998877") {
            forgotErrorMsg.value = "Code incorrect."
            return
        }
        if (forgotNewPassword.value.isEmpty()) {
            forgotErrorMsg.value = "Veuillez entrer un nouveau mot de passe."
            return
        }

        viewModelScope.launch {
            val user = repository.getUserByEmail(forgotEmail.value) ?: repository.getUserByPhone(forgotEmail.value)
            if (user != null) {
                repository.updateUser(user.copy(motDePasse = forgotNewPassword.value))
                forgotSuccessMsg.value = "Mot de passe réinitialisé avec succès. Veuillez vous connecter."
                forgotOtpSent.value = false
                forgotEmail.value = ""
                forgotOtpInput.value = ""
                forgotNewPassword.value = ""
            } else {
                forgotErrorMsg.value = "Utilisateur introuvable."
            }
        }
    }

    fun handleLogout() {
        _currentUser.value = null
        _selectedColis.value = null
        loginEmail.value = ""
        loginPassword.value = ""
        _navigationRoute.value = "splash"
    }

    fun handleGoogleSignInAccount(email: String, fullName: String, idToken: String) {
        viewModelScope.launch {
            isGoogleLoading.value = true
            googleLoadingMessage.value = "Connexion sécurisée avec Google..."
            
            // Simulation d'un délai réseau pour l'expérience utilisateur
            kotlinx.coroutines.delay(800)
            
            // Tentative de synchronisation avec le backend Neon via Vercel
            // Utilise l'URL définie dans le fichier .env (via BuildConfig)
            val isRemoteConfigured = com.rfacto.shipping.BuildConfig.REMOTE_API_URL.isNotEmpty() && 
                                    !com.rfacto.shipping.BuildConfig.REMOTE_API_URL.contains("PLACEHOLDER")

            if (isRemoteConfigured) {
                try {
                    val api = com.rfacto.shipping.data.api.RFactoApi.getInstance()
                    val response = api.googleSignIn(com.rfacto.shipping.data.api.GoogleSignInRequest(email.trim(), fullName, idToken))
                    if (response.isSuccessful && response.body() != null) {
                        val auth = response.body()!!
                        // On pourrait ici stocker le token JWT de production
                        android.util.Log.d("RFactoAuth", "Token JWT reçu: ${auth.token}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RFactoAuth", "Erreur backend: ${e.message}")
                }
            }
            
            val user = repository.getUserByEmail(email.trim())
            if (user != null) {
                _currentUser.value = user
                _currentRole.value = user.role
                syncProfileFields(user)
                _navigationRoute.value = "dashboard"
                addNotification("Bienvenue, ${user.prenom} (via Google)")
            } else {
                // Création automatique du compte si c'est une première connexion
                val parts = fullName.trim().split(" ", limit = 2)
                val firstName = parts.getOrNull(0) ?: "Utilisateur"
                val lastName = parts.getOrNull(1) ?: "Google"
                
                val newUser = User(
                    nom = lastName,
                    prenom = firstName,
                    email = email.trim(),
                    telephone = "", // À compléter par l'utilisateur plus tard
                    motDePasse = "google_auth_${java.util.UUID.randomUUID()}",
                    role = "CLIENT",
                    pays = "Canada",
                    statut = "ACTIVE"
                )
                val id = repository.insertUser(newUser)
                val userWithId = newUser.copy(id = id.toInt())
                _currentUser.value = userWithId
                _currentRole.value = "CLIENT"
                syncProfileFields(userWithId)
                _navigationRoute.value = "dashboard"
                addNotification("Compte RFacto créé avec succès via Google !")
            }
            isGoogleLoading.value = false
        }
    }

    // 2. Parcel Declaration (Client)
    fun estimateParcelFees() {
        val weight = declPoidsEst.value.toDoubleOrNull() ?: 0.0
        val pays = declPaysDest.value
        val mode = declModeLivraison.value
        val valDecl = declValeur.value.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            // Get rates from repository
            val rates = repository.getTarifByPays(pays)
            val prKg = rates?.prixParKg ?: 15.0
            val livLoc = rates?.livraisonLocale ?: 8.0
            val currentAssuranceBase = rates?.assurance ?: 5.0

            // Logic: Base price by weight + delivery
            val base = weight * prKg
            val deliveryExtra = if (mode == "LIVRAISON_DOMICILE") livLoc else 0.0

            // Insurance logic: 5$ base if > 200$ + 2% of value if > 1000$
            var insuranceExtra = 0.0
            if (valDecl > 200.0) {
                insuranceExtra = currentAssuranceBase
                if (valDecl > 1000.0) {
                    insuranceExtra += (valDecl * 0.02) // 2% extra insurance for high value
                }
            }

            declEstimatedPrice.value = base + deliveryExtra + insuranceExtra
            _navigationRoute.value = "declaration_payment"
        }
    }

    /**
     * Step 1: Request Stripe Payment Intent from Backend
     */
    fun prepareStripePayment() {
        isPaymentLoading.value = true
        viewModelScope.launch {
            try {
                // We create a temporary parcel ID or use -1 if the backend supports it
                // To be clean, the backend usually expects the final amount.
                val client = _currentUser.value ?: return@launch
                val response = RFactoApi.getInstance().createPaymentIntent(
                    CreatePaymentIntentRequest(
                        parcelId = 0, // In this flow, parcel is created AFTER payment success
                        amountCad = declEstimatedPrice.value,
                        userId = client.id
                    )
                )
                
                if (response.isSuccessful && response.body() != null) {
                    stripeCustomerConfig.value = response.body()
                } else {
                    declError.value = "Erreur de connexion au serveur de paiement."
                }
            } catch (e: Exception) {
                declError.value = "Erreur Stripe: ${e.message}"
            } finally {
                isPaymentLoading.value = false
            }
        }
    }

    fun onStripePaymentSuccess() {
        // Once Stripe confirms payment on mobile, we finalize the declaration in our DB/Backend
        declareParcelAfterPayment("STRIPE_CARD")
    }

    fun handleStripeResult(result: com.stripe.android.paymentsheet.PaymentSheetResult) {
        when (result) {
            is com.stripe.android.paymentsheet.PaymentSheetResult.Completed -> {
                onStripePaymentSuccess()
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Canceled -> {
                declError.value = "Paiement annulé."
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Failed -> {
                declError.value = "Échec du paiement : ${result.error.localizedMessage}"
            }
        }
    }

    fun declareParcelAfterPayment(modePaiement: String) {
        declError.value = null
        val nom = declNom.value.trim()
        val desc = declDesc.value.trim()
        val weight = declPoidsEst.value.toDoubleOrNull() ?: 0.0
        val valDecl = declValeur.value.toDoubleOrNull() ?: 0.0

        if (nom.isEmpty() || desc.isEmpty() || weight <= 0.0 || valDecl <= 0.0) {
            declError.value = "Veuillez remplir correctement tous les champs."
            return
        }

        val client = _currentUser.value ?: return
        isPaymentLoading.value = true

        // Generate tracking ID
        val randomSuffix = (100000..999999).random()
        val trackingNo = "RFC-2026-$randomSuffix"

        viewModelScope.launch {
            var finalPhotoUrl = declPhotoUri.value
            
            // Upload photo to cloud if it's a local URI
            if (finalPhotoUrl != null && finalPhotoUrl.startsWith("content://")) {
                val uploadedUrl = uploadParcelPhoto(finalPhotoUrl)
                if (uploadedUrl != null) {
                    finalPhotoUrl = uploadedUrl
                }
            }

            val colis = Colis(
                numero = trackingNo,
                clientId = client.id,
                clientName = "${client.prenom} ${client.nom}",
                description = "$nom - $desc",
                poids = weight,
                dimensions = declDim.value,
                valeur = valDecl,
                photo = finalPhotoUrl,
                paysDestination = declPaysDest.value,
                ville = declVille.value,
                adresseDestination = declAdresse.value,
                modeLivraison = declModeLivraison.value,
                statut = "PAIEMENT_VALIDE"
            )
            val id = repository.insertColis(colis)
            val savedColis = colis.copy(id = id.toInt())
            newlyCreatedColis.value = savedColis
            _selectedColis.value = savedColis

            // Sync with backend: Remote Parcel Creation
            try {
                val api = RFactoApi.getInstance()
                api.createColis(com.rfacto.shipping.data.api.CreateColisRequest(
                    numero = trackingNo,
                    clientId = client.id,
                    clientName = "${client.prenom} ${client.nom}",
                    description = "$nom - $desc",
                    poids = weight,
                    dimensions = declDim.value,
                    valeur = valDecl,
                    photo = finalPhotoUrl,
                    paysDestination = declPaysDest.value,
                    ville = declVille.value,
                    adresseDestination = declAdresse.value,
                    modeLivraison = declModeLivraison.value,
                    statut = "PAIEMENT_VALIDE"
                ))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Remote parcel creation failed: ${e.message}")
            }

            // Insert payment record
            repository.insertPaiement(Paiement(
                colisId = id.toInt(),
                colisNumero = trackingNo,
                montant = declEstimatedPrice.value,
                mode = modePaiement,
                statut = "PAID"
            ))

            // Sync payment with backend
            try {
                RFactoApi.getInstance().syncPayment(com.rfacto.shipping.data.api.SyncPaymentRequest(
                    colisId = id.toInt(),
                    montant = declEstimatedPrice.value,
                    mode = modePaiement,
                    statut = "PAID"
                ))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Remote payment sync failed: ${e.message}")
            }

            // Insert initial tracking log
            repository.insertSuivi(Suivi(
                colisId = id.toInt(),
                statut = "CREE",
                lieu = "Canada (Client)",
                commentaire = "Déclaration effectuée et paiement validé ($modePaiement). En attente de dépôt."
            ))

            addNotification("Nouveau colis payé et déclaré : $trackingNo.")
            isPaymentLoading.value = false
            _navigationRoute.value = "declaration_success"

            // Reset declaration fields
            resetDeclFields()
        }
    }

    private fun resetDeclFields() {
        declNom.value = ""
        declDesc.value = ""
        declPoidsEst.value = "2.0"
        declDim.value = "30x20x15"
        declValeur.value = "100.0"
        declPaysDest.value = "Gabon"
        declVille.value = "Libreville"
        declAdresse.value = "RFacto Libreville Relay"
        declPhotoUri.value = null
        declEstimatedPrice.value = 0.0
    }

    // Cancellation logic
    fun deleteOrCancelColis(colis: Colis) {
        viewModelScope.launch {
            if (colis.statut == "CREE" || colis.statut == "EN_ATTENTE_RECEPTION") {
                // Simple deletion if not paid or just created
                repository.deleteColis(colis.id)
                addNotification("Colis ${colis.numero} supprimé.")
            } else if (colis.statut == "PAIEMENT_VALIDE") {
                // Cancellation with 20% loss
                val payments = repository.getPaiementsForColis(colis.id).first()
                val totalPaid = payments.sumOf { it.montant }
                val refundAmount = totalPaid * 0.8
                val lossAmount = totalPaid * 0.2
                
                repository.updateColis(colis.copy(statut = "ANNULE"))
                
                // Sync cancellation with backend
                try {
                    RFactoApi.getInstance().updateColisStatus(com.rfacto.shipping.data.api.UpdateColisStatusRequest(
                        colisId = colis.id,
                        statut = "ANNULE",
                        lieu = "Système RFacto",
                        commentaire = "Annulation par le client. Remboursement de $refundAmount $ (Pénalité 20%: $lossAmount $)."
                    ))
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "Remote cancellation sync failed: ${e.message}")
                }

                addNotification("Colis ${colis.numero} annulé. Remboursement de $refundAmount $ effectué (Pénalité de 20% : $lossAmount $)")
                
                repository.insertSuivi(Suivi(
                    colisId = colis.id,
                    statut = "ANNULE",
                    lieu = "Système RFacto",
                    commentaire = "Annulation par le client. Remboursement partiel (80%) traité."
                ))
            } else {
                addNotification("Impossible d'annuler un colis déjà en cours d'expédition internationale.")
            }
        }
    }

    fun updateColisDetails(colis: Colis, newDesc: String, newPoids: Double) {
        if (colis.statut == "PAIEMENT_VALIDE" || colis.statut == "RECU_CANADA") {
             // Logic could be added to recalculate price if weight changes, but user asked for modification before payment
        }
        viewModelScope.launch {
            repository.updateColis(colis.copy(description = newDesc, poids = newPoids))
            addNotification("Colis ${colis.numero} mis à jour.")
        }
    }

    // 3. Canada Agent Receive Parcel
    fun receiveParcelAtCanadaTransit(colisId: Int) {
        val pReal = agentPoidsReel.value.toDoubleOrNull() ?: 0.0
        val dims = agentDimReelles.value.trim()
        val etat = agentEtatColis.value
        val photo = agentPhoto.value

        if (pReal <= 0.0 || dims.isEmpty()) {
            return
        }

        isPaymentLoading.value = true
        viewModelScope.launch {
            val colis = repository.getColisById(colisId) ?: return@launch

            var finalPhotoUrl = photo
            if (photo.startsWith("content://")) {
                val uploadedUrl = uploadParcelPhoto(photo)
                if (uploadedUrl != null) {
                    finalPhotoUrl = uploadedUrl
                }
            }

            val updatedColis = colis.copy(
                poids = pReal,
                dimensions = dims,
                photo = finalPhotoUrl,
                statut = "RECU_CANADA"
            )
            repository.updateColis(updatedColis)

            // Sync status update with backend
            try {
                RFactoApi.getInstance().updateColisStatus(com.rfacto.shipping.data.api.UpdateColisStatusRequest(
                    colisId = colisId,
                    statut = "RECU_CANADA",
                    lieu = "Transit Montréal, Canada",
                    commentaire = "Colis réceptionné et inspecté par l'agent. Poids réel: $pReal kg, Dimensions: $dims. État: $etat.",
                    poids = pReal,
                    dimensions = dims,
                    photo = finalPhotoUrl
                ))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Remote status update failed: ${e.message}")
            }

            // Add payment entry based on calculations
            val rates = repository.getTarifByPays(colis.paysDestination)
            val prKg = rates?.prixParKg ?: 15.0
            val livLoc = rates?.livraisonLocale ?: 8.0
            val ass = if (colis.valeur > 200.0) rates?.assurance ?: 5.0 else 0.0
            val totalFee = (pReal * prKg) + (if (colis.modeLivraison == "LIVRAISON_DOMICILE") livLoc else 0.0) + ass

            repository.insertPaiement(Paiement(
                colisId = colis.id,
                colisNumero = colis.numero,
                montant = totalFee,
                mode = "MOBILE_MONEY",
                statut = "PENDING"
            ))

            // Sync payment entry with backend
            try {
                RFactoApi.getInstance().syncPayment(com.rfacto.shipping.data.api.SyncPaymentRequest(
                    colisId = colisId,
                    montant = totalFee,
                    mode = "MOBILE_MONEY",
                    statut = "PENDING"
                ))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Remote payment sync failed: ${e.message}")
            }

            // Add suivi tracking item
            repository.insertSuivi(Suivi(
                colisId = colis.id,
                statut = "RECU_CANADA",
                lieu = "Transit Montréal, Canada",
                commentaire = "Colis réceptionné et inspecté par l'agent. Poids réel: $pReal kg, Dimensions: $dims. État: $etat. Calcul des frais effectué: $totalFee $."
            ))

            addNotification("Agent Canada: Colis ${colis.numero} réceptionné avec succès. Frais d'expédition calculés : $totalFee $.")
            isPaymentLoading.value = false
            _selectedColis.value = updatedColis
            _navigationRoute.value = "colis_detail"
        }
    }

    // 4. Pay Parcel fees
    fun payParcelFees(colisId: Int, modePaiement: String) {
        viewModelScope.launch {
            val colis = repository.getColisById(colisId) ?: return@launch
            val updatedColis = colis.copy(statut = "PAIEMENT_VALIDE")
            repository.updateColis(updatedColis)

            // Sync status update with backend
            try {
                RFactoApi.getInstance().updateColisStatus(com.rfacto.shipping.data.api.UpdateColisStatusRequest(
                    colisId = colisId,
                    statut = "PAIEMENT_VALIDE",
                    lieu = "En ligne",
                    commentaire = "Paiement validé avec succès via $modePaiement."
                ))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Remote status update failed: ${e.message}")
            }

            // Update payment history
            val paiements = repository.getPaiementsForColis(colisId).first()
            if (paiements.isNotEmpty()) {
                val p = paiements.first()
                val updatedP = p.copy(statut = "PAID", mode = modePaiement)
                repository.updatePaiement(updatedP)
                
                // Sync updated payment with backend
                try {
                    RFactoApi.getInstance().syncPayment(com.rfacto.shipping.data.api.SyncPaymentRequest(
                        colisId = colisId,
                        montant = updatedP.montant,
                        mode = modePaiement,
                        statut = "PAID"
                    ))
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "Remote payment sync failed: ${e.message}")
                }
            } else {
                // If somehow empty, create paid record
                val newP = Paiement(
                    colisId = colisId, colisNumero = colis.numero,
                    montant = 48.0, mode = modePaiement, statut = "PAID"
                )
                repository.insertPaiement(newP)

                // Sync new payment with backend
                try {
                    RFactoApi.getInstance().syncPayment(com.rfacto.shipping.data.api.SyncPaymentRequest(
                        colisId = colisId,
                        montant = newP.montant,
                        mode = modePaiement,
                        statut = "PAID"
                    ))
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "Remote payment sync failed: ${e.message}")
                }
            }

            // Insert tracking log
            repository.insertSuivi(Suivi(
                colisId = colisId,
                statut = "PAIEMENT_VALIDE",
                lieu = "En ligne",
                commentaire = "Paiement validé avec succès via $modePaiement. Colis en attente de préparation pour l'expédition internationale."
            ))

            addNotification("Frais d'expédition pour le colis ${colis.numero} réglés avec succès !")
            _selectedColis.value = updatedColis
            _navigationRoute.value = "colis_detail"
        }
    }

    // 5. Update Status Flow (Agent Canada, Agent Local or Admin can update status sequentially)
    fun advanceParcelStatus(colisId: Int, nextStatus: String, lieu: String, commentaire: String) {
        viewModelScope.launch {
            val colis = repository.getColisById(colisId) ?: return@launch
            val updatedColis = colis.copy(statut = nextStatus)
            repository.updateColis(updatedColis)

            // Sync status update with backend
            try {
                RFactoApi.getInstance().updateColisStatus(com.rfacto.shipping.data.api.UpdateColisStatusRequest(
                    colisId = colisId,
                    statut = nextStatus,
                    lieu = lieu,
                    commentaire = commentaire
                ))
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Remote status update failed: ${e.message}")
            }

            // Insert suivi tracking entry
            repository.insertSuivi(Suivi(
                colisId = colisId,
                statut = nextStatus,
                lieu = lieu,
                commentaire = commentaire
            ))

            addNotification("Statut de ${colis.numero} mis à jour : $nextStatus (${lieu})")
            _selectedColis.value = updatedColis
        }
    }

    // 6. Tarifs Configuration (Admin)
    fun addOrUpdateTarif() {
        val pays = adminPays.value.trim()
        val prKg = adminPrixKg.value.toDoubleOrNull() ?: 15.0
        val livLoc = adminLivLocal.value.toDoubleOrNull() ?: 8.0
        val ass = adminAssurance.value.toDoubleOrNull() ?: 5.0

        if (pays.isEmpty()) return

        viewModelScope.launch {
            val existing = repository.getTarifByPays(pays)
            if (existing != null) {
                repository.updateTarif(existing.copy(prixParKg = prKg, livraisonLocale = livLoc, assurance = ass))
                addNotification("Tarifs pour le pays $pays mis à jour avec succès.")
            } else {
                repository.insertTarif(Tarifs(pays = pays, prixParKg = prKg, livraisonLocale = livLoc, assurance = ass))
                addNotification("Tarifs pour le pays $pays ajoutés avec succès.")
            }
            adminPays.value = ""
            adminPrixKg.value = "15.0"
            adminLivLocal.value = "8.0"
            adminAssurance.value = "5.0"
        }
    }

    // Update User Profile Settings
    fun updateProfile() {
        profileError.value = null
        profileSuccess.value = null
        val user = _currentUser.value ?: return

        if (profileNom.value.isEmpty() || profilePrenom.value.isEmpty() || profilePhone.value.isEmpty()) {
            profileError.value = "Veuillez remplir les champs obligatoires."
            return
        }

        viewModelScope.launch {
            try {
                // local update
                val updatedUser = user.copy(
                    nom = profileNom.value.trim(),
                    prenom = profilePrenom.value.trim(),
                    telephone = profilePhone.value.trim(),
                    pays = profilePays.value.trim(),
                    profilePhoto = profilePhoto.value,
                    ville = profileVille.value.trim(),
                    adresse = profileAdresse.value.trim(),
                    prefLangue = profilePrefLangue.value,
                    prefNotifSms = profilePrefNotifSms.value,
                    prefNotifEmail = profilePrefNotifEmail.value,
                    prefNotifPush = profilePrefNotifPush.value
                )
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser

                // remote update
                val api = com.rfacto.shipping.data.api.RFactoApi.getInstance()
                api.updateProfile(
                    com.rfacto.shipping.data.api.UpdateProfileRequest(
                        userId = user.id,
                        nom = profileNom.value.trim(),
                        prenom = profilePrenom.value.trim(),
                        telephone = profilePhone.value.trim(),
                        photoUrl = profilePhoto.value ?: "",
                        ville = profileVille.value.trim(),
                        adresse = profileAdresse.value.trim()
                    )
                )

                profileSuccess.value = "Profil mis à jour avec succès !"
                addNotification("Votre profil utilisateur a été mis à jour.")
            } catch (e: Exception) {
                profileError.value = "Erreur de synchronisation : ${e.localizedMessage}"
            }
        }
    }

    /**
     * Upload photo using Signed URL logic (Cloudinary/S3)
     */
    suspend fun uploadParcelPhoto(uri: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val api = RFactoApi.getInstance()
                val fileName = "parcel_${System.currentTimeMillis()}.jpg"
                val response = api.getColisUploadUrl(fileName, "image/jpeg")
                
                if (response.isSuccessful && response.body() != null) {
                    val uploadData = response.body()!!
                    val uploadUrl = uploadData.uploadUrl
                    val publicUrl = uploadData.publicUrl

                    if (contentResolver != null) {
                        val inputStream = contentResolver.openInputStream(Uri.parse(uri))
                        val bytes = inputStream?.readBytes()
                        inputStream?.close()

                        if (bytes != null) {
                            val client = OkHttpClient()
                            val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            val uploadRequest = Request.Builder()
                                .url(uploadUrl)
                                .put(requestBody)
                                .build()

                            val uploadResponse = client.newCall(uploadRequest).execute()
                            if (uploadResponse.isSuccessful) {
                                return@withContext publicUrl
                            } else {
                                android.util.Log.e("MainViewModel", "Binary upload failed: ${uploadResponse.message}")
                            }
                        }
                    }
                } else {
                    android.util.Log.e("MainViewModel", "Failed to get signed URL: ${response.message()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Parcel photo upload failed: ${e.localizedMessage}")
            }
            null
        }
    }

    fun uploadAndSetProfilePhoto(uri: String) {
        viewModelScope.launch {
            try {
                val api = com.rfacto.shipping.data.api.RFactoApi.getInstance()
                // 1. Get signed URL
                val response = api.getProfileUploadUrl(
                    fileName = "profile_${_currentUser.value?.id}.jpg",
                    fileType = "image/jpeg"
                )
                
                if (response.isSuccessful && response.body() != null) {
                    val uploadData = response.body()!!
                    val uploadUrl = uploadData.uploadUrl
                    val publicUrl = uploadData.publicUrl

                    // 2. Perform direct upload to S3/Cloudinary using OkHttp
                    if (contentResolver != null) {
                        withContext(Dispatchers.IO) {
                            val inputStream = contentResolver.openInputStream(Uri.parse(uri))
                            val bytes = inputStream?.readBytes()
                            inputStream?.close()

                            if (bytes != null) {
                                val client = OkHttpClient()
                                val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                val uploadRequest = Request.Builder()
                                    .url(uploadUrl)
                                    .put(requestBody)
                                    .build()

                                val uploadResponse = client.newCall(uploadRequest).execute()
                                if (!uploadResponse.isSuccessful) {
                                    throw Exception("Failed to upload to storage: ${uploadResponse.message}")
                                }
                            } else {
                                throw Exception("Could not read image data")
                            }
                        }
                    } else {
                        // Fallback/Simulated if contentResolver is null (e.g. in tests)
                        android.util.Log.w("MainViewModel", "ContentResolver is null, skipping actual binary upload")
                    }
                    
                    // 3. Update local and remote profile with the public URL
                    updateProfilePhoto(publicUrl)
                    
                    // Sync with backend
                    _currentUser.value?.let { user ->
                        api.updateProfile(
                            com.rfacto.shipping.data.api.UpdateProfileRequest(
                                userId = user.id,
                                nom = user.nom,
                                prenom = user.prenom,
                                telephone = user.telephone,
                                photoUrl = publicUrl,
                                ville = user.ville ?: "",
                                adresse = user.adresse ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                profileError.value = "Erreur d'upload : ${e.localizedMessage}"
            }
        }
    }

    fun updateProfilePhoto(uriString: String?) {
        profilePhoto.value = uriString
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updatedUser = user.copy(profilePhoto = uriString)
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            addNotification("Photo de profil mise à jour !")
        }
    }

    // Stripe & Remote Vercel/Neon Backend State Management
    val stripeLoading = MutableStateFlow(false)
    val stripeError = MutableStateFlow<String?>(null)
    val stripePaymentIntentData = MutableStateFlow<com.rfacto.shipping.data.api.PaymentIntentResponse?>(null)

    fun initiateStripePayment(parcelId: Int, amountCad: Double, onResult: (com.rfacto.shipping.data.api.PaymentIntentResponse?) -> Unit) {
        viewModelScope.launch {
            stripeLoading.value = true
            stripeError.value = null
            val client = _currentUser.value
            if (client == null) {
                stripeError.value = "Utilisateur non connecté."
                onResult(null)
                return@launch
            }
            try {
                val api = com.rfacto.shipping.data.api.RFactoApi.getInstance()
                val response = api.createPaymentIntent(
                    com.rfacto.shipping.data.api.CreatePaymentIntentRequest(
                        parcelId = parcelId,
                        amountCad = amountCad,
                        userId = client.id
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()
                    stripePaymentIntentData.value = data
                    onResult(data)
                } else {
                    stripeError.value = "Erreur backend Neon/Vercel (${response.code()}) : ${response.message()}"
                    onResult(null)
                }
            } catch (e: Exception) {
                stripeError.value = "Connexion impossible with Neon/Vercel : ${e.localizedMessage}"
                onResult(null)
            } finally {
                stripeLoading.value = false
            }
        }
    }
}