package com.appliqant.quantilus.appliqant;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.*;

import java.nio.charset.MalformedInputException;

import static constants.Constants.REQUEST_CAMERA_PERMISSION;
import static constants.Constants.URL_PARAM;


public class MainActivity extends AppCompatActivity {

    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        myWebView = (WebView)findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        checkIntent();
        //myWebView.setWebViewClient(new WebViewClient()); //prevent URL to open in new tab
        setUpWebViewDefaults();
        myWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

    }
    //@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUpWebViewDefaults() {
        WebSettings settings = myWebView.getSettings();

        // Enable Javascript
        settings.setJavaScriptEnabled(true);

        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);

        // Allow use of Local Storage
        settings.setDomStorageEnabled(true);

        /*if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }*/
        myWebView.setWebViewClient(new WebViewClient());

        // AppRTC requires third party cookies to work
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(myWebView, true);
        cookieManager.setAcceptCookie(true);
    }


    //TODO: Due to time contrainsts, app is limited to WebView only
    private void checkIntent(){
        if(getIntent().getData()!=null){//check if intent is not null
            String url = getIntent().getDataString();
            Bundle extras = new Bundle();
            extras.putString(URL_PARAM, url);

            Intent i = new Intent(MainActivity.this, TakeInterview.class);
            i.putExtras(extras);
            startActivity(i);

        }
        else{//else open main page
            String url = "https://appliqantapp.com";
            myWebView.loadUrl(url);
            System.out.println("Going to url: " + url);
        }
    }

    @Override
    public void onBackPressed() {
        if(myWebView.canGoBack()){
            myWebView.goBack();
        }
        else{
            super.onBackPressed();
        }
    }
}
