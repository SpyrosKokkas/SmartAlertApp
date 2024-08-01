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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    Button register;
    Button login;
    private FirebaseAuth mAuth;
    EditText registeremail;
    EditText registerpassword;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String lang;
    TextView textView, textView3 ,textView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        registeremail = findViewById(R.id.registeremail);
        registerpassword = findViewById(R.id.registerpassword);
        textView = findViewById(R.id.textView);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        SharedPreferences sp = getApplicationContext().getSharedPreferences("sharedPrefs" , Context.MODE_PRIVATE);

        lang = sp.getString("lan" , "Greek");
        changeLang();
    }

    public void changeLang(){
        if (lang.equals("English")){
            textView.setText("Register");
            textView3.setText("Password");
            textView4.setText("Already have an account? Click here to login");
            login.setText("Login");
            register.setText("Register");
        }
    }

    public void login (View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public void register (View view){

        String email = registeremail.getText().toString();
        String password = registerpassword.getText().toString();
        if ( !email.isEmpty() && !password.isEmpty()){
            mAuth.createUserWithEmailAndPassword(email , password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();

                            Map<String, Object> userData = new HashMap<>();
                            userData.put("UserId", userId);
                            userData.put("email", email);
                            userData.put("role", "user");

                            db.collection("User").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            login(view);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(RegisterActivity.this, "Κάτι πήγε στραβά. Δοκιμάστε ξανά.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            //login(view);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }

                       });
        }else {
            Toast.makeText(RegisterActivity.this, "Παρακαλώ συμπληρώστε όλα τα πεδία", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

}