package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    Button btnSingIn, btnLogIN;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users ;

    RelativeLayout root ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSingIn = findViewById(R.id.btnSingin);
        btnLogIN = findViewById(R.id.btnRegister);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        root = findViewById(R.id.root_element);

        btnLogIN.setOnClickListener(v -> showRegisterWindow());
        btnSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogInWindow();
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");


        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            auth.signInWithEmailAndPassword(savedEmail, savedPassword)
                    .addOnSuccessListener(authResult -> {
                        Intent myIntent = new Intent(MainActivity.this, SwapActivity.class);
                        startActivity(myIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(root, "Error. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    });
        } else {

            showLogInWindow();
        }

        btnLogIN.setOnClickListener(v -> showRegisterWindow());
        btnSingIn.setOnClickListener(v -> showLogInWindow());
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null && user.isEmailVerified()) {
                Intent intent = new Intent(MainActivity.this, SwapActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }


    private void showLogInWindow() {

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("email", "");
        String savedPassword = sharedPreferences.getString("password", "");

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            Intent myIntent = new Intent(MainActivity.this, SwapActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Log in");
        dialog.setMessage("Enter data for log in");

        LayoutInflater infater = LayoutInflater.from(this);
        View loginwindows = infater.inflate(R.layout.sing_in_window, null);
        dialog.setView(loginwindows);

        final EditText email = loginwindows.findViewById(R.id.emailField);
        final EditText password = loginwindows.findViewById(R.id.passField);
        CheckBox savePasswordCheckbox = loginwindows.findViewById(R.id.save_password_checkbox);

        dialog.setNegativeButton("Back", (dialogInterface, which) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Log In", (dialogInterface, which) -> {
            if (TextUtils.isEmpty(email.getText().toString())) {
                Snackbar.make(root, "Enter your email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (password.getText().toString().length() < 8) {
                Snackbar.make(root, "Enter password more than 8 characters", Snackbar.LENGTH_SHORT).show();
                return;
            }

            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnSuccessListener(authResult -> {

                        if (savePasswordCheckbox.isChecked()) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", email.getText().toString());
                            editor.putString("password", password.getText().toString());
                            editor.apply();
                        } else {
                            // Удаляем сохраненный пароль, если есть
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("password");
                            editor.apply();
                        }
                        Intent myIntent = new Intent(MainActivity.this, SwapActivity.class);
                        startActivity(myIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(root, "Error. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }


    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Enter all data for registration");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.windows1, null);
        dialog.setView(registerWindow);

        final EditText email = registerWindow.findViewById(R.id.emailField);
        final EditText password = registerWindow.findViewById(R.id.passField);
        final EditText name = registerWindow.findViewById(R.id.nameField);
        final EditText phone = registerWindow.findViewById(R.id.phoneField);

        dialog.setNegativeButton("Back", (dialogInterface, which) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Register", (dialogInterface, which) -> {
            if(TextUtils.isEmpty(email.getText().toString())){
                Snackbar.make(root, "Enter your email", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(name.getText().toString())){
                Snackbar.make(root, "Enter your name", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if(TextUtils.isEmpty(phone.getText().toString())){
                Snackbar.make(root, "Enter your phone number", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if(password.getText().toString().length()< 8 ){
                Snackbar.make(root,"Enter password more than 8 characters", Snackbar.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnSuccessListener(authResult -> {
                        User user = new User();
                        user.setEmail(email.getText().toString());
                        user.setPassword(password.getText().toString());
                        user.setName(name.getText().toString());
                        user.setPhone(phone.getText().toString());
                        user.setVerified(false);

                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        firebaseUser.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Snackbar.make(root, "A verification email has been sent to your email address. Please check your inbox.", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(root, "Failed to send verification email. " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                                    }
                                });

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    Intent intent = new Intent(MainActivity.this, SwapActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(root, "Registration failed. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(root, "Registration failed. " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }


}