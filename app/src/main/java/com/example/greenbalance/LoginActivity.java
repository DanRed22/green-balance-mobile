package com.example.greenbalance;

import static com.example.greenbalance.utility.User.loginUser;
import static com.example.greenbalance.utility.Prefs.saveLoginData;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.greenbalance.utility.Prefs;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LoginActivity extends AppCompatActivity {

    Button loginButton, signUpButton;
    EditText email, password;
    FirebaseAuth firebaseAuth;
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
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setClickable(true);
        loginButton.setClickable(true);

        signUpButton.setOnClickListener(v->{
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(intent);
            finish();
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

}