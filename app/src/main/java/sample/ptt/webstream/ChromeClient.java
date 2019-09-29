package sample.ptt.webstream;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;

public class ChromeClient extends WebChromeClient {

    public static final int PERMISSION_AUDIO = 5;

    private final Context context;
    private PermissionRequest request;

    public ChromeClient(Context context){
        this.context = context;
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        super.onPermissionRequest(request);

        this.request = request;

        for(String permission : request.getResources()) {

            if(permission.equals(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {

                askForPermission(request, Manifest.permission.RECORD_AUDIO, PERMISSION_AUDIO);
                break;
            }
        }
    }

    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        super.onPermissionRequestCanceled(request);
        System.out.println("Permission request canceled!");
    }

    public void askForPermission(final PermissionRequest request, String permission, int requestCode) {

        this.request = request;

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    permission)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                System.out.println("needs to show explanation!");

                new Handler(Looper.getMainLooper()).post(()->{
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity)context,
                                    new String[]{permission},
                                    requestCode);
                        }
                    };

                    final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setMessage("This app needs permission to access your microphone")
                            .setPositiveButton("Ok", dialogClickListener);

                    builder.create().show();
                });

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{permission},
                        requestCode);
            }

        }
    }

    public void grant(){
        if(request!= null) {
            request.grant(request.getResources());
        }
    }
}
