package sample.ptt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import sample.ptt.nativestream.AudioStream;
import sample.ptt.nativestream.PTT;
import sample.ptt.nativestream.PTTConnection;

/**
 * @author Merve Sahin
 */
public class MainActivity2 extends AppCompatActivity implements View.OnTouchListener {

    private static String CHANNEL = "test";

    private Button button;
    private PTTConnection connection;
    private AudioStream audioStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        button = findViewById(R.id.ptt);
        button.setOnTouchListener(this);
        button.setEnabled(false);

        /**
         * Retrieves push-to-talk connection object
         */
        PTT.getConnection(this, button, new PTT.ConnectionListener() {
            @Override
            public void onSuccess(PTTConnection conn) {
                connection = conn;
                audioStream = connection.getAudioStream();
                connection.subscribe(CHANNEL);
                runOnUiThread(() ->{
                    button.setEnabled(true);
                });
                System.out.println("subscribed to channel '"+CHANNEL+"'");
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.getWebSocket().cancel();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            audioStream.start();

        }else if(event.getAction() == MotionEvent.ACTION_UP){
            audioStream.stop();
        }
        return false;
    }

}
