package ku.oaz.jyp.lecurenote_demo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.cloud.speech.v1beta1.StreamingRecognizeResponse;
import com.google.protobuf.TextFormat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by JYP on 2016. 12. 21..
 */

public class Recorder {
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static String AUDIO_RECORDER_FOLDER = "LeNoteJYP";
    public static String AUDIO_RECORDING_FILE_NAME = "recording";
    public static String AUDIO_RECORDING_FILE_EXT = ".raw";
    private ArrayList<String> filelist = new ArrayList<String>();

    private AudioRecord mAudioRecord = null;
    private Thread mRecordingThread = null;
    private boolean mIsRecording = false;
    private int mBufferSize;
    private String cur_filename = null;
    private ArrayList<String> results = new ArrayList<String>();

    private StreamingRecognizeClient mStreamingClient;


    Recorder()
    {
        this.initialize();
    }

    public void setFilename(String filename){
        this.AUDIO_RECORDING_FILE_NAME = filename;
    }

    void initialize() {
        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat
                .CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                   RECORDER_SAMPLERATE,
                                   RECORDER_CHANNELS,
                                   RECORDER_AUDIO_ENCODING,
                                   mBufferSize);
    }

    public void setStreamingClient(StreamingRecognizeClient mstreamingclient){
        this.mStreamingClient = mstreamingclient;
        this.mStreamingClient.setHandler(mHandler);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            pushResults((StreamingRecognizeResponse)msg.obj);
        }
    };

    private void pushResults(StreamingRecognizeResponse result_obj) {
        String result = TextFormat.printToString(result_obj);
        this.results.add(result);
    }

    public ArrayList<String> getResults(){
        return results;
    }

    void reset() {
        this.filelist.clear();
        this.results.clear();
    }

    void record() {
        if (mIsRecording) {
            mIsRecording = false;
            mAudioRecord.stop();
        } else {
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                startRecording();
            } else {
                Log.i(getClass().getSimpleName(), "Not Initialized yet.");
            }
        }
    }

    void stop() {
        if(mIsRecording) {
            mIsRecording = false;
            mAudioRecord.stop();
        }
    }

    private void startRecording() {
        mAudioRecord.startRecording();
        mIsRecording = true;
        mRecordingThread = new Thread(new Runnable() {
            public void run() {
                Log.i("JYP", "Thread run() in startRecording.");
                readData();
            }
        }, "AudioRecorder Thread");
        mRecordingThread.start();
    }


    private BufferedOutputStream openfile() {
        String path = Environment.getExternalStorageDirectory().getPath()
                + "/" + AUDIO_RECORDER_FOLDER;
        String LOGTAG = getClass().getSimpleName();
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int file_no = 0;
        String filePath = path + "/" + AUDIO_RECORDING_FILE_NAME + file_no +AUDIO_RECORDING_FILE_EXT;
        while(true) {
            File file = new File(filePath);
            if (file.exists()) {
                file_no++;
                filePath = path + "/" + AUDIO_RECORDING_FILE_NAME + file_no +AUDIO_RECORDING_FILE_EXT;
            }
            else {
                break;
            }
        }

        BufferedOutputStream os = null;

        try {
            os = new BufferedOutputStream(new FileOutputStream(filePath));
        } catch (FileNotFoundException e) {
            Log.e(LOGTAG, "File not found for recording ", e);
        }

        cur_filename = filePath;
        pushFilename();
        return os;
    }

    private void readData() {
        byte sData[] = new  byte[mBufferSize];
        BufferedOutputStream os = openfile();
        // Write the output audio in byte
        String LOGTAG = getClass().getSimpleName();


        while (mIsRecording) {
            Log.i(LOGTAG, "Writing...");
            int status = mAudioRecord.read(sData, 0, sData.length);

            if (status == AudioRecord.ERROR_INVALID_OPERATION ||
                  status == AudioRecord.ERROR_BAD_VALUE) {
                Log.e(LOGTAG, "Error reading audio data!");
                        return;
                }

                try {
                    this.mStreamingClient.recognizeBytes(sData, status);
                } catch (Exception e) {
                    e.printStackTrace();
                }//SpeechAPI

                try {
                    os.write(sData, 0, sData.length);
                } catch (IOException e) {
                    Log.e(LOGTAG, "Error saving recording ", e);
                    return;
                }
        }

        try {
            os.close();
        } catch (IOException e) {
            Log.e(LOGTAG, "Error when releasing", e);
        }

    }

    void pushFilename()
    {
        String LOGTAG = getClass().getSimpleName();
        filelist.add(cur_filename);
        Log.i(LOGTAG, cur_filename);
    }

    public ArrayList<String> getFilelist() { return filelist;}
}
