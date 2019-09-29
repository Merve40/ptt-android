package sample.ptt.nativestream;

import android.content.Context;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * Establishes connection to server.
 *
 * @author Merve Sahin
 */
public class PTT {

    public interface ConnectionListener{
        void onSuccess(PTTConnection connection);
        void onFailure(Exception e);
    }

    private static final String URL_LOGIN = "https://ptt-demo.herokuapp.com/login";
    private static final String URL_WEBSOCKET = "wss://ptt-demo.herokuapp.com/wss?id=";


    /**
     * Connects to the server and creates a {@link PTTConnection} object.
     *
     * @param context Context
     * @param button a button to control message flow
     * @param listener a listener which obtains the {@link PTTConnection} object on successful connection.
     */
    public static void getConnection(final Context context, final View button, final ConnectionListener listener){

        OkHttpClient client =  new OkHttpClient();

        Callback callback = new Callback() {

            String id;
            WebsocketListenerImpl websocketListener = new WebsocketListenerImpl(context, button);
            AudioStream audioStream;
            WebSocket webSocket;

            @Override
            public void onFailure(Call call, IOException e) {
                listener.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    JSONObject body = new JSONObject(response.body().string());

                    id = body.getString("id");

                    System.out.println("session id="+id);

                    Request reqWebsocket = new Request.Builder().url(URL_WEBSOCKET+id).build();
                    webSocket = client.newWebSocket(reqWebsocket, websocketListener);
                    audioStream = new AudioStream(webSocket);

                    PTTConnection connection = new PTTConnection(id, audioStream, client, webSocket);
                    listener.onSuccess(connection);

                } catch (JSONException | IOException e1) {
                    e1.printStackTrace();
                    listener.onFailure(e1);
                }
            }
        };

        Request login = new Request.Builder()
                .url(URL_LOGIN)
                .get()
                .build();
        Call callLogin = client.newCall(login);
        callLogin.enqueue(callback);
        System.out.println("Connecting to Server..");
    }
}
