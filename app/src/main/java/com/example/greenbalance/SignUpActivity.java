package com.example.greenbalance;

import static com.example.greenbalance.utility.User.createUserAccount;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;

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

    Button signUpButton;
    ImageButton backButton;
    ImageView eyeIcon, eyeIcon2;
    EditText username, email, password, confirmPassword;
    TextView loginTextView;
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
        backButton = findViewById(R.id.backBtn);
        loginTextView = findViewById(R.id.loginTextView);
        username = findViewById(R.id.usernameEditText);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        confirmPassword = findViewById(R.id.confirmPasswordEditText);
        eyeIcon = findViewById(R.id.eyeIcon);
        eyeIcon2 = findViewById(R.id.eyeIcon2);
        loginTextView.setClickable(true);

        eyeIcon.setOnClickListener(v -> togglePasswordVisibility(password, eyeIcon));
        eyeIcon2.setOnClickListener(v -> togglePasswordVisibility(confirmPassword, eyeIcon2));

        backButton.setOnClickListener(
                v->{
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
        );
        loginTextView.setOnClickListener(
                v->{
                    loginTextView.setTextColor(getResources().getColor(R.color.button_color_pressed));

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

    private void togglePasswordVisibility(EditText passwordField, ImageView toggleButton) {
        if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
            passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.visibility_24dp_e8eaed_fill0_wght400_grad0_opsz24);
        } else {
            passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleButton.setImageResource(R.drawable.visibility_off_24dp_e8eaed_fill0_wght400_grad0_opsz24);
        }

        passwordField.setSelection(passwordField.getText().length());
    }
}
