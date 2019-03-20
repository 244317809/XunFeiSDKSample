package com.ut.iatdemolib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.ut.iatdemolib.util.FucUtil;
import com.ut.iatdemolib.util.JsonParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class IatActivity extends AppCompatActivity {

    private static final int MIN_SCORE = 50;
    private static String TAG = "IatDemo";

    private  final String GRAMMAR_TYPE_BNF = "bnf";

    //语音听写对象
    private SpeechRecognizer mIat;
    //语音听写UI
    private RecognizerDialog mIatDialog;
    //听写结果内容
//    private EditText mResultText;
    //用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();

    private ImageButton mStartBtn;
    private Button mSubmitBtn;
    private TextView mDataSourceTextView;

    private int progress = 0;
    private String mContent;

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FeedbackFragment mFeedbackFragment;
    private DataSourceFragment mDataSourceFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iat);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        requestPermissions();

//        RadioGroup radioGroup = findViewById(R.id.radio_group);
//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (checkedId == R.id.radio_free) {
//                    mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, null);
//                    mDataSourceTextView.setVisibility(View.GONE);
//                } else if (checkedId == R.id.radio_datasource) {
//                    // 设置本地识别使用语法id
//                    mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
//                    mDataSourceTextView.setText("当前可识别的数据源:\n" + mContent);
//                    mDataSourceTextView.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//        mDataSourceTextView = findViewById(R.id.tv_datasource);



        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return position == 0 ?
                        (mFeedbackFragment = new FeedbackFragment()) :
                        (mDataSourceFragment = new DataSourceFragment());
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return position == 0 ? "用户反馈" : "数据源识别";
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, null);
                } else {
                    // 设置本地识别使用语法id
                    mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //初始化识别无UI识别对象
        //使用SpeechRecognizer对象和回调
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);

        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(this, mInitListener);

        // 设置本地识别资源
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        //设置参数
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "2000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "60000");
        // 设置识别的门限值
        mIat.setParameter(SpeechConstant.MIXED_THRESHOLD, "90");


        // TODO: 2019/3/19 测试更新本地词典
        // 本地语法构建路径
        String grmPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/msc/test";
        // 设置语法构建路径
        mIat.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        // 设置文本编码格式
        mIat.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");

        String mLocalGrammar = FucUtil.readFile(this,"call.bnf", "utf-8");
        int buildGrammarRet = mIat.buildGrammar(GRAMMAR_TYPE_BNF, mLocalGrammar, grammarListener);
        if(buildGrammarRet != ErrorCode.SUCCESS){
            showTip("语法构建失败,错误码：" + buildGrammarRet);
        }
    }

    public void startIatRecognize() {
        // 显示听写对话框
        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
        showTip(getString(R.string.text_begin));
        //获取字体所在的控件，设置为"", 隐藏对话框中"语音识别功能由讯飞输入法提供"字样.
        TextView txt = (TextView)mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
        txt.setText("");
    }

    private String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //识别通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "iat/common.jet"));
        tempBuffer.append(";");
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "iat/sms_16k.jet"));
        //识别8k资源-使用8k的时候请解开注释
        return tempBuffer.toString();
    }

    public void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IatActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败， 错误码: " + code);
            }
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());

        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 构建语法监听器。
     */
    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                showTip("语法构建成功：" + grammarId);

                // 设置语法名称
                mIat.setParameter(SpeechConstant.GRAMMAR_LIST, "call");

                List<String> data = Arrays.asList(getResources().getStringArray(R.array.names));
                StringBuilder stringBuilder = new StringBuilder();
                for (String item : data) {
                    stringBuilder.append(item).append("\n");
                }

//                mContent = "张海羊\n刘婧\n王锋\n田林\n谭志武\n李杏宇";
                mContent = stringBuilder.toString();
                int ret = mIat.updateLexicon("list", mContent, new LexiconListener() {
                    @Override
                    public void onLexiconUpdated(String s, SpeechError speechError) {
                        if(speechError == null) {
                            showTip("词典更新成功");
                        } else {
                            showTip("词典更新失败,错误码："+speechError.getErrorCode());
                        }
                    }
                });
                if (ret != ErrorCode.SUCCESS) {
                    showTip("更新词典失败,错误码：" + ret);
                }
            } else {
                showTip("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            String id = Thread.currentThread().getName();
            Log.d(TAG, "recognizer result：" + results.getResultString());


            String text = JsonParser.parseIatResult(results.getResultString());

            int position = mViewPager.getCurrentItem();
            if (position == 0) {
                mFeedbackFragment.onSpeechRecognizeResult(text);
            } else {
                int score = Integer.parseInt(JsonParser.parseIatScore(results.getResultString()));
                if (score < MIN_SCORE) {
                    text = "";
                }
                mDataSourceFragment.onSpeechRecognizeResult(text);
            }

            mIatDialog.dismiss();
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
            mIatDialog.getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIatDialog.dismiss();
                }
            }, 2000);
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( null != mIat ){
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE,Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS,Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_CONTACTS},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
