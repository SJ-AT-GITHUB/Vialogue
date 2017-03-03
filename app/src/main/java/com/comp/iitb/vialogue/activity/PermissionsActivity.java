package com.comp.iitb.vialogue.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.helpers.SharedPreferenceHelper;

public class PermissionsActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_ACCESS_PERMISSION = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // permission granted
            } else {
                // permission not granted
                // ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_ACCESS_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case REQUEST_STORAGE_ACCESS_PERMISSION :
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    // Launch MainActivity
                    onAllPermissionsGranted();
                } else {
                    Toast.makeText(PermissionsActivity.this, "Lokavidya requires storage permission to function correctly", Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    public void onAllPermissionsGranted() {
        SharedPreferenceHelper help = new SharedPreferenceHelper(getApplicationContext());
        try {

            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();

        } catch (NullPointerException e) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
            e.printStackTrace();
        } catch (Exception e) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
            e.printStackTrace();
        }
    }
}
