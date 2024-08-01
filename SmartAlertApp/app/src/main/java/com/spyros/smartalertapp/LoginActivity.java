package com.spyros.smartalertapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    Button login;
    Button register;
    FirebaseAuth mAuth;
    EditText loginpassword;
    EditText loginemail;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
    CollectionReference users = db.collection("User");
    String lang;
    TextView textView , textView3 , textView4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.login);
        register = findViewById(R.id.register);
        loginemail = findViewById(R.id.loginemail);
        loginpassword = findViewById(R.id.loginpassword);
        mAuth = FirebaseAuth.getInstance();
        textView = findViewById(R.id.textView);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        SharedPreferences sp = getApplicationContext().getSharedPreferences("sharedPrefs" , Context.MODE_PRIVATE);

        lang = sp.getString("lan" , "Greek");
        changeLang();
    }

    public void changeLang(){
        if (lang.equals("English")){
            textView.setText("Login");
            textView3.setText("Password");
            textView4.setText("Dont have an account? Click here to register");
            login.setText("Login");
            register.setText("Register");
        }
    }

    public void login (View view){
        String email = loginemail.getText().toString();
        String password = loginpassword.getText().toString();


        if (!email.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Update(view);
                            }
                            else {
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Η σύνδεση απέτυχε παρακαλώ προσπαθήστε ξανά",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(this, "Παρακαλώ συμπληρώστε όλα τα πεδία" , Toast.LENGTH_SHORT).show();
        }


    }
    public void register (View view){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
    public void Update(View view){
        String userId = mAuth.getUid();
        FirebaseUser user = mAuth.getCurrentUser();
        DocumentReference userRef = FirebaseFirestore.getInstance()
                .collection("User")
                .document(userId);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String userRole = documentSnapshot.getString("role");
                    Log.w(TAG, "Role found: " + userRole);
                    if ("user".equals(userRole)){
                        Intent intent = new Intent(LoginActivity.this, MainScreenUser.class);
                        startActivity(intent);
                        finish();
                        Log.w(TAG, "Role found: " + userRole);
                    }
                    else if ("admin".equals(userRole)){
                        Intent intent = new Intent(LoginActivity.this, MainScreenAdmin.class);
                        startActivity(intent);
                        finish();
                    }
                    else {
                        Log.w(TAG, "Unknown role: " + userRole);
                        Toast.makeText(LoginActivity.this, "Unknown user. Please contact support.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Current user: " + user);
                    }
                }
            }
        });
    }
}