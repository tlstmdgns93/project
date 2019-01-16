package org.androidtown.sleepdrive_cognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private final int PERMISSIONS_REQUEST_RESULT = 100; // 콜백함수 호출시 requestCode로 넘어가는 구분자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        startActivity(new Intent(this,SplashActivity.class));
        //new Intent는 context와 class를 인자로 받습니다.
        //만들어둔 SplashActivity를 호출합니다.

        requestPermissionAUDIO();

        ImageButton driving = (ImageButton) findViewById(R.id.driving_button);
        ImageButton restarea_search = (ImageButton) findViewById(R.id.restarea_search_button);
        ImageButton setting = (ImageButton) findViewById(R.id.setting_button);
        ImageView sleep1 = (ImageView) findViewById(R.id.sleeping1);
        ImageView sleep2 = (ImageView) findViewById(R.id.sleeping2);

        driving.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), org.androidtown.sleepdrive_cognition.googleapi.GooglyEyesActivity.class);
                startActivityForResult(intent,110);
            }
        });

        sleep1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), org.androidtown.sleepdrive_cognition.googleapi.GooglyEyesActivity.class);
                startActivityForResult(intent,110);
            }
        });


        restarea_search.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SleepRestArea.class);
                startActivityForResult(intent,120);
            }
        });
        sleep2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SleepRestArea.class);
                startActivityForResult(intent,120);
            }
        });

        setting.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), VoiceRecord.class);
                startActivityForResult(intent,130);
            }
        });

    }

    public boolean requestPermissionAUDIO(){
        int sdkVersion = Build.VERSION.SDK_INT;
        // 해당 단말기의 안드로이드 OS버전체크
        if(sdkVersion >= Build.VERSION_CODES.M) {
            // 버전 6.0 이상일 경우

            // 해당 퍼미션이 필요한지 체크 - true : 퍼미션 동의가 필요한 권한일 때 , false : 퍼미션 동의가 필요하지 않은 권한일 때.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // true : 퍼미션 동의가 필요한 권한일 때

                // 사용자가 최초 퍼미션 체크를 했는지 확인한다. - true : 사용자가 최초 퍼미션 요청시 '거부'했을 때, false : 퍼미션 요청이 처음일 경우
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                    // true : 사용자가 최초 퍼미션 요청시 '거부'해서 재요청일 때
                } else {
                    // false : 퍼미션 요청이 처음일 경우.

                    // 퍼미션의 동의 여부를 다이얼로그를 띄워 요청한다. 이 때 '동의', '거부'의 결과값이 onRequestPermissionsResult 으로 콜백된다.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO},
                            PERMISSIONS_REQUEST_RESULT);
                }
            }else {
                // false : 퍼미션 동의가 필요하지 않은 권한일 때.
            }
        }else{
            // version 6 이하일 때에는 별도의 작업이 필요없다.
        }

        return true;

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (PERMISSIONS_REQUEST_RESULT == requestCode) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 퍼미션에 동의 했을 때 콜백. 다음 작업을 진행한다.

            } else {
                // 퍼미션을 거부 하였을 때 콜백. 퍼미션이 거부 되어 해당 작업을 진행 할 수 없다.

            }
            return;
        }

    }

}
