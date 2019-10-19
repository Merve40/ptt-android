package sample.ptt.nativestream;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

/**
 * Holds the connection to the web-server.
 * Communicates over websocket and http-requests.
 *
 * @author Merve Sahin
 */
public class PTTConnection {


    private static final String URL_SUBSCRIBE = "https://ptt-demo.herokuapp.com/subscribe?channel=";
    //private static final String URL_SUBSCRIBE = "https://wise-grasshopper-74.localtunnel.me/subscribe?channel=";

    private String id;
    private AudioStream audioStream;
    private OkHttpClient client;
    private WebSocket webSocket;

    private String channel;

    PTTConnection(String id, AudioStream audioStream, OkHttpClient client, WebSocket webSocket) {
        this.id = id;
        this.audioStream = audioStream;
        this.client = client;
        this.webSocket = webSocket;
    }

    /**
     * Returns unique session id assigned by the server.
     *
     * @return session id
     */
    public String getSessionId(){
        return id;
    }

    public AudioStream getAudioStream() {
        return audioStream;
    }

    public OkHttpClient getHttpClient() {
        return client;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    /**
     * Subscribes to another channel.
     *
     * @param channel new channel
     */
    public void subscribe(String channel){
        this.channel = channel;
        Request subscribe = new Request.Builder()
                .url(URL_SUBSCRIBE+channel+"&id="+id)
                .build();
        try {
            client.newCall(subscribe).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentChannel(){
        return channel;
    }


}
