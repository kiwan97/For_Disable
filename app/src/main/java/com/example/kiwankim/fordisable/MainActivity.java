package com.example.kiwankim.fordisable;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.kiwankim.fordisable.motiondetection.MotionDetector;
import com.example.kiwankim.fordisable.motiondetection.MotionDetectorCallback;

public class MainActivity extends Activity implements RecognitionListener {

    private TextView returnedText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private WebView webView;
    private FrameLayout FL_matrix100;
    private FrameLayout FL_matrix50;
    private static final int MESSAGE_TIMER_START = 100;

    private TextView txtStatus;
    private MotionDetector motionDetector;
    private boolean isMotionPossible = false;
    private boolean isMotionUnder = true;

    private Toast toast;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toast design
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.supertoast,
                (ViewGroup) findViewById(R.id.toast_layout_root));

        text = (TextView) layout.findViewById(R.id.toast_text);
        text.setText("Welcome!");

        toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        FL_matrix100 = findViewById(R.id.matrix100);
        FL_matrix50 = findViewById(R.id.matrix50);
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new MyWebClient());
        WebSettings set = webView.getSettings();
        set.setJavaScriptEnabled(true);
        set.setBuiltInZoomControls(true);
        webView.loadUrl("http://www.naver.com");
//        webView.setOnTouchListener(new View.OnTouchListener(){
//            public boolean onTouch(View v, MotionEvent event) {
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        Toast.makeText(getApplicationContext(),"Action_down",Toast.LENGTH_SHORT).show();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        Toast.makeText(getApplicationContext(),"Action_Up",Toast.LENGTH_SHORT).show();
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        Toast.makeText(getApplicationContext(),"Action_MOVE",Toast.LENGTH_SHORT).show();
//                        break;
//                }
//
//                return false;
//            }
//
//        });
        requestAudioPermissions();
        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton1);

        TimerHandler timerHandler = new TimerHandler();
        timerHandler.sendEmptyMessage(MESSAGE_TIMER_START);

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"ko-KR");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en-US");
        recognizerIntent.putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", new String[]{"en-US"});
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                    make_Touch("");
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                }
            }
        });

        //Motion Parts
        txtStatus = (TextView) findViewById(R.id.txtStatus);

        motionDetector = new MotionDetector(this, (SurfaceView) findViewById(R.id.surfaceView));
        motionDetector.setMotionDetectorCallback(new MotionDetectorCallback() {
            @Override
            public void onMotionDetected() {
                if(isMotionPossible) {
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(80);
                    //아래로 드래그하기
                    long time = SystemClock.uptimeMillis();
                    int interval = 300;
                    if(isMotionUnder) {
                        webView.dispatchTouchEvent(MotionEvent.obtain(time,
                                time,
                                MotionEvent.ACTION_DOWN,
                                300,
                                1000,
                                0));
                        webView.dispatchTouchEvent(MotionEvent.obtain(time,
                                time,
                                MotionEvent.ACTION_MOVE,
                                300,
                                1000 - interval,
                                0));
                        webView.dispatchTouchEvent(MotionEvent.obtain(time,
                                time,
                                MotionEvent.ACTION_UP,
                                300,
                                1000 - interval,
                                0));
                    }else {
                        //위로 드래그하기
                        webView.dispatchTouchEvent(MotionEvent.obtain(time,
                                time,
                                MotionEvent.ACTION_DOWN,
                                300,
                                300,
                                0));
                        webView.dispatchTouchEvent(MotionEvent.obtain(time,
                                time,
                                MotionEvent.ACTION_MOVE,
                                300,
                                300 + interval,
                                0));
                        webView.dispatchTouchEvent(MotionEvent.obtain(time,
                                time,
                                MotionEvent.ACTION_UP,
                                300,
                                300 + interval,
                                0));
                    }
                    txtStatus.setText("Motion detected");

                }
            }

            @Override
            public void onTooDark() {
                txtStatus.setText("Too dark here");
            }
        });
