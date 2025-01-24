package com.example.geotendy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class PinActivity extends AppCompatActivity {
    TextView PinDisplay;
    StringBuilder enteredPin = new StringBuilder(); // Store the PIN here
    String savedPin = null;  // Store the first entered PIN for confirmation
    boolean isConfirmingPin = false;  // Flag to track if we're confirming the PIN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pin);

        PinDisplay = findViewById(R.id.textViewPin);

        // Check if the user is already authenticated
        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isAuthenticated = preferences.getBoolean("isAuthenticated", false);

        if (isAuthenticated) {
            // Skip authentication flow and go straight to the dashboard
            proceedToNextScreen();
            return;
        }

        // Proceed with PIN or fingerprint authentication
        Toast.makeText(this, "Please authenticate to continue", Toast.LENGTH_SHORT).show();

        // Initialize fingerprint logic
        initializeFingerprintAuthentication();
        initializePinButtons();
    }

    private void initializeFingerprintAuthentication() {
        ImageView fingerprintIcon = findViewById(R.id.imageViewFingerprint);
        fingerprintIcon.setOnClickListener(v -> checkFingerprintSupportAndAuthenticate());
    }


    private void initializePinButtons() {
        Button buttonOne = findViewById(R.id.buttonOne);
        Button buttonTwo = findViewById(R.id.buttonTwo);
        Button buttonThree = findViewById(R.id.buttonThree);
        Button buttonFour = findViewById(R.id.buttonfour);
        Button buttonFive = findViewById(R.id.buttonFive);
        Button buttonSix = findViewById(R.id.buttonSix);
        Button buttonSeven = findViewById(R.id.buttonSeven);
        Button buttonEight = findViewById(R.id.buttoneEight);
        Button buttonNine = findViewById(R.id.buttonNine);
        Button buttonZero = findViewById(R.id.buttonZero);
        Button buttonDelete = findViewById(R.id.buttonDelete);

        buttonOne.setOnClickListener(v -> appendDigit("1"));
        buttonTwo.setOnClickListener(v -> appendDigit("2"));
        buttonThree.setOnClickListener(v -> appendDigit("3"));
        buttonFour.setOnClickListener(v -> appendDigit("4"));
        buttonFive.setOnClickListener(v -> appendDigit("5"));
        buttonSix.setOnClickListener(v -> appendDigit("6"));
        buttonSeven.setOnClickListener(v -> appendDigit("7"));
        buttonEight.setOnClickListener(v -> appendDigit("8"));
        buttonNine.setOnClickListener(v -> appendDigit("9"));
        buttonZero.setOnClickListener(v -> appendDigit("0"));
        buttonDelete.setOnClickListener(v -> deleteLastDigit());
    }

    private void appendDigit(String digit) {
        if (enteredPin.length() < 4) {
            enteredPin.append(digit);
            updatePinDisplay();
        }
        if (enteredPin.length() == 4) {
            handleFullPinEntry();
        }
    }

    private void deleteLastDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        PinDisplay.setText(enteredPin.toString());  // Show the digits
        new Handler().postDelayed(() -> {
            StringBuilder maskedPin = new StringBuilder();
            for (int i = 0; i < enteredPin.length(); i++) {
                maskedPin.append("*");
            }
            PinDisplay.setText(maskedPin.toString());
        }, 500);  // Mask after 500ms
    }

    private void handleFullPinEntry() {
        if (!isConfirmingPin && savedPin == null) {
            savedPin = enteredPin.toString();
            enteredPin.setLength(0);
            PinDisplay.setText("");
            isConfirmingPin = true;
            Toast.makeText(this, "Please confirm your PIN", Toast.LENGTH_SHORT).show();
        } else if (isConfirmingPin) {
            String confirmPin = enteredPin.toString();
            if (savedPin.equals(confirmPin)) {
                savePin(savedPin);
                Toast.makeText(this, "PIN successfully set!", Toast.LENGTH_SHORT).show();
                proceedToNextScreen();
            } else {
                Toast.makeText(this, "PINs do not match. Please try again.", Toast.LENGTH_SHORT).show();
                resetPinSetup();
            }
        } else {
            if (enteredPin.toString().equals(savedPin)) {
                Toast.makeText(this, "PIN correct!", Toast.LENGTH_SHORT).show();
                proceedToNextScreen();
            } else {
                Toast.makeText(this, "Incorrect PIN. Try again.", Toast.LENGTH_SHORT).show();
                enteredPin.setLength(0);
                PinDisplay.setText("");
            }
        }
    }

    private void resetPinSetup() {
        enteredPin.setLength(0);
        savedPin = null;
        isConfirmingPin = false;
        PinDisplay.setText("");
    }

    private void savePin(String pin) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedPin", pin);  // No hashing for simplicity here
        editor.apply();
    }

    private void checkFingerprintSupportAndAuthenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPrompt();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No biometric hardware available", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric hardware is unavailable", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No fingerprints enrolled. Please set up fingerprint authentication", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Biometric authentication is unsupported", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(PinActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                // Save authentication state in SharedPreferences
                SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isAuthenticated", true); // Mark user as authenticated
                editor.apply();

                // Notify the user and proceed
                Toast.makeText(PinActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                proceedToNextScreen();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(PinActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });

        // Create a biometric prompt dialog
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Use your fingerprint to authenticate")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void proceedToNextScreen() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String role = sharedPreferences.getString("role", "student");  // Default to "student" if role is missing

        if ("staff".equals(role)) {
            // Direct to Staff Dashboard
            startActivity(new Intent(PinActivity.this, StaffActivity.class));
        } else {
            // Direct to Student Dashboard
            startActivity(new Intent(PinActivity.this, StudentDashboard.class));
        }

        finish();  // Close the current activity
    }
//    private void logoutUser() {
//        SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();  // Clear all session data
//        editor.apply();
//
//        // Redirect to login or authentication screen
//        startActivity(new Intent(this, LoginActivity.class));
//        finish();
//    }
}
