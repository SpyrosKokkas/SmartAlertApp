    package com.spyros.smartalertapp;

    import static android.content.ContentValues.TAG;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;

    import android.Manifest;
    import android.app.ProgressDialog;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.graphics.Bitmap;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.net.Uri;
    import android.os.Bundle;
    import android.provider.MediaStore;
    import android.util.Log;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ImageView;
    import android.widget.Spinner;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.material.textfield.TextInputLayout;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.OnProgressListener;
    import com.google.firebase.storage.StorageReference;
    import com.google.firebase.storage.UploadTask;
    import com.google.type.LatLng;

    import java.io.IOException;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.UUID;


    public class MainScreenUser extends AppCompatActivity implements AdapterView.OnItemSelectedListener , LocationListener {


        String[] dangers = {"Πυρκαγιά", "Πλυμμήρα", "Σεισμός", "Καταιγίδα", "Ανεμοστρόβηλος"};
        String[] dangersEN = {"Fire", "Flood", "Earthquake", "Storm", "Tornado"};
        Button upload, submit;
        FirebaseStorage storage;
        StorageReference storageRef;
        private final int PICK_IMAGE_REQUEST = 22;
        ImageView imageView;
        private Uri filePath;
        String imageRandomId, selection;
        Double UserLat, UserLon;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth;
        private LocationManager locationManager;
        EditText commentsEditText;
        String lang;
        TextView textView6, textView80, textView7, textViewComment;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            setContentView(R.layout.activity_main_screen_user);
            Spinner spinner = findViewById(R.id.spinner);
            spinner.setOnItemSelectedListener(this);
            upload = findViewById(R.id.upload);
            submit = findViewById(R.id.submit);
            imageView = findViewById(R.id.imageView3);
            textView6 = findViewById(R.id.textView6);
            textView80 = findViewById(R.id.textView80);
            textView7 = findViewById(R.id.textView7);
            textViewComment = findViewById(R.id.textViewcomment);
            mAuth = FirebaseAuth.getInstance();
            SharedPreferences sp = getApplicationContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
            lang = sp.getString("lan", "Greek");

            commentsEditText = findViewById(R.id.editTextText);
            changeLan();
            if (lang.equals("Greek")) {
                ArrayAdapter adapter
                        = new ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        dangers);

                adapter.setDropDownViewResource(
                        android.R.layout
                                .simple_spinner_dropdown_item);

                spinner.setAdapter(adapter);
            }else if (lang.equals("English")){
                ArrayAdapter adapter
                        = new ArrayAdapter(
                        this,
                        android.R.layout.simple_spinner_item,
                        dangersEN);

                adapter.setDropDownViewResource(
                        android.R.layout
                                .simple_spinner_dropdown_item);

                spinner.setAdapter(adapter);
            }
            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference();

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getimage();
                }
            });


        }

        public void changeLan(){
            if (lang.equals("English")){
                textView6.setText("Report Incident");
                textView80.setText("Incident Type");
                textView7.setText("Image");
                textViewComment.setText("Comments");
                upload.setText("Add image");
                submit.setText("Submit");
            }

        }


        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            selection = parent.getItemAtPosition(pos).toString();
            Toast.makeText(MainScreenUser.this , "Selected:" + selection , Toast.LENGTH_SHORT).show();

        }
        public void onNothingSelected(AdapterView<?> parent) {
            Toast.makeText(MainScreenUser.this , "Nothing Selected:", Toast.LENGTH_SHORT).show();
        }

        public void uploadimage (String imageRandomId){
            if (filePath != null) {


                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Uploading...");
                progressDialog.show();

                String userId = mAuth.getUid();


                StorageReference ref = storageRef.child(this.imageRandomId);

                ref.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressDialog.dismiss();
                                Toast.makeText(MainScreenUser.this, "Επιτυχής Υποβολή", Toast.LENGTH_SHORT).show();
                            }
                        })
                            .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                // Error, Image not uploaded
                                progressDialog.dismiss();
                                Toast.makeText(MainScreenUser.this, "Απέτυχε " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int) progress + "%");
                            }
                        });
            }else {

            }
        }

        public void getimage (){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(
                            intent,
                            "Select Image from here..."),
                    PICK_IMAGE_REQUEST);
        }
        @Override
        protected void onActivityResult(int requestCode,
                                        int resultCode,
                                        Intent data)
        {

            super.onActivityResult(requestCode,
                    resultCode,
                    data);

            if (requestCode == PICK_IMAGE_REQUEST
                    && resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null) {

                filePath = data.getData();
                try {

                    Bitmap bitmap = MediaStore
                            .Images
                            .Media
                            .getBitmap(
                                    getContentResolver(),
                                    filePath);
                    imageView.setImageBitmap(bitmap);
                }

                catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        public void submit (View view){
            try {
                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                    Log.d(TAG, "Elegxos GPS");
                } else {
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {
                        // Use the last known location
                        UserLat = lastKnownLocation.getLatitude();
                        UserLon = lastKnownLocation.getLongitude();
                        Log.d(TAG, "Last Known Location - Lat: " + UserLat + ", Lon: " + UserLon);
                        submitData();
                    } else {
                        // If last known location is not available, request location updates
                        Log.d(TAG, "Last known location not available, requesting location updates");
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainScreenUser.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }



        private void submitData() {
            Log.d(TAG, "SubmitData");
            imageRandomId = UUID.randomUUID().toString();
            String submisionId = UUID.randomUUID().toString();
            Date CurrentTime = Calendar.getInstance().getTime();
            String User = mAuth.getUid();
            String comments = commentsEditText.getText().toString();
            if (comments.isEmpty()) {
                comments = "Χωρίς σχόλια";
            }
            if (selection == " ") {
                Toast.makeText(MainScreenUser.this, "Παρακαλώ επιλέξετε είδος κινδύνου", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> userSubmition = new HashMap<>();
                userSubmition.put("DangerType", selection);
                userSubmition.put("LocLat", UserLat);
                userSubmition.put("LocLon", UserLon);
                userSubmition.put("currentTime", CurrentTime);
                userSubmition.put("imageId", imageRandomId);
                userSubmition.put("comments", comments);
                userSubmition.put("Priority" , 0);

                uploadimage(imageRandomId);

                db.collection("submitions").document(submisionId)
                        .set(userSubmition)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(MainScreenUser.this, "Επιτυχής υποβολή", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainScreenUser.this, "Κάτι πήγε στραβά. Δοκιμάστε ξανά.", Toast.LENGTH_SHORT).show();
                            }
                        });


                // Remove location updates after getting the first location
                locationManager.removeUpdates(this);
            }
        }

        @Override
        public void onLocationChanged(@NonNull Location location) {

        }

        public void goToStats(View view) {
            Intent intent = new Intent(MainScreenUser.this , Statistics.class);
            startActivity(intent);
        }
    }