package com.ut.iatdemolib;

import android.app.Application;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.ut.iatdemolib.R;

public class MApplication extends Application {

    @Override
    public void onCreate() {

        StringBuffer param = new StringBuffer();
        param.append("appid="+getString(R.string.app_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(MApplication.this, param.toString());

        Log.d("MApplication", "onCreate: !!!");

        super.onCreate();
    }
}
