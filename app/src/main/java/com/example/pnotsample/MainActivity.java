package com.example.pnotsample;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                Log.w("PNotSample", "Requesting exact alarm permission");
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
        requestPermission();
        // Check if the activity was launched by a notification click
//        handleIntent(getIntent());
    }


//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        // If the activity is already running in the foreground and a new notification is tapped
//        setIntent(intent); // Update the activity's intent
//        handleIntent(intent);
//    }

    private void handleIntent(Intent intent) {
        Log.d("MyFirebaseMessaging","handling intent here");
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();

            String first_open = extras.getString("first_open");
            String discounted_pizza_offer = extras.getString("discounted_pizza_offer");
            Log.d("MyFirebaseMessaging","first_open: "+first_open);
            Log.d("MyFirebaseMessaging","discounted_pizza_offer: "+discounted_pizza_offer);


        }
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
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()){
                    Log.e(TAG," Fetching Toke Failed"+ task.getException());
                    return;
                }

                //Get Device Token
                String token = task.getResult();
                Log.v(TAG,"Device Token: "+token);
            }
        });
    }
}