package com.example.greenbalance;

import static com.example.greenbalance.utility.User.createUserAccount;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.greenbalance.utility.ApiResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.CompletableFuture;


public class SignUpActivity extends AppCompatActivity {

    Button signUpButton, loginButton;
    EditText username, email, password, confirmPassword;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        signUpButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.loginButton);
        username = findViewById(R.id.usernameEditText);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        confirmPassword = findViewById(R.id.confirmPasswordEditText);

        loginButton.setOnClickListener(
                v->{
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
        );
        signUpButton.setOnClickListener(
                v -> {
                    if (validateInputs()) {
                        firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                        if (firebaseUser != null) {
                                            String userId = firebaseUser.getUid();

                                            // Make the API call
                                            CompletableFuture<ApiResponse> response = createUserAccount(getApplicationContext(), userId, username.getText().toString(), email.getText().toString());

                                            // Handle the API response when it's complete
                                            response.thenAccept(apiResponse -> {
                                                if (apiResponse.isSuccessful() && !apiResponse.isError()) {
                                                    Log.d("API Response", "Response: " + apiResponse.getBody().toString());
                                                    Toast.makeText(this, "Account Created Successfully, Please Sign in!", Toast.LENGTH_SHORT).show();
                                                    Intent intent_dashboard = new Intent(this, LoginActivity.class);
                                                    startActivity(intent_dashboard);
                                                    finish();
                                                } else {
                                                    // Handle the error response from the API
                                                    Log.d("API Error", "Error: " + apiResponse.getBody().toString());
                                                    Toast.makeText(this, "Error: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }).exceptionally(ex -> {
                                                // Handle any errors in the asynchronous execution
                                                Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                                return null;
                                            });
                                        }
                                    } else {
                                        Toast.makeText(this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private boolean validateInputs() {
        if (username.getText().toString().isEmpty()) {
            username.setError("Username cannot be empty");
            return false;
        }

        if (email.getText().toString().isEmpty() || !email.getText().toString().contains("@") || !email.getText().toString().contains(".") || email.getText().toString().length() < 5 || email.getText().toString().length() > 50) {
            email.setError("Invalid Email");
            return false;
        }

        if (password.getText().toString().isEmpty()) {
            password.setError("Password cannot be empty");
            return false;
        }

        if (password.getText().toString().length() < 8) {
            password.setError("Password must be at least 8 characters long");
            return false;
        }

        if (confirmPassword.getText().toString().isEmpty()) {
            confirmPassword.setError("Confirm Password cannot be empty");
            return false;
        }

        String pass = password.getText().toString();
        String confirmPass = confirmPassword.getText().toString();

        if (!pass.equals(confirmPass)) {
            password.setError("Passwords do not match");
            confirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }
}
