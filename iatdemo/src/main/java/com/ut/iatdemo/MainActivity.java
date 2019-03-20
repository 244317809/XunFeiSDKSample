package com.ut.iatdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ut.iatdemolib.util.IatUiUtils;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void start(View view) {
        IatUiUtils.startIatActivity(this);
    }
}
