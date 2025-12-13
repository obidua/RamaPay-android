package com.alphawallet.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.alphawallet.app.R;
import com.alphawallet.app.service.AppSecurityManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity for unlocking the app with password or biometric
 */
@AndroidEntryPoint
public class AppLockActivity extends BaseActivity {
    
    public static final String EXTRA_FOR_TRANSACTION = "for_transaction";
    public static final int RESULT_AUTHENTICATED = 1001;
    public static final int RESULT_CANCELLED = 1002;
    
    @Inject
    AppSecurityManager securityManager;
    
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private MaterialButton btnUnlock;
    private MaterialButton btnBiometric;
    
    private boolean forTransaction = false;
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    public static Intent createIntent(Context context) {
        return new Intent(context, AppLockActivity.class);
    }
    
    public static Intent createIntentForTransaction(Context context) {
        Intent intent = new Intent(context, AppLockActivity.class);
        intent.putExtra(EXTRA_FOR_TRANSACTION, true);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        
        forTransaction = getIntent().getBooleanExtra(EXTRA_FOR_TRANSACTION, false);
        
        initViews();
        setupListeners();
        updateBiometricButton();
        
        // Auto-show biometric if enabled
        if (securityManager.isBiometricEnabled()) {
            btnBiometric.setVisibility(View.VISIBLE);
            // Auto-trigger biometric after a short delay
            btnBiometric.postDelayed(this::showBiometricPrompt, 300);
        } else {
            btnBiometric.setVisibility(View.GONE);
        }
    }
    
    private void updateBiometricButton() {
        if (btnBiometric == null) return;
        
        AppSecurityManager.BiometricType biometricType = securityManager.getBiometricType();
        switch (biometricType) {
            case FACE:
                btnBiometric.setIconResource(R.drawable.ic_face_id);
                btnBiometric.setText(R.string.use_face_id);
                break;
            case FINGERPRINT:
            case MULTIPLE:
            default:
                btnBiometric.setIconResource(R.drawable.ic_fingerprint);
                btnBiometric.setText(R.string.use_biometric);
                break;
        }
    }
    
    private void initViews() {
        passwordLayout = findViewById(R.id.password_layout);
        passwordInput = findViewById(R.id.password_input);
        btnUnlock = findViewById(R.id.btn_unlock);
        btnBiometric = findViewById(R.id.btn_biometric);
        
        // Update UI based on whether user is using PIN or password
        if (securityManager.isUsingPin()) {
            passwordLayout.setHint(getString(R.string.enter_pin));
            passwordInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                    android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            passwordLayout.setHint(getString(R.string.enter_password));
            passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | 
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
    }
    
    private void setupListeners() {
        btnUnlock.setOnClickListener(v -> verifyCredential());
        btnBiometric.setOnClickListener(v -> showBiometricPrompt());
        
        // Handle keyboard done action
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                verifyCredential();
                return true;
            }
            return false;
        });
    }
    
    private void verifyCredential() {
        String credential = passwordInput.getText() != null ? passwordInput.getText().toString() : "";
        
        if (credential.isEmpty()) {
            passwordLayout.setError(securityManager.isUsingPin() ? 
                    getString(R.string.enter_pin) : getString(R.string.enter_password));
            return;
        }
        
        boolean verified;
        if (securityManager.isUsingPin()) {
            verified = securityManager.verifyPin(credential);
        } else {
            verified = securityManager.verifyPassword(credential);
        }
        
        if (verified) {
            onAuthenticationSuccess();
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                passwordLayout.setError(getString(R.string.too_many_fails));
                btnUnlock.setEnabled(false);
                // Re-enable after 30 seconds
                btnUnlock.postDelayed(() -> {
                    btnUnlock.setEnabled(true);
                    failedAttempts = 0;
                }, 30000);
            } else {
                passwordLayout.setError(securityManager.isUsingPin() ? 
                        getString(R.string.incorrect_pin) : getString(R.string.incorrect_password));
            }
            passwordInput.setText("");
        }
    }
    
    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // User can still use password
                        if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                            errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            Toast.makeText(AppLockActivity.this, errString, Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        onAuthenticationSuccess();
                    }
                    
                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Biometric didn't match, user can retry or use password
                    }
                });
        
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.unlock_ramapay))
                .setSubtitle(getString(R.string.authenticate_to_continue))
                .setNegativeButtonText(getString(R.string.enter_password))
                .build();
        
        biometricPrompt.authenticate(promptInfo);
    }
    
    private void onAuthenticationSuccess() {
        securityManager.onAuthenticationSuccess();
        
        if (forTransaction) {
            setResult(RESULT_AUTHENTICATED);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (forTransaction) {
            setResult(RESULT_CANCELLED);
            finish();
        } else {
            // Don't allow back press on app lock screen
            // User must authenticate
            moveTaskToBack(true);
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent home and recent apps buttons from bypassing lock
        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
