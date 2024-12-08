package com.example.greenbalance;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.greenbalance.properties.Configuration;
import com.example.greenbalance.utility.Prefs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private EditText usernameEditText, newPasswordEditText;
    private TextView emailTextView;
    private Button saveButton, logoutButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        emailTextView = findViewById(R.id.emailTextView);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Load current user data
        loadUserData();

        // Set up click listeners
        saveButton.setOnClickListener(v -> saveChanges());
        logoutButton.setOnClickListener(v -> logout());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");

        usernameEditText.setText(username);
        emailTextView.setText(email);
    }

    private void saveChanges() {
        String newUsername = usernameEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (newUsername.isEmpty()) {
            usernameEditText.setError("Username cannot be empty");
            return;
        }

        // Update username in backend
        updateUsername(newUsername);

        // Update password if provided
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 8) {
                newPasswordEditText.setError("Password must be at least 8 characters");
                return;
            }
            updatePassword(newPassword);
        }
        showSuccessDialog();

        Prefs.setUsername(newUsername);
    }

    private void updateUsername(String newUsername) {
        String url = Configuration.API_URL + "/v1/users/" + Prefs.getKeyId();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", newUsername);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT, url, jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            // Update local storage
                            Prefs.init(this);
                            SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
                            editor.putString("username", newUsername);
                            editor.apply();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error updating username", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("x-api-key", Configuration.API_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updatePassword(String newPassword) {
        if(newPassword.length() < 8) {
            newPasswordEditText.setError("Password must be at least 8 characters");
            return;
        }
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                            newPasswordEditText.setText("");
                        } else {
                            Toast.makeText(this, "Failed to update password: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void logout() {
        // Sign out from Firebase
        firebaseAuth.signOut();
        
        // Clear local storage
        Prefs.init(this);
        Prefs.clearLoginData();
        
        // Redirect to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_changes_saved, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button okButton = dialogView.findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> {
            dialog.dismiss();
            // Redirect to dashboard
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }
}