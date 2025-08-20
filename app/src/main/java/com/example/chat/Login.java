package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    TextInputEditText editmail, editPassword;
    Button L_button;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView txt1;

    @Override
    public void onStart() {
        super.onStart();

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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);


        editmail = findViewById(R.id.login_email);
        editPassword = findViewById(R.id.login_password);
        L_button = findViewById(R.id.login_button);
        txt1 = findViewById(R.id.signupRedirectText);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Redirect to Signup activity
        txt1.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent1);
            finish();
        });

        // Login button click listener
        L_button.setOnClickListener(v -> {
            String email = String.valueOf(editmail.getText()).trim();
            String pass = String.valueOf(editPassword.getText()).trim();

            // Validation
            if (email.isEmpty()) {
                Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.isEmpty()) {
                Toast.makeText(Login.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase sign-in
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intnn = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intnn);
                            finish();
                        } else {

                            Toast.makeText(Login.this, "Invalid email or password. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}
