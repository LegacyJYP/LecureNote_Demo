package ku.oaz.jyp.lecurenote_demo;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by JYP on 16. 8. 25..
 */
public class Player {
    public static String AUDIO_RECORDING_FILE_NAME = "recording";
    private static String AUDIO_RECORDER_FOLDER = "LeNoteJYP";
    public static String AUDIO_RECORDING_FILE_EXT = ".raw";
    String filename = "Test";
    private ArrayList<String> wavfilelist = new ArrayList<String>();
    private boolean terminate_flag = false;

    private MediaPlayer mediaplayer;
    String path = Environment.getExternalStorageDirectory().getPath()
            + "/" + AUDIO_RECORDER_FOLDER;


    public Player() {
        this.mediaplayer = new MediaPlayer();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isplaying() {
        return this.mediaplayer.isPlaying();
    }

    public void reset() {
        this.wavfilelist.clear();
    }

    public void play() {
        String LOGTAG = getClass().getSimpleName();
        terminate_flag = false;
        if (wavfilelist.size()==0){
            Log.i(LOGTAG,"There's no filelist");
            return;
        }
        if (this.isplaying()) {
            Log.i(LOGTAG,"Already playing");
            return;
        }

        for (int idx = 0; idx< wavfilelist.size(); idx++ ){
            while(this.mediaplayer.isPlaying()) {
                if(terminate_flag)
                    return;
            }
            play(idx);
        }
    }

    public void play(int idx) {

        String LOGTAG = getClass().getSimpleName();
        if (this.isplaying()){
            Log.i(LOGTAG,"Already playing");
            return;}


        String ent_filename;
        if (wavfilelist.size() > 0){
            ent_filename = this.wavfilelist.get(idx);}
        else{
            Log.i(LOGTAG,"There's no filelist");
            return;
        }


        try {
            Log.i(LOGTAG,"To play : " + ent_filename);

            this.mediaplayer.reset();
            this.mediaplayer.setDataSource(ent_filename);
            this.mediaplayer.prepare();
            this.mediaplayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void convert(ArrayList<String> filelist) {
        if (filelist.size()==0){
            return;
        }
        wavfilelist.clear();
        for (int idx = 0; idx < filelist.size(); idx++){
            try {
                wavfilelist.add(convert_to_wav(filelist.get(idx)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static String convert_to_wav(String pcm_filename) throws IOException {
        Converter pcmtowav = new Converter();
        String wav_filename = extension_pcm_to_wav(pcm_filename);

        try {
            pcmtowav.convert_to_wav(pcm_filename, wav_filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("Recorder",wav_filename);
        return wav_filename;
    }

    public static String extension_pcm_to_wav(String pcm_path) {
        return pcm_path.replace(".raw", ".wav");
    }

    public void stop() {
        if(this.isplaying()) {
            terminate_flag = true;
            try {
                this.mediaplayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}