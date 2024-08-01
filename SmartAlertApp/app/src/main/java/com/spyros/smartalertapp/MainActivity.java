package com.spyros.smartalertapp;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    Button loginbtn;
    Button registerbtn;
    ImageView greekLan , englishLan;
    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference users = db.collection("User");
    String sharedPrefs = "sharedPrefs";
    String language = "lan" , languageChange;
    int a;
    TextView welcometext ,textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerbtn = findViewById(R.id.registerbtn);
        loginbtn = findViewById(R.id.loginbtn);
        mAuth = FirebaseAuth.getInstance();
        greekLan = findViewById(R.id.greekLan);
        englishLan = findViewById(R.id.EnglishLan);
        welcometext = findViewById(R.id.welcometext);
        textView2 = findViewById(R.id.textView2);
        applyChange();
        updateView();
    }


    public void changeLanguage(View view){
        greekLan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                a = 1;
                saveChange();
                applyChange();
            }
        });

        englishLan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                a=2;
                saveChange();
                applyChange();
            }
        });
    }

    public void saveChange() {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (a == 1) {
            editor.putString(language, "Greek");
        }
        else if (a == 2 ){
            editor.putString(language, "English");
        }
        editor.apply();
        updateView();
    }

    public void applyChange(){
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefs , MODE_PRIVATE);
        languageChange = sharedPreferences.getString(language ,"Greek");


        updateView();

    }

    public void updateView(){
        if (languageChange.equals("English")){
            welcometext.setText("Get immediate information about risks and incidents");
            textView2.setText("Please login or register to continue");
            loginbtn.setText("Login");
            registerbtn.setText("Register");
        }
        else if ( languageChange.equals("Greek")){
            welcometext.setText("Ενημερώσου άμεσα για κινδύνους και συμβάντα");
            textView2.setText("Συνδεθείτε για να συνεχίσετε");
            loginbtn.setText("Σύνδεση");
            registerbtn.setText("Εγγραφή");
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

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
                        Log.w(TAG, "Unknown role: " + userRole);
                        if ("user".equals(userRole)){
                            Intent intent = new Intent(MainActivity.this, MainScreenUser.class);
                            startActivity(intent);
                            finish();
                            Log.w(TAG, "Role found " + userRole);
                        }
                        else if ("admin".equals(userRole)){
                            Intent intent = new Intent(MainActivity.this, MainScreenAdmin.class);
                            startActivity(intent);
                            finish();
                            Log.w(TAG, "Role found " + userRole);
                        }
                        else {
                            Log.w(TAG, "Unknown role: " + userRole);
                            Toast.makeText(MainActivity.this, "Unknown user. Please contact support.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Current user: " + user);
                        }
                    }
                }
            });

        }

    }
}