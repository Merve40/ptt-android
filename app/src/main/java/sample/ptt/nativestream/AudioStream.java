package sample.ptt.nativestream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

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

    private AudioStreamMetadata metadata;
    private AudioRecord recorder;
    private volatile boolean hasStopped;
    private final WebSocket socket;

    public AudioStream(WebSocket socket) {
        this.socket = socket;
        metadata = AudioStreamMetadata.getDefault();
        initRecorder();
    }

    public void start() {
        hasStopped = false;
        recorder.startRecording();

        new Thread(() -> {

            socket.send("started");
            socket.send(metadata.toString());

            while (!hasStopped) {

                if(metadata.getEncoding() == AudioFormat.ENCODING_PCM_8BIT){

                    byte[] data = new byte[metadata.getBufferSize()];
                    recorder.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
                    socket.send(ByteString.of(data));

                }else if(metadata.getEncoding() == AudioFormat.ENCODING_PCM_16BIT){

                    short[] data = new short[metadata.getBufferSize()];
                    recorder.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
                    byte[] output = new byte[data.length * metadata.getBytesPerSample()];
                    ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(data);
                    socket.send(ByteString.of(output));

                }else if(metadata.getEncoding() == AudioFormat.ENCODING_PCM_FLOAT){

                    float[] data = new float[metadata.getBufferSize()];
                    recorder.read(data, 0, data.length, AudioRecord.READ_BLOCKING);
                    byte[] output = new byte[data.length * metadata.getBytesPerSample()];
                    ByteBuffer.wrap(output).asFloatBuffer().put(data);
                    socket.send(ByteString.of(output));

                }else{
                    this.stop();
                    return;
                }
            }

        }).start();

    }

    public void stop() {
        hasStopped = true;
        recorder.stop();
        socket.send("stopped");
    }

    private void initRecorder() {
        int min = AudioRecord.getMinBufferSize(metadata.getSampleRate(), metadata.getChannels(true), metadata.getEncoding());
        metadata.setBufferSize(min);
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, metadata.getSampleRate(),
                metadata.getChannels(true), metadata.getEncoding(), min);
    }
}
