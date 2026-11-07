package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class AuthProviderType {
    GOOGLE,
    PHONE,
    APPLE,
    EMAIL,
    GUEST
}

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val provider: AuthProviderType = AuthProviderType.GUEST,
    val isGuest: Boolean = false
)

class AuthManager(private val context: Context, private val scope: CoroutineScope) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("teleprompt_auth_prefs", Context.MODE_PRIVATE)

    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        // Listen to Firebase Auth state
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val profile = mapFirebaseUser(firebaseUser)
                _currentUser.value = profile
                persistUserLocally(profile)
                syncUserToFirestore(profile)
            } else {
                // Check if user was previously signed in as Guest or custom provider
                val isGuest = prefs.getBoolean("is_guest_user", false)
                if (isGuest) {
                    val guestName = prefs.getString("guest_name", "Creator Guest") ?: "Creator Guest"
                    val guestUid = prefs.getString("guest_uid", "guest_${System.currentTimeMillis()}") ?: "guest_user"
                    _currentUser.value = UserProfile(
                        uid = guestUid,
                        displayName = guestName,
                        provider = AuthProviderType.GUEST,
                        isGuest = true
                    )
                } else {
                    val savedUid = prefs.getString("saved_uid", null)
                    if (savedUid != null) {
                        _currentUser.value = UserProfile(
                            uid = savedUid,
                            displayName = prefs.getString("saved_name", "User") ?: "User",
                            email = prefs.getString("saved_email", "") ?: "",
                            phoneNumber = prefs.getString("saved_phone", "") ?: "",
                            provider = try {
                                AuthProviderType.valueOf(prefs.getString("saved_provider", "EMAIL") ?: "EMAIL")
                            } catch (_: Exception) {
                                AuthProviderType.EMAIL
                            }
                        )
                    } else {
                        _currentUser.value = null
                    }
                }
            }
        }
    }

    fun clearError() {
        _authError.value = null
    }

    private fun mapFirebaseUser(user: FirebaseUser): UserProfile {
        return UserProfile(
            uid = user.uid,
            displayName = user.displayName?.ifBlank { "Creator" } ?: "Creator",
            email = user.email ?: "",
            phoneNumber = user.phoneNumber ?: "",
            photoUrl = user.photoUrl?.toString() ?: "",
            provider = AuthProviderType.GOOGLE,
            isGuest = false
        )
    }

    private fun persistUserLocally(profile: UserProfile) {
        prefs.edit()
            .putString("saved_uid", profile.uid)
            .putString("saved_name", profile.displayName)
            .putString("saved_email", profile.email)
            .putString("saved_phone", profile.phoneNumber)
            .putString("saved_provider", profile.provider.name)
            .putBoolean("is_guest_user", profile.isGuest)
            .apply()
    }

    private fun syncUserToFirestore(profile: UserProfile) {
        scope.launch(Dispatchers.IO) {
            try {
                val dbId = context.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                val userRef = db.collection("users").document(profile.uid)
                val data = mapOf(
                    "displayName" to profile.displayName,
                    "email" to profile.email,
                    "phoneNumber" to profile.phoneNumber,
                    "photoUrl" to profile.photoUrl,
                    "provider" to profile.provider.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(data, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to sync user profile to Firestore: ${e.message}")
            }
        }
    }

    // 1. Google Sign-In via Credential Manager
    fun signInWithGoogle(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            val msg = "Google Client ID configuration missing"
            _authError.value = msg
            onError(msg)
            return
        }

        _isLoading.value = true
        _authError.value = null

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val profile = mapFirebaseUser(user)
                        _currentUser.value = profile
                        persistUserLocally(profile)
                        syncUserToFirestore(profile)
                        _isLoading.value = false
                        onSuccess()
                    } else {
                        _isLoading.value = false
                        onError("Google user profile not found")
                    }
                } else {
                    _isLoading.value = false
                    onError("Unexpected credential format received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthManager", "Google Sign-In cancelled: ${e.message}")
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("AuthManager", "Google Sign-In error", e)
                _isLoading.value = false
                val msg = e.localizedMessage ?: "Google Sign-In failed"
                _authError.value = msg
                onError(msg)
            }
        }
    }

    // Silent Auto Sign-In on App Startup
    fun attemptAutoSignIn() {
        if (auth.currentUser != null) {
            _currentUser.value = mapFirebaseUser(auth.currentUser!!)
            return
        }
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                }
            } catch (_: Exception) {
                // Silent check, ignore on failure
            }
        }
    }

    // 2. Phone Number Authentication (Direct phone login + SMS verification simulation)
    fun signInWithPhone(phoneNumber: String, smsCode: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (phoneNumber.length < 8) {
            val msg = "Please enter a valid phone number with country code"
            _authError.value = msg
            onError(msg)
            return
        }
        if (smsCode.length != 6) {
            val msg = "Please enter the 6-digit verification code"
            _authError.value = msg
            onError(msg)
            return
        }

        _isLoading.value = true
        _authError.value = null

        scope.launch {
            kotlinx.coroutines.delay(1200L) // Simulate network auth handshake
            val uid = "phone_" + phoneNumber.replace("[^0-9]".toRegex(), "")
            val profile = UserProfile(
                uid = uid,
                displayName = "Phone User (${phoneNumber.takeLast(4)})",
                phoneNumber = phoneNumber,
                provider = AuthProviderType.PHONE,
                isGuest = false
            )
            _currentUser.value = profile
            persistUserLocally(profile)
            syncUserToFirestore(profile)
            _isLoading.value = false
            onSuccess()
        }
    }

    // 3. Apple ID Sign-In
    fun signInWithApple(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _isLoading.value = true
        _authError.value = null

        scope.launch {
            kotlinx.coroutines.delay(1000L) // Simulate Apple ID token verification
            val appleUid = "apple_${System.currentTimeMillis() % 100000}"
            val profile = UserProfile(
                uid = appleUid,
                displayName = "Apple Creator",
                email = "user_${appleUid.takeLast(4)}@privaterelay.appleid.com",
                provider = AuthProviderType.APPLE,
                isGuest = false
            )
            _currentUser.value = profile
            persistUserLocally(profile)
            syncUserToFirestore(profile)
            _isLoading.value = false
            onSuccess()
        }
    }

    // 4. Email & Password Sign-In / Sign-Up
    fun signInWithEmail(email: String, isSignUp: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!email.contains("@") || !email.contains(".")) {
            val msg = "Please enter a valid email address"
            _authError.value = msg
            onError(msg)
            return
        }

        _isLoading.value = true
        _authError.value = null

        scope.launch {
            kotlinx.coroutines.delay(1000L)
            val username = email.substringBefore("@").replace(".", " ").capitalize()
            val uid = "email_" + email.hashCode()
            val profile = UserProfile(
                uid = uid,
                displayName = username,
                email = email,
                provider = AuthProviderType.EMAIL,
                isGuest = false
            )
            _currentUser.value = profile
            persistUserLocally(profile)
            syncUserToFirestore(profile)
            _isLoading.value = false
            onSuccess()
        }
    }

    // 5. Guest / Skip Exploration Mode
    fun continueAsGuest(onSuccess: () -> Unit) {
        val guestUid = "guest_" + System.currentTimeMillis().toString().takeLast(6)
        val profile = UserProfile(
            uid = guestUid,
            displayName = "Guest Creator",
            provider = AuthProviderType.GUEST,
            isGuest = true
        )
        _currentUser.value = profile
        persistUserLocally(profile)
        onSuccess()
    }

    // Sign Out
    fun signOut(onComplete: () -> Unit = {}) {
        auth.signOut()
        prefs.edit().clear().apply()
        _currentUser.value = null
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthManager", "Failed to clear credential state", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}
