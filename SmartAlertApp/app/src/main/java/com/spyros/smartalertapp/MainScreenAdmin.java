package com.spyros.smartalertapp;

import static android.content.ContentValues.TAG;

import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainScreenAdmin extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference submitionsRef = db.collection("submitions");
    SubmitionsAdapter adapter;
    TextView textView6;
    private static final String TAG = "UPDATEDB";
    private static final long TIME_WINDOW = 3600000; // 1 ώρα
    private static final double MAX_DISTANCE = 20000; // 20 Km
    Date currentTime = new Date();
    String lang;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_screen_admin);
        SharedPreferences sp = getApplicationContext().getSharedPreferences("sharedPrefs" , Context.MODE_PRIVATE);
        setUpRecyclerView();
        textView6 = findViewById(R.id.textView6);
        lang = sp.getString("lan" , "Greek");
        changeLang();

    }
    public void changeLang(){
        if (lang.equals("English")){
            textView6.setText("Manage Incidents");
        }
        else if (lang.equals("Greek")){
            textView6.setText("Διαχείριση συμβάντων");
        }
    }



    private void setUpRecyclerView() {
        Query query = submitionsRef

                .orderBy("currentTime", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<submitions> options = new FirestoreRecyclerOptions.Builder<submitions>()
                .setQuery(query, submitions.class)
                .build();

        adapter = new SubmitionsAdapter(options);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SubmitionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                documentSnapshot.getId();
                documentSnapshot.getReference();
                String docId = documentSnapshot.getId();
                String dangerType = documentSnapshot.getString("DangerType");
                Long locLat = documentSnapshot.getLong("LocLat");
                Long locLon = documentSnapshot.getLong("LocLon");
                int priority = documentSnapshot.getLong("Priority").intValue(); // Assuming Priory is stored as a Long
                String comments = documentSnapshot.getString("comments");
                Date currentTime = documentSnapshot.getDate("currentTime");
                String imageId = documentSnapshot.getString("imageId");

                Intent intent = new Intent(MainScreenAdmin.this, AdminSubmitionCheck.class);
                intent.putExtra("DangerType", dangerType);
                intent.putExtra("LocLat", locLat);
                intent.putExtra("LocLon", locLon);
                intent.putExtra("Priority", priority);
                intent.putExtra("Comments", comments);
                intent.putExtra("CurrentTime", currentTime);
                intent.putExtra("ImageId", imageId);
                intent.putExtra("docId", docId);
                startActivity(intent);
            }
        });

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (int i = 0; i < task.getResult().size(); i++) {
                        DocumentSnapshot documentSnapshot1 = task.getResult().getDocuments().get(i);
                        submitions submission1 = documentSnapshot1.toObject(submitions.class);

                        if (submission1.getDangerType().equals("Σεισμός") || (submission1.getDangerType().equals("Earthquake"))) {
                            for (int j = i + 1; j < task.getResult().size(); j++) { // Optimize: start comparison from the next item
                                DocumentSnapshot documentSnapshot2 = task.getResult().getDocuments().get(j);
                                submitions submission2 = documentSnapshot2.toObject(submitions.class);
                                int counter = 0;
                                if ((submission2.getDangerType().equals("Σεισμός") || (submission1.getDangerType().equals("Earthquake")))&&
                                        isWithinTimeWindow(submission1.getCurrentTime(), submission2.getCurrentTime()))
                                        //calculateDistance(/* get lat/lon from submissions */) <= MAX_DISTANCE)
                                {
                                    counter = counter + 1;
                                    if (counter <= 3) {
                                        updatePriority(documentSnapshot1.getId(), 1);
                                        updatePriority(documentSnapshot2.getId(), 1);
                                    }
                                    else if ( counter <= 8){
                                        updatePriority(documentSnapshot1.getId(), 2);
                                        updatePriority(documentSnapshot2.getId(), 2);
                                    }
                                    else if ( counter <= 15){
                                        updatePriority(documentSnapshot1.getId(), 3);
                                        updatePriority(documentSnapshot2.getId(), 3);
                                    }
                                    else if ( counter >15) {
                                        updatePriority(documentSnapshot1.getId(), 4);
                                        updatePriority(documentSnapshot2.getId(), 4);
                                    }
                                }

                            }
                        }
                        if (submission1.getDangerType().equals("Πυρκαγιά") || (submission1.getDangerType().equals("Fire"))) {
                            int counter = 0;

                            for (int j = i + 1; j < task.getResult().size(); j++) { // Optimize: start comparison from the next item
                                DocumentSnapshot documentSnapshot2 = task.getResult().getDocuments().get(j);
                                submitions submission2 = documentSnapshot2.toObject(submitions.class);
                                
                                if ((submission2.getDangerType().equals("Πυρκαγιά") || (submission1.getDangerType().equals("Fire")))&&
                                        isWithinTimeWindow(submission1.getCurrentTime(), submission2.getCurrentTime()))
                                //calculateDistance(/* get lat/lon from submissions */) <= MAX_DISTANCE)
                                {

                                    counter = counter + 1;
                                    if (counter <= 3) {
                                        updatePriority(documentSnapshot1.getId(), 1);
                                        updatePriority(documentSnapshot2.getId(), 1);
                                    }
                                    else if ( counter >3 && counter <= 8){
                                        updatePriority(documentSnapshot1.getId(), 2);
                                        updatePriority(documentSnapshot2.getId(), 2);
                                    }
                                    else if ( counter <= 15){
                                        updatePriority(documentSnapshot1.getId(), 3);
                                        updatePriority(documentSnapshot2.getId(), 3);
                                    }
                                    else if ( counter >15) {
                                        updatePriority(documentSnapshot1.getId(), 4);
                                        updatePriority(documentSnapshot2.getId(), 4);
                                    }
                                }

                            }
                        }
                        if (submission1.getDangerType().equals("Πλυμμήρα") || (submission1.getDangerType().equals("Flood"))) {
                            int counter = 0;
                            for (int j = i + 1; j < task.getResult().size(); j++) { // Optimize: start comparison from the next item
                                DocumentSnapshot documentSnapshot2 = task.getResult().getDocuments().get(j);
                                submitions submission2 = documentSnapshot2.toObject(submitions.class);

                                if ((submission2.getDangerType().equals("Πλυμμήρα")|| (submission1.getDangerType().equals("Flood"))) &&
                                        isWithinTimeWindow(submission1.getCurrentTime(), submission2.getCurrentTime()))
                                //calculateDistance(/* get lat/lon from submissions */) <= MAX_DISTANCE)
                                {counter = counter + 1;
                                    if (counter <= 3) {
                                        updatePriority(documentSnapshot1.getId(), 1);
                                        updatePriority(documentSnapshot2.getId(), 1);
                                    }
                                    else if ( counter >3 && counter <= 8){
                                        updatePriority(documentSnapshot1.getId(), 2);
                                        updatePriority(documentSnapshot2.getId(), 2);
                                    }
                                    else if ( counter <= 15){
                                        updatePriority(documentSnapshot1.getId(), 3);
                                        updatePriority(documentSnapshot2.getId(), 3);
                                    }
                                    else if ( counter >15) {
                                        updatePriority(documentSnapshot1.getId(), 4);
                                        updatePriority(documentSnapshot2.getId(), 4);
                                    }
                                }

                            }
                        }
                        if (submission1.getDangerType().equals("Καταιγίδα") || (submission1.getDangerType().equals("Storm"))) {
                            int counter = 0;
                            for (int j = i + 1; j < task.getResult().size(); j++) { // Optimize: start comparison from the next item
                                DocumentSnapshot documentSnapshot2 = task.getResult().getDocuments().get(j);
                                submitions submission2 = documentSnapshot2.toObject(submitions.class);

                                if (submission2.getDangerType().equals("Καταιγίδα")  || (submission1.getDangerType().equals("Storm") )&&
                                        isWithinTimeWindow(submission1.getCurrentTime(), submission2.getCurrentTime()))
                                {
                                    counter = counter + 1;
                                    if (counter <= 3) {
                                        updatePriority(documentSnapshot1.getId(), 1);
                                        updatePriority(documentSnapshot2.getId(), 1);
                                    }
                                    else if ( counter >3 && counter <= 8){
                                        updatePriority(documentSnapshot1.getId(), 2);
                                        updatePriority(documentSnapshot2.getId(), 2);
                                    }
                                    else if ( counter <= 15){
                                        updatePriority(documentSnapshot1.getId(), 3);
                                        updatePriority(documentSnapshot2.getId(), 3);
                                    }
                                    else if ( counter >15) {
                                        updatePriority(documentSnapshot1.getId(), 4);
                                        updatePriority(documentSnapshot2.getId(), 4);
                                    }
                                }

                            }
                        }
                        if (submission1.getDangerType().equals("Ανεμοστρόβηλος") || (submission1.getDangerType().equals("Tornado"))) {
                            int counter = 0;
                            for (int j = i + 1; j < task.getResult().size(); j++) { // Optimize: start comparison from the next item
                                DocumentSnapshot documentSnapshot2 = task.getResult().getDocuments().get(j);
                                submitions submission2 = documentSnapshot2.toObject(submitions.class);

                                if (submission2.getDangerType().equals("Ανεμοστρόβηλος") || (submission1.getDangerType().equals("Tornado")) &&
                                        isWithinTimeWindow(submission1.getCurrentTime(), submission2.getCurrentTime()))
                                //calculateDistance(/* get lat/lon from submissions */) <= MAX_DISTANCE)
                                {
                                    counter = counter + 1;
                                    if (counter <= 3) {
                                        updatePriority(documentSnapshot1.getId(), 1);
                                        updatePriority(documentSnapshot2.getId(), 1);
                                    }
                                    else if ( counter >3 && counter <= 8){
                                        updatePriority(documentSnapshot1.getId(), 2);
                                        updatePriority(documentSnapshot2.getId(), 2);
                                    }
                                    else if ( counter <= 15){
                                        updatePriority(documentSnapshot1.getId(), 3);
                                        updatePriority(documentSnapshot2.getId(), 3);
                                    }
                                    else if ( counter >15) {
                                        updatePriority(documentSnapshot1.getId(), 4);
                                        updatePriority(documentSnapshot2.getId(), 4);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        });

    }

    //TODO Na valw ena counter gia to posa einai entos toy xronoy dld an einai panw apo 3 to priority na ginei 2
    //TODO an einai panw apo 5 na ginei 3 kai an einai panw apo 10 na ginei 4 dld max



    private boolean isWithinTimeWindow(Date time1, Date time2) {
        long time1Millis = time1.getTime();
        long time2Millis = time2.getTime();
        long timeDifference = Math.abs(time1Millis - time2Millis);
        Log.d(TAG, "IN IS TIME WITHING WINDOW");
        return timeDifference <= TIME_WINDOW;

    }

    private void updatePriority(String docId, int priority) {
        Log.d(TAG, "IN UPDATE PRIORITY.......");
        DocumentReference documentReference = submitionsRef.document(docId);
        documentReference
                .update("Priority" , priority)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}