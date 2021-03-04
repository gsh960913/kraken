package com.example.teletron;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;

import org.altbeacon.beacon.BeaconManager;

import java.util.Objects;

public class BeaconActivity extends AppCompatActivity {

    private static final String TAG = "sampleCreateBeacon";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    public static final int REQUEST_CODE = 100;

    // 커스텀 툴바
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        //툴바 설정
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);   // 기본 액션바를 custom toolbar 로 사용한다.
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);  // 툴바에 아이콘이 정상적으로 표시된다.
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 다른 프레그먼트로 넘어갈 때 back 버튼이 툴바에 생김.
//        getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 이름 안보이게
//        getSupportActionBar().setTitle("텔레트론");
        verifyBluetooth();

        // Android 퍼미션 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }
                });
                builder.show();
            }
        }
    }
    // 툴바 menu.xml inflate
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.beacon_menu, menu);
        return true;
    }
    // 툴바 메뉴 이벤트 처리
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.beacon_setting:
                Intent intent_setting = new Intent(this, BeaconSettingActivity.class);
                startActivityForResult(intent_setting, REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_in_right);
                Toast.makeText(this, "Beacon Setting Execute", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // BeaconSetting 결과 값 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE){
            if (resultCode != Activity.RESULT_OK){
                Toast.makeText(this, "Activity Result를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
                return ;
            }
            Toast.makeText(this, "Activity Result를 받았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onEnableClicked(View view) {
        BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
        if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
            application.disableMonitoring();
            ((Button) findViewById(R.id.bt_beacon)).setText("포그라운드 시작");
            ((Button) findViewById(R.id.bt_beacon)).setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }
        else {
            ((Button) findViewById(R.id.bt_beacon)).setText("포그라운드 중지");
            ((Button) findViewById(R.id.bt_beacon)).setBackgroundColor(ContextCompat.getColor(this,R.color.colorAccent));

            application.enableMonitoring();
        }
    }

    // 퍼미션 요청후 callback
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background location permission granted");
                } else {
                    final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
        application.setMonitoringActivity(this);
        updateLog(application.getLog());
    }
    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }
    private void verifyBluetooth() {
        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //finish();
                        //System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    //finish();
                    //System.exit(0);
                }
            });
            builder.show();
        }
    }

    public void updateLog(final String log) {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d("", "=========== Update Log Show ===========");
            }
        });
    }
}