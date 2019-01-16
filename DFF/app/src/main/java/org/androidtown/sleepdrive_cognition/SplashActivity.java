package org.androidtown.sleepdrive_cognition;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;

public class SplashActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_style);
        Log.e("bsos1202","SplashActivity income!!");
        Handler hd = new Handler();
        hd.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2200); // 3초 후 이미지를 닫습니다
    }

}
