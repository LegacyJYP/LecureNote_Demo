package ku.oaz.jyp.lecurenote_demo;

import android.media.AudioRecord;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    final String filename = "Test";
    Recorder recorder = new Recorder();
    Player player = new Player();

    private View m_btnRecord;
    private View m_btnStop;
    private View m_btnPlay;
    private View m_btnExit;
    private View m_btnReset;
    private TextView m_textResult;
    private TextView m_textStatus;
    private boolean thread_flag = false;
    private SpeechAPI speechapi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        //Git test

        recorder.setFilename(filename);
        player.setFilename(filename);

        initSpeechAPI();
    }

    private void initView()
    {
        m_btnRecord = findViewById(R.id.m_record);
        m_btnStop = findViewById(R.id.m_stop);
        m_btnPlay = findViewById(R.id.m_play);
        m_btnExit = findViewById(R.id.m_exit);
        m_btnReset = findViewById(R.id.m_reset);
        m_btnRecord.setOnClickListener(mClickListener);
        m_btnStop.setOnClickListener(mClickListener);
        m_btnPlay.setOnClickListener(mClickListener);
        m_btnExit.setOnClickListener(mClickListener);
        m_btnReset.setOnClickListener(mClickListener);

        m_textResult = (TextView)findViewById(R.id.m_resultText);
        m_textStatus = (TextView)findViewById(R.id.m_resultTime);
    }
    private void initSpeechAPI(){
        speechapi = new SpeechAPI();
        speechapi.setAppContext(this.getApplicationContext());
        try {
        speechapi.setCredentials(getAssets().open("speechAPI-500dcd00c1a2.json"));
        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(), "Error", e);
        }

        speechapi.initialize();
    }

    Button.OnClickListener mClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.m_record:
                    recorder.setStreamingClient(speechapi.getStreamingClient());
                    recorder.record();
//                    Thread mRecordingThread = new Thread(new Runnable() {
//                        public void run() {
//                            thread_flag = true;
//                            while(true) {
//
//                                ArrayList<String> results = recorder.getResults();
//                                String results_ar[] = results.toArray(new String[results.size()]);
//                                if (results.size() > 0)
//                                    Log.i("jyp",TextUtils.join("/", results_ar));
//                            }
//                        }
//                    }, "AudioRecorder Thread");
//                    if (!thread_flag)
//                        mRecordingThread.start();
                    break;
                case R.id.m_stop:
                    recorder.stop();
                    player.stop();
                    break;
                case R.id.m_reset:
                    recorder.reset();
                    player.reset();
                    break;
                case R.id.m_play:
                    player.convert(recorder.getFilelist());
                    player.play();
                    break;
                case R.id.m_exit:
                    finish();
                    break;
            }
        }
    };


}
