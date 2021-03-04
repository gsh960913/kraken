package com.example.teletron;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;

import org.altbeacon.beacon.BeaconManager;

public class BeaconFragment extends Fragment {


    private LottieAnimationView lottie_home;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private TextInputEditText edtUUID;
    private String uuid;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_beacon, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UUID, Major, Minor Shared Preference 값 받아 오기
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("BeaconSetting", Context.MODE_PRIVATE);
        // UUID, Major, Minor Shared Preference 값 받아 오기
        uuid = sharedPreferences.getString("UUID","");

        // UUID Set
        edtUUID = view.findViewById(R.id.edt_uuid);
        // Lottie
        lottie_home = view.findViewById(R.id.lottie_home);
        //json 애니메이션을 셋팅한다
        lottie_home.setAnimation("ani_upload.json");
        // 반복하기
        lottie_home.loop(true);
        // 애니메이션 재생하기
        lottie_home.playAnimation();
        // UUID Settings
        edtUUID.setText(uuid);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}