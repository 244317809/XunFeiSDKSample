package com.ut.iatdemolib;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class FeedbackFragment extends Fragment implements SpeechSupportable {

    //听写结果内容
    private EditText mResultText;
    private ImageButton mStartBtn;
    private Button mSubmitBtn;

    private IatActivity mHost;

    private int progress = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubmitBtn = view.findViewById(R.id.btn_submit);
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mResultText.getText().toString())) {
                    showTip("内容不能为空。");
                    return;
                }
                final ProgressDialog dialog = new ProgressDialog(getContext());

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        dialog.setProgress(progress += 33);
                        if (progress < 100) {
                            dialog.getWindow().getDecorView().postDelayed(this, 800);
                        } else {
                            dialog.dismiss();
                            mResultText.setText("");
                            showTip("上传完成");
                        }
                    }
                };
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setTitle("上传中...");
                dialog.show();
                dialog.getWindow().getDecorView().postDelayed(runnable, 800);
            }
        });
        mResultText = view.findViewById(R.id.et_result);
        mStartBtn = view.findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHost.startIatRecognize();
            }
        });
        mStartBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mHost.startIatRecognize();
                return true;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHost = (IatActivity) context;
    }

    private void showTip(String string) {
        mHost.showTip(string);
    }

    @Override
    public void onSpeechRecognizeResult(String result) {
        mResultText.append(result);
        mResultText.setSelection(mResultText.length());
    }


}
