package com.yilmazgroup.ptt.web;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Streams audio over a websocket within {@link WebView}.
 *
 * @author Merve Sahin
 */
public class AudioStreamer {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNELS = 2;
    private static final int ENCODING = 8;

    private final WebView web;
    private AudioRecord recorder;
    private volatile boolean hasStopped;

    public AudioStreamer(WebView web){
        this.web = web;
        hasStopped = false;
        initRecorder();
    }

    /**
     * Starts the audio streaming from javascript code.
     *
     * @param callbackFunc callback function to invoke for each stream.
     */
    @JavascriptInterface
    public void start(String callbackFunc){

        hasStopped = false;
        recorder.startRecording();

        new Thread(()->{

            while(!hasStopped){
                byte[] data = new byte[4096];
                recorder.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
                byte[] output = StreamUtil.toWAV(data, (short) CHANNELS, SAMPLE_RATE, (short) ENCODING);
                send(callbackFunc, output);
            }

        }).start();
    }

    /**
     * Stops audio stream.
     */
    @JavascriptInterface
    public void stop(){
        hasStopped = true;
        recorder.stop();
    }

    /**
     * Sends data over socket within webview.
     *
     * @param callback function, which is passed from javascript and will be invoked here
     * @param output the pcm output to send
     */
    private void send(String callback, byte[] output){
        try {
            String json = new JSONArray(output).toString();
            web.post(()->{

                String script = callback + "('" + json + "');";

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    web.evaluateJavascript(script, null);
                }else{
                    web.loadUrl("javascript:"+script);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes recorder with default configurations.
     */
    private void initRecorder(){

        int channel;

        if(CHANNELS == 1){
            channel = AudioFormat.CHANNEL_IN_MONO;
        }else{
            channel = AudioFormat.CHANNEL_IN_STEREO;
        }

        int encoding;

        if(ENCODING == 8){
            encoding = AudioFormat.ENCODING_PCM_8BIT;
        }else if(ENCODING == 16){
            encoding = AudioFormat.ENCODING_PCM_16BIT;
        }else{

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                encoding = AudioFormat.ENCODING_PCM_FLOAT;
            }else{
                encoding = AudioFormat.ENCODING_PCM_16BIT;
            }
        }

        int min = AudioRecord.getMinBufferSize(SAMPLE_RATE, channel, encoding);
        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, channel, encoding, min);
    }
}
