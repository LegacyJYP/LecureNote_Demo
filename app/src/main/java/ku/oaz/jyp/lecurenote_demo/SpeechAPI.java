package ku.oaz.jyp.lecurenote_demo;

/**
 * Created by JYP on 2016. 12. 22..
 */

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import java.io.InputStream;
import java.util.ArrayList;

import io.grpc.ManagedChannel;

public class SpeechAPI {
    private static final String HOSTNAME = "speech.googleapis.com";
    private static final int PORT = 443;
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private InputStream Credentials;
    private Context AppContext;

    private AudioRecord mAudioRecord = null;
    private Thread mRecordingThread = null;
    private boolean mIsRecording = false;
    private StreamingRecognizeClient mStreamingClient;
    private int mBufferSize;

    private String status = null;
    private ArrayList<String> results = new ArrayList<String>();


    public SpeechAPI() {
        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat
                .CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                mBufferSize);

    }

    public StreamingRecognizeClient getStreamingClient() {
        return this.mStreamingClient;
    }

    public void setCredentials(InputStream credentials)
    {
        this.Credentials = credentials;
    }
    public void setAppContext(Context appcontext){
        this.AppContext =  appcontext;
    }


    public void record() {
        if (mIsRecording) {
            mIsRecording = false;
            mAudioRecord.stop();
            mStreamingClient.finish();
            status = "시작합니다";
            Log.i(getClass().getSimpleName(), "Recording Finished.");
        } else {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                status = "종료가능";
                startRecording();
                Log.i(getClass().getSimpleName(), "Recording Started.");
            } else {
                Log.i(getClass().getSimpleName(), "Not Initialized yet.");
            }
        }
    }


    private void startRecording() {
        mAudioRecord.startRecording();
        mIsRecording = true;
        mRecordingThread = new Thread(new Runnable() {
            public void run() {
                readData();
            }
        }, "AudioRecorder Thread");
        mRecordingThread.start();
    }

    private void readData() {
        byte sData[] = new  byte[mBufferSize];
        while (mIsRecording) {
            int bytesRead = mAudioRecord.read(sData, 0, mBufferSize);
            if (bytesRead > 0) {
                try {
                    mStreamingClient.recognizeBytes(sData, bytesRead);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(getClass().getSimpleName(), "Error while reading bytes: " + bytesRead);
            }
        }
    }

    public void initialize() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // Required to support Android 4.x.x (patches for OpenSSL from Google-Play-Services)
                try {
                    ProviderInstaller.installIfNeeded(AppContext);
                } catch (GooglePlayServicesRepairableException e) {

                    // Indicates that Google Play services is out of date, disabled, etc.
                    e.printStackTrace();
                    // Prompt the user to install/update/enable Google Play services.
                    GooglePlayServicesUtil.showErrorNotification(
                            e.getConnectionStatusCode(), AppContext);
                    return;

                } catch (GooglePlayServicesNotAvailableException e) {
                    // Indicates a non-recoverable error; the ProviderInstaller is not able
                    // to install an up-to-date Provider.
                    e.printStackTrace();
                    return;
                }

                try {
                    //InputStream credentials = getAssets().open("speechAPI-500dcd00c1a2.json");
                    ManagedChannel channel = StreamingRecognizeClient.createChannel(
                            HOSTNAME, PORT, Credentials);
                    mStreamingClient = new StreamingRecognizeClient(channel, RECORDER_SAMPLERATE);
                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "Error", e);
                }
            }
        }).start();
    }

    protected void destroy() {
        if (mStreamingClient != null) {
            try {
                mStreamingClient.shutdown();
            } catch (InterruptedException e) {
                Log.e(MainActivity.class.getSimpleName(), "Error", e);
            }
        }
    }
}