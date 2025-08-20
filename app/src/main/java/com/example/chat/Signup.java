package com.example.chat;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class Signup extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button button;
    TextInputLayout emailInputLayout, passwordInputLayout;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView txt;

    private static final String TAG = "Signup"; // TAG for logging

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intnn = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intnn);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        editTextEmail = findViewById(R.id.sign_email);
        editTextPassword = findViewById(R.id.sign_password);
        button = findViewById(R.id.sign_button);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        txt = findViewById(R.id.loginRedirectText);

        // Apply insets for edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Redirect to Login activity
        txt.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        button.setOnClickListener(v -> {
            String email = String.valueOf(editTextEmail.getText()).trim();
            String pass = String.valueOf(editTextPassword.getText()).trim();

            // Validation for empty fields
            if (email.isEmpty()) {
                Toast.makeText(Signup.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.isEmpty()) {
                Toast.makeText(Signup.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the user already exists
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean userExists = !task.getResult().getSignInMethods().isEmpty();

                            if (userExists) {
                                // User already exists
                                Toast.makeText(Signup.this, "User already exists. Please log in.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Create a new user
                                mAuth.createUserWithEmailAndPassword(email, pass)
                                        .addOnCompleteListener(createTask -> {
                                            if (createTask.isSuccessful()) {
                                                // Account creation successful
                                                Toast.makeText(Signup.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                updateUI(user);
                                            } else {
                                                // Account creation failed
                                                String errorMessage = createTask.getException() != null ? createTask.getException().getMessage() : "Unknown error occurred";
                                                Toast.makeText(Signup.this, "Account creation failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // Error during user existence check
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred";
                            Toast.makeText(Signup.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });


    }

    // Method to update the UI based on the current user
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Handle user UI updates here
            Toast.makeText(Signup.this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
        } else {
            // Handle failed signup or no user scenario
            Toast.makeText(Signup.this, "Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}


