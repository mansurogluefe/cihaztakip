package com.efe.gpstelegram;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService extends Service {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler handler;
    private Runnable runnable;
    private Location currentLocation;

    private static final String BOT_TOKEN = "7739075002:AAEpEvduB6kSgdjtb9LogBdHIVBVFRDherw";
    private static final String CHAT_ID = "1772624267";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "LocationChannel")
                .setContentTitle("GPS Takip Aktif")
                .setContentText("Konum verisi gönderiliyor...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(1, notification);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        handler = new Handler(Looper.getMainLooper());

        locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                currentLocation = location;
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 1, locationListener);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    double lat = currentLocation.getLatitude();
                    double lon = currentLocation.getLongitude();
                    sendLocationToTelegram(lat, lon);
                }
                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnable);
    }

    private void sendLocationToTelegram(double lat, double lon) {
        new Thread(() -> {
            try {
                String message = "🛰️ Konum: " + lat + ", " + lon + "\n— Efe’nin takip sisteminden gönderildi.";
                String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String payload = "chat_id=" + CHAT_ID + "&text=" + message;
                OutputStream os = conn.getOutputStream();
                os.write(payload.getBytes());
                os.flush();
                os.close();
                conn.getInputStream();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("LocationChannel",
                    "GPS Takip Kanalı", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
