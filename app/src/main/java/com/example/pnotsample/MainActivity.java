package com.example.pnotsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        requestPermission();
    }

    private ActivityResultLauncher<String> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted){
                    // Permission Granted
                    //Get Device token from firebase
                    getDeviceToken();
                }else{
                    //Permission denied
                    Log.d(TAG, "Permission denied");
                }
            }
    );
    private static final String TAG = "MyFirebaseMessaging";
    public void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED){
                //Permission already Granted
                Log.d(TAG, "Permission already Granted so get device tokem");
                getDeviceToken();
            }else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                //You can explain user that why do you need permission bu showing Dialogue box or Toast Message

                Log.d(TAG, "You can explain user that why do you need permission bu showing Dialogue");
            }else {
                //Request For Permission
                Log.d(TAG, "Request For Permission");
                resultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }else {
            //GEt Device Token from Firebase
            Log.d(TAG, "Get token from Firebase");
            getDeviceToken();
        }
    }

    public void getDeviceToken(){
        Log.d(TAG, "Inside get token");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Log.e(TAG," Fetching Toke Failed"+ task.getException());
                return;
            }

            //Get Device Token
            String token = task.getResult();
            Log.v(TAG,"Device Token: "+token);
        });
    }
}