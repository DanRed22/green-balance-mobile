package com.example.greenbalance;

import static com.example.greenbalance.utility.User.loginUser;
import static com.example.greenbalance.utility.Prefs.saveLoginData;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.greenbalance.utility.ApiResponse;
import com.example.greenbalance.utility.Prefs;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LoginActivity extends AppCompatActivity {

    Button loginButton;
    TextView signUpTextView;
    EditText email, password;
    FirebaseAuth firebaseAuth;
    ImageView eyeIcon;
    private static final String TAG = "LoginActivity";
    private String uid, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuth.getInstance().getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        email = findViewById(R.id.emailEditText);
        password = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        signUpTextView.setClickable(true);
        loginButton.setClickable(true);
        eyeIcon = findViewById(R.id.eyeIcon);

        eyeIcon.setOnClickListener(v -> togglePasswordVisibility(password, eyeIcon));

        signUpTextView.setOnClickListener(v -> {
            signUpTextView.setTextColor(getResources().getColor(R.color.button_color_pressed));

            Intent signUpIntent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(signUpIntent);

            signUpTextView.postDelayed(() ->
                            signUpTextView.setTextColor(getResources().getColor(R.color.dialog_button_color)),
                    200
            );
        });

        loginButton.setOnClickListener(v -> {
            if (validateInputs()) {
                firebaseAuth.signInWithEmailAndPassword(
                        email.getText().toString().trim(),
                        password.getText().toString().trim()
                ).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully signed in
                        AuthResult authResult = task.getResult();
                        assert authResult != null;
                        this.uid = Objects.requireNonNull(authResult.getUser()).getUid();
                        this.userEmail = authResult.getUser().getEmail();

                        // Make API call
                        CompletableFuture<ApiResponse> apiResponse = loginUser(getApplicationContext(), userEmail);
                        apiResponse.thenAccept(response -> {
                            try {
                                if (response.isSuccessful()) {
                                    JSONObject body = response.getBody();

                                    String username = body.getString("username");
                                    Prefs.init(this);
                                    Prefs.saveLoginData(body);
                                    Log.d("[Login Activity] Logged in with data", body.toString());
                                    Toast.makeText(LoginActivity.this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Login Failed! ", Toast.LENGTH_SHORT).show();
                                }
                            }
                            catch(Exception e){
                                Log.e("[Login Activity]", "Error: " + e.getMessage());
                            }
                        });
                    } else {
                        // Login failed
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private boolean validateInputs() {
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

        return true;
    }

    /* Saves login data so user wont have to keep relogging-in
     * Parameters would be the whole data token returned by the user login.
     * */

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
