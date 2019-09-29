package sample.ptt.nativestream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import okhttp3.WebSocket;
import okio.ByteString;

/**
 * Streams audio over a websocket connection.
 *
 * @author Merve Sahin
 */
public class AudioStream {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNELS = 2;
    private static final int ENCODING = 16;

    private AudioRecord recorder;
    private volatile boolean hasStopped;
    private final WebSocket socket;

    public AudioStream(WebSocket socket){
        this.socket = socket;
        initRecorder();
    }

    public void start(){
        hasStopped = false;
        recorder.startRecording();

        new Thread(()->{

            socket.send("started");

            while(!hasStopped){
                short[] data = new short[6144];
                recorder.read(data, 0, data.length, AudioRecord.READ_BLOCKING);

                byte[] output = new byte[data.length*2];
                ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(data);
                socket.send(ByteString.of(output));
            }

        }).start();
    }

    public void stop(){
        hasStopped = true;
        recorder.stop();
        socket.send("stopped");
    }


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
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, channel, encoding, min);
    }
}
