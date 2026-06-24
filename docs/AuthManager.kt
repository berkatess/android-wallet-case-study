package com.ba.walletcase.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Production-ready replacement for the reviewed `AuthManager`.
 *
 * Design notes (the "why" behind the rewrite):
 *
 * - **No plaintext storage.** The session token is encrypted with an AES/GCM key that lives
 *   in the Android Keystore and never leaves the secure hardware. The ciphertext is held in
 *   [EncryptedSharedPreferences], so even the at-rest blob is double-protected.
 * - **Biometric is cryptographically bound.** The Keystore key requires user authentication
 *   (`setUserAuthenticationRequired(true)`) and is only usable through a [BiometricPrompt]
 *   [BiometricPrompt.CryptoObject]. A `completion(true)` shortcut can no longer unlock
 *   anything — without a successful biometric the cipher cannot decrypt.
 * - **Key invalidated on new enrollment.** `setInvalidatedByBiometricEnrollment(true)` means a
 *   newly added fingerprint/face permanently invalidates the key, so a third party who enrolls
 *   their own biometric cannot reach the stored token.
 * - **No secret logging.** Nothing sensitive is ever passed to `Log`.
 * - **logout() really clears state.** It wipes both the in-memory copy and the encrypted disk
 *   entry, and deletes the Keystore key.
 * - **Injectable, not a hand-rolled singleton.** This is a plain class behind the [AuthManager]
 *   interface, so it is provided as a DI singleton with the application `Context` — no broken
 *   double-checked locking and no Activity-context leak.
 *
 * Scope note: the wallet PIN is intentionally NOT persisted here. A PIN is a verification
 * secret that belongs hashed on the server; the client should submit it for verification and
 * forget it. Only the resulting session token is stored. (See review items 5 and 6.)
 */
interface AuthManager {

    /** True if an encrypted session token exists on disk. Does not decrypt it. */
    fun hasStoredSession(): Boolean

    /**
     * Encrypts and persists [token] after a successful BIOMETRIC_STRONG authentication.
     * The biometric prompt is shown to authorize the encryption cipher.
     */
    fun saveSession(token: String, activity: FragmentActivity, callback: AuthCallback)

    /**
     * Decrypts and returns the stored token after a successful biometric authentication.
     * Fails with [AuthError.NoSession] if nothing is stored.
     */
    fun loadSession(activity: FragmentActivity, callback: TokenCallback)

    /** Clears the in-memory token, the encrypted disk entry, and the Keystore key. */
    fun logout()

    fun interface AuthCallback {
        fun onResult(success: Boolean, error: AuthError?)
    }

    fun interface TokenCallback {
        fun onResult(token: String?, error: AuthError?)
    }
}

/** Differentiated failure reasons, so callers can react correctly instead of seeing a bare false. */
sealed interface AuthError {
    /** Device has no enrolled BIOMETRIC_STRONG credential, or no biometric hardware. */
    data object BiometricUnavailable : AuthError
    /** User cancelled or dismissed the prompt. */
    data object UserCancelled : AuthError
    /** Too many failed attempts; the sensor is temporarily locked out. */
    data object LockedOut : AuthError
    /** No session token is stored. */
    data object NoSession : AuthError
    /** The Keystore key was invalidated (e.g. a new biometric was enrolled). */
    data object KeyInvalidated : AuthError
    /** Any other biometric or crypto failure. */
    data class Unknown(val message: String) : AuthError
}

