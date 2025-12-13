package com.alphawallet.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import timber.log.Timber;

/**
 * Manages app-wide security settings including:
 * - Password-based authentication
 * - Biometric (fingerprint/face) authentication
 * - App lock functionality
 * - Security state management
 */
@Singleton
public class AppSecurityManager {
    
    private static final String PREFS_NAME = "ramapay_security_prefs";
    private static final String KEY_SECURITY_ENABLED = "security_enabled";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_PASSWORD_SALT = "password_salt";
    private static final String KEY_BIOMETRIC_ENABLED = "biometric_enabled";
    private static final String KEY_SECURITY_SETUP_SKIPPED = "security_setup_skipped";
    private static final String KEY_FIRST_WALLET_CREATED = "first_wallet_created";
    private static final String KEY_LAST_AUTH_TIME = "last_auth_time";
    
    // Authentication timeout - require re-auth after 5 minutes of inactivity
    private static final long AUTH_TIMEOUT_MS = 5 * 60 * 1000;
    
    private final Context context;
    private SharedPreferences securePrefs;
    private boolean isAuthenticated = false;
    private long lastAuthTime = 0;
    
    @Inject
    public AppSecurityManager(@ApplicationContext Context context) {
        this.context = context;
        initSecurePreferences();
    }
    
    private void initSecurePreferences() {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            
            securePrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Timber.e(e, "Failed to create secure preferences, falling back to regular prefs");
            securePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
    }
    
    /**
     * Check if security is enabled for the app
     */
    public boolean isSecurityEnabled() {
        return securePrefs.getBoolean(KEY_SECURITY_ENABLED, false);
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    public boolean isBiometricEnabled() {
        return securePrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);
    }
    
    /**
     * Check if the user skipped security setup during first wallet creation
     */
    public boolean isSecuritySetupSkipped() {
        return securePrefs.getBoolean(KEY_SECURITY_SETUP_SKIPPED, false);
    }
    
    /**
     * Check if this is the first wallet being created (no wallet has been created before)
     */
    public boolean isFirstWalletCreated() {
        return securePrefs.getBoolean(KEY_FIRST_WALLET_CREATED, false);
    }
    
    /**
     * Mark that the first wallet has been created
     */
    public void setFirstWalletCreated() {
        securePrefs.edit().putBoolean(KEY_FIRST_WALLET_CREATED, true).apply();
    }
    
    /**
     * Set security setup as skipped
     */
    public void setSecuritySetupSkipped(boolean skipped) {
        securePrefs.edit().putBoolean(KEY_SECURITY_SETUP_SKIPPED, skipped).apply();
    }
    
    /**
     * Check if biometric hardware is available on this device
     */
    public boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        );
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }
    
    /**
     * Check if device has biometric enrolled
     */
    public boolean hasBiometricEnrolled() {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        );
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }
    
    /**
     * Setup password-based security
     * @param password The user's chosen password
     * @param enableBiometric Whether to enable biometric authentication
     * @return true if setup was successful
     */
    public boolean setupSecurity(@NonNull String password, boolean enableBiometric) {
        try {
            // Generate salt
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            
            // Hash password with salt
            String passwordHash = hashPassword(password, salt);
            
            // Store in secure preferences
            securePrefs.edit()
                    .putString(KEY_PASSWORD_HASH, passwordHash)
                    .putString(KEY_PASSWORD_SALT, saltBase64)
                    .putBoolean(KEY_BIOMETRIC_ENABLED, enableBiometric && isBiometricAvailable())
                    .putBoolean(KEY_SECURITY_ENABLED, true)
                    .putBoolean(KEY_SECURITY_SETUP_SKIPPED, false)
                    .apply();
            
            isAuthenticated = true;
            lastAuthTime = System.currentTimeMillis();
            
            return true;
        } catch (Exception e) {
            Timber.e(e, "Failed to setup security");
            return false;
        }
    }
    
    /**
     * Verify password
     * @param password The password to verify
     * @return true if password matches
     */
    public boolean verifyPassword(@NonNull String password) {
        try {
            String storedHash = securePrefs.getString(KEY_PASSWORD_HASH, null);
            String saltBase64 = securePrefs.getString(KEY_PASSWORD_SALT, null);
            
            if (storedHash == null || saltBase64 == null) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(saltBase64);
            String inputHash = hashPassword(password, salt);
            
            boolean matches = storedHash.equals(inputHash);
            if (matches) {
                isAuthenticated = true;
                lastAuthTime = System.currentTimeMillis();
            }
            return matches;
        } catch (Exception e) {
            Timber.e(e, "Failed to verify password");
            return false;
        }
    }
    
    /**
     * Change password
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     * @return true if password was changed successfully
     */
    public boolean changePassword(@NonNull String currentPassword, @NonNull String newPassword) {
        if (!verifyPassword(currentPassword)) {
            return false;
        }
        
        try {
            // Generate new salt
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            
            // Hash new password
            String passwordHash = hashPassword(newPassword, salt);
            
            securePrefs.edit()
                    .putString(KEY_PASSWORD_HASH, passwordHash)
                    .putString(KEY_PASSWORD_SALT, saltBase64)
                    .apply();
            
            return true;
        } catch (Exception e) {
            Timber.e(e, "Failed to change password");
            return false;
        }
    }
    
    /**
     * Enable or disable biometric authentication
     */
    public void setBiometricEnabled(boolean enabled) {
        securePrefs.edit()
                .putBoolean(KEY_BIOMETRIC_ENABLED, enabled && isBiometricAvailable())
                .apply();
    }
    
    /**
     * Mark authentication as successful (called after biometric auth)
     */
    public void onAuthenticationSuccess() {
        isAuthenticated = true;
        lastAuthTime = System.currentTimeMillis();
    }
    
    /**
     * Check if user is currently authenticated and session is still valid
     */
    public boolean isSessionValid() {
        if (!isSecurityEnabled()) {
            return true;
        }
        
        if (!isAuthenticated) {
            return false;
        }
        
        // Check if auth has timed out
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAuthTime > AUTH_TIMEOUT_MS) {
            isAuthenticated = false;
            return false;
        }
        
        return true;
    }
    
    /**
     * Refresh the authentication session
     */
    public void refreshSession() {
        if (isAuthenticated) {
            lastAuthTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Lock the app (require re-authentication)
     */
    public void lockApp() {
        isAuthenticated = false;
        lastAuthTime = 0;
    }
    
    /**
     * Disable security entirely
     * @param password Current password for verification
     * @return true if security was disabled
     */
    public boolean disableSecurity(@NonNull String password) {
        if (!verifyPassword(password)) {
            return false;
        }
        
        securePrefs.edit()
                .remove(KEY_PASSWORD_HASH)
                .remove(KEY_PASSWORD_SALT)
                .putBoolean(KEY_BIOMETRIC_ENABLED, false)
                .putBoolean(KEY_SECURITY_ENABLED, false)
                .apply();
        
        isAuthenticated = false;
        return true;
    }
    
    /**
     * Check if authentication is required for app access
     */
    public boolean requiresAuthentication() {
        return isSecurityEnabled() && !isSessionValid();
    }
    
    /**
     * Hash password with salt using SHA-256
     */
    private String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        // Double hash for extra security
        hash = digest.digest(hash);
        return Base64.getEncoder().encodeToString(hash);
    }
}
