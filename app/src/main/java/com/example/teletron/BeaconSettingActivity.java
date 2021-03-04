package com.example.teletron;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BeaconSettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    // 커스텀 툴바
    private Toolbar toolbar;
    private TextInputEditText edtUUID, edtMajor, edtMinor;
    private MaterialButton btnSend;

    public String uuid = "2f234454-cf6d-4a0f-adf2-f4911ba9fff4";
    public String major = "2";
    public String minor = "5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_setting);

        toolbar = findViewById(R.id.toolbar);
        edtUUID = findViewById(R.id.edt_uuid);
        edtMajor = findViewById(R.id.edt_date);
        edtMinor = findViewById(R.id.edt_reason);
        btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(btnClickListener);

        // UUID, Major, Minor Shared Preference 값 받아 오기
        SharedPreferences sharedPreferences = getSharedPreferences("BeaconSetting", MODE_PRIVATE);
        uuid = sharedPreferences.getString("UUID","");
        major = sharedPreferences.getString("Major","");
        minor = sharedPreferences.getString("Minor","");

        // UUID, Major, Minor 텍스트
        if(uuid == null){

        }
        edtUUID.setText(uuid);
        edtMajor.setText(major);
        edtMinor.setText(minor);

        //툴바 설정
        setSupportActionBar(toolbar);   // 기본 액션바를 custom toolbar 로 사용한다.
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);  // 툴바에 아이콘이 정상적으로 표시된다.
//      getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 다른 프레그먼트로 넘어갈 때 back 버튼이 툴바에 생김.
//      getSupportActionBar().setDisplayShowTitleEnabled(false); // 타이틀 이름 안보이게
    }

    // Send버튼 클릭시 이벤트
    private Button.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Shared Preference
            SharedPreferences sharedPreferences = getSharedPreferences("BeaconSetting", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // 이메일, 비밀번호 텍스트 값
            String uuid = edtUUID.getText().toString();     // UUID 값 가져오기
            String major = edtMajor.getText().toString();   // Major 값 가져오기
            String minor = edtMinor.getText().toString();   // Major 값 가져오기

            switch (view.getId()) {
                case R.id.btn_send:  // 전송 버튼
                    if(uuid.isEmpty() || major.isEmpty() || minor.isEmpty()){
                        Toast.makeText(com.example.teletron.BeaconSettingActivity.this, "값을 확인해주세요", Toast.LENGTH_SHORT).show();
                    }else{

                        // Json Serialize
                        Post post = new Post(uuid, major, minor);
                        Gson gson = new Gson();

                        String postJson = gson.toJson(post);
                        System.out.println(postJson);

                        // Retrofit
                        String baseUrl = "https://172.10.10.10/";
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(baseUrl)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        // Retrofit 인터페이스 연결
                        RetrofitAPI service = retrofit.create(RetrofitAPI.class);
                        service.listPost(post).enqueue(new Callback<Post>() {
                            @Override
                            public void onResponse(Call<Post> call, Response<Post> response) {
                                if(response.isSuccessful()){
                                    Post posts = response.body();
                                    Log.d(TAG, "onResponse: 성공");
                                    Log.d(TAG, posts.getReason());
                                }
                                Log.d(TAG, "onResponse: " + response);

                            }

                            @Override
                            public void onFailure(Call<Post> call, Throwable t) {
                                Log.d(TAG, "onResponse: Fail");
                            }
                        });

                        // UUID, Major, Minor 로컬 저장
                        editor.putString("UUID", uuid);
                        editor.putString("Major", major);
                        editor.putString("Minor", minor);
                        editor.commit();
                        Toast.makeText(com.example.teletron.BeaconSettingActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                break;
            }
        }
    };
}