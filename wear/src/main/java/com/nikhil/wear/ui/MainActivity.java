package com.nikhil.wear.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nikhil.shared.SendMessageAsyncTask;
import com.nikhil.wear.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.nikhil.shared.Constants.ChannelC.PATH_START_APP;
import static com.nikhil.shared.Constants.IntentC.REQUEST_CODE_GROUP_PERMISSIONS;

/**
 * Created by Nikhil on 20/7/17.
 */

public class MainActivity extends WearableActivity {

    private static final String TAG = "nikhil MainActivity";
    RelativeLayout main_activity_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);
        setAmbientEnabled();

        main_activity_layout = findViewById(R.id.main_activity_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String s = getIntent().getStringExtra(PATH_START_APP);
        if (s != null) {
            if (!s.trim().equals("")) {
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            }
        }
        Log.d(TAG, "onCreate: MESSAGE: " + s);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRequiredPermissions();
    }

    private void checkRequiredPermissions() {
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.BODY_SENSORS,
                Manifest.permission.READ_PHONE_STATE};
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, REQUEST_CODE_GROUP_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_GROUP_PERMISSIONS) {
            for (int resultStatus : grantResults) {
                if (resultStatus == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
    }

    @OnClick(R.id.openOnPhone_Button)
    public void openOnPhoneButton_Clicked() {
        new SendMessageAsyncTask(this, PATH_START_APP).execute("started-from-wear");
    }

    @OnClick(R.id.gotoSensors_Button)
    public void goToSensorsButton_Clicked() {
        startActivity(new Intent(this, SensorActivity.class));
    }

}