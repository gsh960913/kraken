package com.example.teletron;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by dyoung on 12/13/13.
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";

    // ADVERTISE 모드 설정 상수 값
    private static final int ADVERTISE_MODE_LOW_LATENCY = 2;
    private static final int ADVERTISE_TX_POWER_MEDIUM = 2;

    // Beacon UUID, Major, Minor Setting
    public String uuid = null;
    public String major = "3";
    public String minor = "5";

    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private BeaconActivity beaconActivity = null;
    private String cumulativeLog = "";

    public void onCreate() {
        super.onCreate();

        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

        /* ################################################################### */
        /* ######## UUID, Major, Minor Shared Preference 값 받아 오기  ######## */
        /* ################################################################### */

        // Shared Preference
        SharedPreferences sharedPreferences = getSharedPreferences("BeaconSetting", MODE_PRIVATE);
        SharedPreferences isInit = getSharedPreferences("isInit", MODE_PRIVATE);

        // Editor Setting
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences.Editor editor2 = isInit.edit();

        // 최초 실행시 UUID 값 지정
        boolean init = isInit.getBoolean("isInit",false);
        if(!init){
            Log.d("Is first Time 1 ?", "first");
            uuid = String.valueOf(UUID.randomUUID()).toUpperCase();
            editor.putString("UUID", uuid);
            editor2.putBoolean("isInit",true);
            editor.commit();
            editor2.commit();
        }else{
            uuid = sharedPreferences.getString("UUID","");
            Log.d("Is first Time? 2 ", "not first");
        }

        /* ##################################### */
        /* ######## 비콘 Transmit 생성  ######## */
        /* ##################################### */

        Beacon beacon = new Beacon.Builder()
                .setId1(uuid) // UUID for beacon  "2f234454-cf6d-4a0f-adf2-f4911ba9fff4"
                .setId2(major) // Major for beacon
                .setId3(minor) // Minor for beacon
                .setManufacturer(0x004C) // Radius Networks.0x0118  Change this for other beacon layouts//0x004C for iPhone
                .setTxPower(-56) // Power in dB
                .setDataFields(Arrays.asList(new Long[] {0l})) // Remove this for beacon layouts without d: fields
                .build();

        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");

        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        beaconTransmitter.setAdvertiseMode(ADVERTISE_MODE_LOW_LATENCY);
        beaconTransmitter.setAdvertiseTxPowerLevel(ADVERTISE_TX_POWER_MEDIUM);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Advertisement start failed with code: " + errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.i(TAG, "Advertisement start succeeded.");
            }
        });

        beaconManager.setDebug(true);

        // 비콘 포그라운드 서비스
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle("Beacon 입출입 관리");

        Intent intent = new Intent(this, BeaconActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);

        // Notification 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // 포그라운드시 Disable 해야 될 목록
        beaconManager.setEnableScheduledScanJobs(false);
        // 백그라운드시 Scan 주기 설정
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1500);

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // 백그라운드 지역 설정
        Region region = new Region("com.example.mobile_practice",
                Identifier.parse(uuid), null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        // 비콘 Power Saver
        backgroundPowerSaver = new BackgroundPowerSaver(this);
    }

    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
        }
    }
    public void enableMonitoring() {
        Region region = new Region("backgroundRegion",
                null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }


    @Override
    public void didEnterRegion(Region arg0) {
        Log.d(TAG, "Sending notification.");
        sendNotification();
        Log.d(TAG, "did enter region.");
        // 일정 범위 도달 시 백그라운드 실행
        Intent Background_intent = new Intent(this, BeaconActivity.class);
        Background_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Send a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        this.startActivity(Background_intent);
        if (beaconActivity != null) {
            // If the Monitoring Activity is visible, we log info about the beacons we have
            // seen on its display
            logToDisplay("I see a beacon again" );
        }
    }

    @Override
    public void didExitRegion(Region region) {
        logToDisplay("I no longer see a beacon.");
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
    }

    private void sendNotification() {
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Beacon Reference Notifications",
                    "Beacon Reference Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, channel.getId());
        }
        else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, BeaconActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle("I detect a beacon");
        builder.setContentText("Tap here to see details in the reference app");
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(BeaconActivity activity) {
        this.beaconActivity = activity;
    }

    private void logToDisplay(String line) {
//        cumulativeLog += (line + "\n");
//        if (this.beaconActivity != null) {
//            this.beaconActivity.updateLog(cumulativeLog);
//        }
//        Toast.makeText(this, "LogToDisplay " , Toast.LENGTH_LONG).show();
    }

    public String getLog() {
        return cumulativeLog;
    }

}
