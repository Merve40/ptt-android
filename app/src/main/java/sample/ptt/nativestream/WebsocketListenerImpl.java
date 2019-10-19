package sample.ptt.nativestream;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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
    private AudioStreamMetadata metadata;

    private volatile boolean hasStarted = false;

    public WebsocketListenerImpl(Context context, View button) {
        this.context = context;
        this.button = button;
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);

        if (text.equals("ping")) {
            webSocket.send("pong");
        } else if (text.equals("started")) {
            runOnUiThread(() -> {
                button.setEnabled(false);
            });
        } else if (text.equals("stopped")) {
            runOnUiThread(() -> {
                button.setEnabled(true);
            });
            audioTrack.stop();
            audioTrack = null;
            hasStarted = false;
        } else {

            try {
                JSONObject meta = new JSONObject(text).getJSONObject("meta");
                metadata = new AudioStreamMetadata(meta.getInt("sampleRate"), meta.getInt("bufferSize"),
                        meta.getInt("channels"), meta.getInt("encoding"));

                System.out.println(metadata.toString());
                initAudioTrack(metadata);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);

        int result;
        byte[] buffer = bytes.toByteArray();

        if (metadata.getEncoding() == AudioFormat.ENCODING_PCM_FLOAT) {

            FloatBuffer fb = ByteBuffer.wrap(buffer).asFloatBuffer();
            float[] out = new float[fb.capacity()];
            fb.get(out);
            result = audioTrack.write(out, 0, out.length, AudioTrack.WRITE_BLOCKING);

        } else{
            result = audioTrack.write(buffer, 0, buffer.length, AudioTrack.WRITE_BLOCKING);
        }

        if (result == AudioTrack.ERROR_BAD_VALUE) {
            System.out.println("ERROR: bad value");
        } else if (result == AudioTrack.ERROR_DEAD_OBJECT) {
            System.out.println("ERROR: dead object");
        } else if (result == AudioTrack.ERROR_INVALID_OPERATION) {
            System.out.println("ERROR: invalid operation");
        } else if (result == AudioTrack.ERROR) {
            System.out.println("ERROR: ??");
        }

        if (!hasStarted) {
            audioTrack.play();
            hasStarted = true;
        }
    }

    private void initAudioTrack(AudioStreamMetadata meta) {

        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFormat format = new AudioFormat.Builder()
                .setSampleRate(meta.getSampleRate())
                .setEncoding(meta.getEncoding())
                .setChannelMask(meta.getChannels(false))
                .build();

        int sessionId = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).generateAudioSessionId();
        audioTrack = new AudioTrack(aa, format, meta.getBufferSizeInBytes(), AudioTrack.MODE_STREAM, sessionId);
    }

    private void runOnUiThread(Runnable run) {
        new Handler(Looper.getMainLooper()).post(run);
    }
}
