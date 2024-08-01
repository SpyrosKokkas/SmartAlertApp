package com.spyros.smartalertapp;

import static android.app.ProgressDialog.show;
import static android.content.ContentValues.TAG;

import static com.spyros.smartalertapp.App.CHANNEL_1_ID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Registry;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Timestamp;
import java.util.Date;

import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.spyros.smartalertapp.databinding.ActivityMainBinding;


public class AdminSubmitionCheck extends AppCompatActivity {

    ImageView imageView;
    TextView DangerType, dateView, commentsView;
    String imagePathId, docId, dangerType;
    //FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    StorageReference storageReference;
    private NotificationManagerCompat notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_admin_submition_check);
        imageView = findViewById(R.id.imageView);
        DangerType = findViewById(R.id.DangerType);
        dateView = findViewById(R.id.dateView);
        commentsView = findViewById(R.id.commentsView);
        notificationManager = NotificationManagerCompat.from(this);
        Intent intent = getIntent();
        dangerType = intent.getStringExtra("DangerType");
        Date currentTime = (Date) intent.getSerializableExtra("CurrentTime");
        String comments = intent.getStringExtra("Comments");
        imagePathId = intent.getStringExtra("ImageId");
        docId = intent.getStringExtra("docId");

        DangerType.setText(dangerType);
        assert currentTime != null;
        dateView.setText(currentTime.toString());
        commentsView.setText(comments);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();


        String imagePath = imagePathId;
        StorageReference imageRef = storageReference.child(imagePath);
        Log.d("ImageURL", imageRef.toString());

        storageReference = FirebaseStorage.getInstance().getReference(imagePathId);
        try {
            File localfile = File.createTempFile("tempfile", ".jpg");
            storageReference.getFile(localfile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void dismiss(View view) {
        db.collection("submitions").document(docId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(AdminSubmitionCheck.this, "Απορρίφθηκε Επιτυχώς!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                        Toast.makeText(AdminSubmitionCheck.this, "Προέκυψε σφάλμα. Δοκιμάστε ξανά αργότερα!", Toast.LENGTH_SHORT).show();
                    }
                });
        Intent intent = new Intent(this , MainScreenAdmin.class );
        startActivity(intent);

    }


    public void notification(View view) {

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(dangerType)
                .setContentText("Προσοχή!" + dangerType + "Παρακαλώ δείξτε μεγάλη προσοχή")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 123);
            return;
        }
        notificationManager.notify(1, notification);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainScreenAdmin.class));
        finish();
    }


}