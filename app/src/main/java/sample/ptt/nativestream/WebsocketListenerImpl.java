package sample.ptt.nativestream;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Listens to websocket events and handles live audio streaming.
 *
 * @author Merve Sahin
 */
public class WebsocketListenerImpl extends WebSocketListener {

    private final Context context;
    private final View button;
    private AudioTrack audioTrack;

    private volatile boolean hasStarted = false;

    public WebsocketListenerImpl(Context context, View button){
        this.context = context;
        this.button = button;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);

        System.out.println(text);

        if(text.equals("ping")){
            webSocket.send("pong");
        }else if(text.equals("started")){
            runOnUiThread(()->{
                button.setEnabled(false);
            });
            initAudioTrack();
        }else if(text.equals("stopped")){
            runOnUiThread(()->{
                button.setEnabled(true);
            });
            audioTrack.stop();
            audioTrack = null;
            hasStarted = false;
        }else{
            System.out.println("received message: "+text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);

        byte[] buffer = bytes.toByteArray();
        audioTrack.write(buffer, 0, buffer.length, AudioTrack.WRITE_BLOCKING);
        if(!hasStarted) {
            audioTrack.play();
            hasStarted = true;
        }
    }

    private void initAudioTrack(){
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setSampleRate(44100)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build();
        int bufferSize = 6144*2;

        int sessionId = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).generateAudioSessionId();
        audioTrack = new AudioTrack(aa, format, bufferSize, AudioTrack.MODE_STREAM, sessionId);
    }

    private void runOnUiThread(Runnable run){
        new Handler(Looper.getMainLooper()).post(run);
    }
}
