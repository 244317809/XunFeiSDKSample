package com.ut.iatdemolib.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.ut.iatdemolib.IatActivity;
import com.ut.iatdemolib.MApplication;
import com.ut.iatdemolib.R;

public class IatUiUtils {

    public static void startIatActivity(Context context) {
        Intent intent = new Intent(context, IatActivity.class);
        context.startActivity(intent);
    }

    public static void initXunFeiIatSdk(Context context) {
        StringBuffer param = new StringBuffer();
        param.append("appid=" + "5c85bd2f");
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());
    }

    public static void updateXunFeiLexicon(String grammar,
                                           SpeechRecognizer recognizer,
                                           LexiconListener listener) {
        recognizer.updateLexicon("<List>", grammar, listener);
    }
}