//onCreate()
    }
    class MyWebClient extends WebViewClient{
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            view.loadUrl(url);
            return true;
        }
    }

    private class TimerHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            switch (msg.what) {
                case MESSAGE_TIMER_START:
                    if(!toggleButton.isChecked())
                        toggleButton.setChecked(true);

                    this.sendEmptyMessageDelayed(MESSAGE_TIMER_START,3000);
                    break;

            }

        }
    }
    public void make_Touch(String msg){
        long time;
//        webView.dispatchTouchEvent(MotionEvent.obtain(time,
//                time+100,
//                MotionEvent.ACTION_DOWN,
//                300,
//                300,
//                0));
//
//        webView.dispatchTouchEvent(MotionEvent.obtain(time,
//                time+2000,
//                MotionEvent.ACTION_POINTER_DOWN,
//                200,
//                200,
//                0));
//        webView.dispatchTouchEvent(MotionEvent.obtain(time+100,
//                time+2000,
//                MotionEvent.ACTION_MOVE,
//                900,
//                900,
//                0));
//        //
//        webView.dispatchTouchEvent(MotionEvent.obtain(time+2000,
//                time+2100,
//                MotionEvent.ACTION_UP,
//                900,
//                900,
//                0));
//        webView.dispatchTouchEvent(MotionEvent.obtain(time+2000,
//                time+2100,
//                MotionEvent.ACTION_POINTER_UP,
//                200,
//                200,
//                0));
//        Toast.makeText(getApplicationContext(),"ACTION MOVE!!!!!",Toast.LENGTH_SHORT).show();
//        return;
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        if(msg.contains("터치")) {
            String[] array = msg.split(" ");
            if (array.length != 3) return;
            int x,y;
            x=y=0;
            if(msg.contains("백")) {
                x = 100; y=100;
            }
            else if(!CheckNumber(array[1]) || !CheckNumber(array[2])){return;}

            if(x!=100 && y!=100) {
                x = Integer.parseInt(array[1]);
                y = Integer.parseInt(array[2]);
            }
            if (x < 0 || x > 1000 || y < 0 || y > 1000) return;
            //Toast.makeText(getApplicationContext(), Integer.toString(x) + " " + Integer.toString(y) + "\n", Toast.LENGTH_SHORT).show();
            webView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_DOWN,
                    x,
                    y,
                    0));
            webView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP,
                    x,
                    y,
                    0));
        }else if(msg.contains("아래로 드래그")) {
            String[] array = msg.split(" ");
            Toast.makeText(getApplicationContext(),"" + SystemClock.currentThreadTimeMillis(),Toast.LENGTH_SHORT).show();
            time = SystemClock.uptimeMillis();
            int interval;
            if (array.length == 3) {
                if (!CheckNumber(array[2])) return;
                interval = Integer.parseInt(array[2]);
                if (interval < 0 || interval > 1000) return;
            }else if(array.length==2){
                interval=300;
            }else return;
            webView.dispatchTouchEvent(MotionEvent.obtain(time,
                    time,
                    MotionEvent.ACTION_DOWN,
                    300,
                    1000,
                    0));
            webView.dispatchTouchEvent(MotionEvent.obtain(time,
                    time,
                    MotionEvent.ACTION_MOVE,
                    300,
                    1000-interval,
                    0));
            webView.dispatchTouchEvent(MotionEvent.obtain(time,
                    time,
                    MotionEvent.ACTION_UP,
                    300,
                    1000-interval,
                    0));

        }else if(msg.contains("위로 드래그")){
            String[] array = msg.split(" ");
            time = SystemClock.uptimeMillis();
            int interval;
            if (array.length == 3) {
                if (!CheckNumber(array[2])) return;
                interval = Integer.parseInt(array[2]);
                if (interval < 0 || interval > 1000) return;
            }else if(array.length==2){
                interval=300;
            }else return;
            webView.dispatchTouchEvent(MotionEvent.obtain(time,
                    time,
                    MotionEvent.ACTION_DOWN,
                    300,
                    50,
                    0));
            webView.dispatchTouchEvent(MotionEvent.obtain(time,
                    time,
                    MotionEvent.ACTION_MOVE,
                    300,
                    50+interval,
                    0));
            webView.dispatchTouchEvent(MotionEvent.obtain(time,
                    time,
                    MotionEvent.ACTION_UP,
                    300,
                    50+interval,
                    0));

        }
        else if(msg.contains("매트릭스")){

            String[] array = msg.split(" ");
            if (array.length != 2) return;
            int interval = Integer.parseInt(array[1]);

            if(interval==100) {
                if (FL_matrix100.getVisibility() == View.VISIBLE) {
                    FL_matrix100.setVisibility(View.INVISIBLE);
                } else {
                    FL_matrix100.setVisibility(View.VISIBLE);
                }
            }
            else if(interval==50){
                if (FL_matrix50.getVisibility() == View.VISIBLE) {
                    FL_matrix50.setVisibility(View.INVISIBLE);
                } else {
                    FL_matrix50.setVisibility(View.VISIBLE);
                }
            }
//            if(msg.contains("보여")) {
//                FL_matrix.setVisibility(View.VISIBLE);
//            }else if(msg.contains("없애")){
//                FL_matrix.setVisibility(View.INVISIBLE);
//            }
        }else if(msg.contains("키보드") || msg.contains("keyboard")){
            //onSearchRequested();
            String[] array = msg.split(" ");
            if(array.length!=2) return;
            String srchWord = array[0];
            for(int i=0;i<srchWord.length();i++){
                makeKeyEvent(srchWord.charAt(i));
            }
            //webView.dispatchKeyEvent(new KeyEvent(SystemClock.uptimeMillis()+500,SystemClock.uptimeMillis() + 1000,KeyEvent.ACTION_UP,KeyEvent.KEYCODE_ASSIST,0));
        }else if(msg.contains("뒤로 가기") || msg.contains("뒤로가")){
            if(webView.canGoBack()){
                webView.goBack();
            }
        }else if(msg.contains("앞으로 가기") || msg.contains("앞으로가")){
            if(webView.canGoForward()){
                webView.goForward();
            }
        }
        else if(msg.contains("검색")){
            String[] array = msg.split(" ");
            if (array.length != 2) return;
            String keyword = array[0];
            webView.loadUrl("https://www.google.com/search?q="+keyword);
        }else if(msg.contains("모션")){
            isMotionPossible=!isMotionPossible;
        }else if(msg.contains("커스텀")){
            isMotionUnder=!isMotionUnder;
        }else if(msg.contains("유튜브") || msg.contains("youtube")){
            String[] array = msg.split(" ");
            if (array.length != 2) return;
            String keyword = array[0];
            webView.loadUrl("https://www.youtube.com/results?search_query="+keyword);
        }else if(msg.contains("네이버") || msg.contains("youtube")){
            String[] array = msg.split(" ");
            if (array.length != 2) return;
            String keyword = array[0];
            webView.loadUrl("https://search.naver.com/search.naver?sm=top_hty&fbm=1&ie=utf8&query="+keyword);
        }


    }
    public void makeKeyEvent(char Input){
        if(Input =='a' || Input =='A'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A));
        }else if(Input =='b'|| Input =='B'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_B));
        }else if(Input =='c'|| Input =='C'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_C));
        }else if(Input =='d'|| Input =='D'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_D));
        }else if(Input =='e'|| Input =='E'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_E));
        }else if(Input =='f'|| Input =='F'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F));
        }else if(Input =='g'|| Input =='G'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_G));
        }else if(Input =='h'|| Input =='H'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_H));
        }else if(Input =='i'|| Input =='I'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_I));
        }else if(Input =='j'|| Input =='J'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_J));
        }else if(Input =='k'|| Input =='K'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_K));
        }else if(Input =='l'|| Input =='L'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_L));
        }else if(Input =='m'|| Input =='M'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_M));
        }else if(Input =='n'|| Input =='N'){
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_N));
        }else if(Input =='o'|| Input =='O') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_O));
        }else if(Input =='p'|| Input =='P') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_P));
        }else if(Input =='q'|| Input =='Q') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Q));
        }else if(Input =='r'|| Input =='R') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_R));
        }else if(Input =='s'|| Input =='S') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_S));
        }else if(Input =='t'|| Input =='T') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_T));
        }else if(Input =='u'|| Input =='U') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_U));
        }else if(Input =='v'|| Input =='V') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_V));
        }else if(Input =='w'|| Input =='W') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_W));
        }else if(Input =='x'|| Input =='X') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_X));
        }else if(Input =='y'|| Input =='Y') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Y));
        }else if(Input =='z'|| Input =='Z') {
            webView.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z));
        }
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        returnedText.setText(errorMessage);
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//        String text = "";
//        for (String result : matches)
//            text += result + "\n";
        text.setText(matches.get(0));
        toast.show();
        //returnedText.setText(matches.get(0));
        make_Touch(matches.get(0));
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    public boolean CheckNumber(String str){
        char check;

        if(str.equals(""))
        {
            //문자열이 공백인지 확인
            return false;
        }

        for(int i = 0; i<str.length(); i++){
            check = str.charAt(i);
            if( check < 48 || check > 58)
            {
                //해당 char값이 숫자가 아닐 경우
                return false;
            }

        }
        return true;
    }
    //Motion Parts
    @Override
    protected void onResume() {
        super.onResume();
        motionDetector.onResume();

        if (motionDetector.checkCameraHardware()) {
            txtStatus.setText("Camera found");
        } else {
            txtStatus.setText("No camera available");
        }
    }

    @Override
    protected void onPause() {
        motionDetector.onPause();
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }



}
