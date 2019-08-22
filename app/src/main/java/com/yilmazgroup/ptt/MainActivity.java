package com.yilmazgroup.ptt;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.yilmazgroup.ptt.web.ChromeClient;
import com.yilmazgroup.ptt.web.PttBrowser;
import com.yilmazgroup.ptt.web.AudioStreamer;

public class MainActivity extends AppCompatActivity {

    private WebView web;
    private CookieManager cookieManager;
    private PttBrowser browser;
    private ChromeClient chromeClient;


    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web = findViewById(R.id.web);

        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(web, true);

        browser = new PttBrowser();
        chromeClient = new ChromeClient(this);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setSupportZoom(false);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.getSettings().setAllowContentAccess(true);
        web.getSettings().setAllowFileAccessFromFileURLs(true);
        web.getSettings().setAllowUniversalAccessFromFileURLs(true);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setDatabaseEnabled(true);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        web.getSettings().setDisplayZoomControls(false);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setDefaultTextEncodingName("utf-8");

        WebView.setWebContentsDebuggingEnabled(true);

        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        web.clearCache(true);
        web.clearHistory();

        web.setWebViewClient(browser);
        web.setWebChromeClient(chromeClient);

        web.loadUrl("file:///android_asset/index.html");
        web.addJavascriptInterface(new AudioStreamer(web), "Android");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ChromeClient.PERMISSION_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    System.out.println("Permission granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    System.out.println("Permission denied!");
                }
            }
        }
    }

}