class DefaultAuthManager(
    // Always the application context — never an Activity — so this singleton cannot leak a screen.
    context: Context,
) : AuthManager {

    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    // In-memory token is private and nulled on logout. Held only between unlock and use.
    private var cachedToken: String? = null

    override fun hasStoredSession(): Boolean = prefs.contains(KEY_CIPHERTEXT)

    override fun saveSession(token: String, activity: FragmentActivity, callback: AuthManager.AuthCallback) {
        when (val availability = biometricAvailability()) {
            null -> Unit // available, continue
            else -> {
                callback.onResult(false, availability)
                return
            }
        }

        val cipher = try {
            encryptionCipher()
        } catch (e: KeyPermanentlyInvalidatedException) {
            // A new biometric was enrolled; the old key is dead. Drop it and surface the reason.
            deleteKey()
            callback.onResult(false, AuthError.KeyInvalidated)
            return
        } catch (e: Exception) {
            callback.onResult(false, AuthError.Unknown(e.message ?: "cipher init failed"))
            return
        }

        showPrompt(
            activity = activity,
            title = "Confirm it's you",
            subtitle = "Authenticate to secure your wallet session",
            cipher = cipher,
            onSuccess = { authedCipher ->
                try {
                    val iv = authedCipher.iv
                    val ciphertext = authedCipher.doFinal(token.toByteArray(Charsets.UTF_8))
                    prefs.edit()
                        .putString(KEY_CIPHERTEXT, ciphertext.toBase64())
                        .putString(KEY_IV, iv.toBase64())
                        .apply() // async — never block the caller's thread
                    cachedToken = token
                    callback.onResult(true, null)
                } catch (e: Exception) {
                    callback.onResult(false, AuthError.Unknown(e.message ?: "encryption failed"))
                }
            },
            onError = { callback.onResult(false, it) },
        )
    }

    override fun loadSession(activity: FragmentActivity, callback: AuthManager.TokenCallback) {
        cachedToken?.let {
            callback.onResult(it, null)
            return
        }

        val ciphertext = prefs.getString(KEY_CIPHERTEXT, null)?.fromBase64()
        val iv = prefs.getString(KEY_IV, null)?.fromBase64()
        if (ciphertext == null || iv == null) {
            callback.onResult(null, AuthError.NoSession)
            return
        }

        when (val availability = biometricAvailability()) {
            null -> Unit
            else -> {
                callback.onResult(null, availability)
                return
            }
        }

        val cipher = try {
            decryptionCipher(iv)
        } catch (e: KeyPermanentlyInvalidatedException) {
            // Key invalidated by new enrollment: the stored token is unrecoverable. Clear it.
            logout()
            callback.onResult(null, AuthError.KeyInvalidated)
            return
        } catch (e: Exception) {
            callback.onResult(null, AuthError.Unknown(e.message ?: "cipher init failed"))
            return
        }

        showPrompt(
            activity = activity,
            title = "Unlock your wallet",
            subtitle = "Authenticate to access your account",
            cipher = cipher,
            onSuccess = { authedCipher ->
                try {
                    val plain = authedCipher.doFinal(ciphertext)
                    val token = String(plain, Charsets.UTF_8)
                    cachedToken = token
                    callback.onResult(token, null)
                } catch (e: Exception) {
                    callback.onResult(null, AuthError.Unknown(e.message ?: "decryption failed"))
                }
            },
            onError = { callback.onResult(null, it) },
        )
    }

    override fun logout() {
        cachedToken = null
        prefs.edit()
            .remove(KEY_CIPHERTEXT)
            .remove(KEY_IV)
            .apply()
        deleteKey()
    }

    // --- BiometricPrompt ---

    private fun showPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        cipher: Cipher,
        onSuccess: (Cipher) -> Unit,
        onError: (AuthError) -> Unit,
    ) {
        // Reuse the main executor instead of spinning up (and leaking) a new thread per call.
        val executor = ContextCompat.getMainExecutor(appContext)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val authedCipher = result.cryptoObject?.cipher
                    if (authedCipher == null) {
                        onError(AuthError.Unknown("missing authenticated cipher"))
                    } else {
                        onSuccess(authedCipher)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode.toAuthError(errString))
                }

                override fun onAuthenticationFailed() {
                    // A single non-matching fingerprint/face: the prompt stays open for a retry,
                    // so we deliberately do not terminate the flow here.
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Cancel")
            // BIOMETRIC_STRONG (Class 3) is required to back a CryptoObject. DEVICE_CREDENTIAL
            // is intentionally omitted: a CryptoObject-bound key needs a biometric, not a PIN/pattern.
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()

        prompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }

    private fun biometricAvailability(): AuthError? {
        val status = BiometricManager.from(appContext).canAuthenticate(BIOMETRIC_STRONG)
        return when (status) {
            BiometricManager.BIOMETRIC_SUCCESS -> null
            else -> AuthError.BiometricUnavailable
        }
    }

    // --- Keystore ---

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE,
        )
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            // Every use must be authorized by a biometric (via the CryptoObject).
            .setUserAuthenticationRequired(true)
            // A newly enrolled biometric permanently kills this key.
            .setInvalidatedByBiometricEnrollment(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+: require a STRONG biometric for every single use (timeout 0).
            builder.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
        } else {
            // API 24–29: -1 means "authenticate on every use" (the CryptoObject path).
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }

    private fun encryptionCipher(): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }

    private fun decryptionCipher(iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        }

    private fun deleteKey() {
        try {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }.deleteEntry(KEY_ALIAS)
        } catch (_: Exception) {
            // Already absent — nothing to clean up.
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "wallet_session_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128

        private const val PREFS_NAME = "secure_auth_prefs"
        private const val KEY_CIPHERTEXT = "session_token_ct"
        private const val KEY_IV = "session_token_iv"
    }
}

// --- small helpers ---

private fun Int.toAuthError(errString: CharSequence): AuthError = when (this) {
    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
    BiometricPrompt.ERROR_USER_CANCELED,
    BiometricPrompt.ERROR_CANCELED,
    -> AuthError.UserCancelled

    BiometricPrompt.ERROR_LOCKOUT,
    BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
    -> AuthError.LockedOut

    BiometricPrompt.ERROR_NO_BIOMETRICS,
    BiometricPrompt.ERROR_HW_NOT_PRESENT,
    BiometricPrompt.ERROR_HW_UNAVAILABLE,
    -> AuthError.BiometricUnavailable

    else -> AuthError.Unknown(errString.toString())
}

private fun ByteArray.toBase64(): String =
    android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

private fun String.fromBase64(): ByteArray =
    android.util.Base64.decode(this, android.util.Base64.NO_WRAP)
