package sample.ptt.nativestream;

import android.media.AudioFormat;

import org.json.JSONException;
import org.json.JSONObject;

public class AudioStreamMetadata {

    public static final int DEFAULT_SAMPLE_RATE = 44100;
    public static final int DEFAULT_CHANNELS = 2;
    public static final int DEFAULT_ENCODING = 8;
    public static final int DEFAULT_BUFFER_SIZE = 6144*4;

    private final int sampleRate;
    private  int bufferSize;
    private final int channels;
    private final int encoding;
    private final int bytesPerSample;
    private int bufferSizeInBytes;

    public AudioStreamMetadata(int sampleRate, int bufferSize, int channels, int encoding) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
        this.channels = channels;
        this.encoding = encoding;
        this.bytesPerSample = encoding / 8;
        this.bufferSizeInBytes = bufferSize * bytesPerSample;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getChannels(boolean in) {
        if(channels == 1){
            return in? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_OUT_MONO;
        }else if(channels == 2){
            return in? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_OUT_STEREO;
        }else{
            return 0;
        }
    }

    public int getEncoding() {
        if(encoding == 8){
            return AudioFormat.ENCODING_PCM_8BIT;
        }else if(encoding == 16){
            return AudioFormat.ENCODING_PCM_16BIT;
        }else if(encoding == 32){
            return AudioFormat.ENCODING_PCM_FLOAT;
        }else{
            return 0;
        }
    }

    public int getBytesPerSample(){
        return this.bytesPerSample;
    }

    public int getBufferSizeInBytes() {
        return this.bufferSizeInBytes;
    }

    public void setBufferSize(int size){
        this.bufferSize = size;
        this.bufferSizeInBytes = bufferSize * bytesPerSample;
    }

    @Override
    public String toString() {
        JSONObject data = new JSONObject();
        try {
            JSONObject o = new JSONObject();
            o.put("sampleRate", sampleRate);
            o.put("bufferSize", bufferSize);
            o.put("channels", channels);
            o.put("encoding", encoding);

            data.putOpt("meta", o);
            return data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public static AudioStreamMetadata getDefault(){
        return new AudioStreamMetadata(DEFAULT_SAMPLE_RATE, DEFAULT_BUFFER_SIZE, DEFAULT_CHANNELS, DEFAULT_ENCODING);
    }
}
