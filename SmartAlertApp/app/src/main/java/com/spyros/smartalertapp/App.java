package com.spyros.smartalertapp;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends  android.app.Application{
    public static final String CHANNEL_1_ID = "AlertNotification";


    @Override
    public void onCreate() {
        super.onCreate();


        createNotChannel();
    }


    private void createNotChannel(){
        NotificationChannel channel1 = new NotificationChannel(
                CHANNEL_1_ID,
                "Event Notification",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel1.setDescription("EventAlert");

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel1);

    }
}
