package de.udsmail.java.udsmail;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;

public class MailActivity extends Activity {

    private WebView mailView;
    private RelativeLayout back;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        back = (RelativeLayout) findViewById(R.id.back);
        img = (ImageView) findViewById(R.id.imageView);

        WebView mails = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = mails.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        mails.loadUrl("http://webmail.uni-saarland.de/");
        mails.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.w("[Main]", "Login");
                final String username = "???";
                final String password = "???";
                final String js = "javascript:" +
                        // "document.getElementsById('password').value = '"+ password +"';" +  // for phone
                        "document.getElementsByName('password')[0].value = '" + password + "';" +  // for tablet
                        "document.getElementById('username').value = '" + username + "';" +
                        "document.getElementsByName('login')[0].click()";
                view.loadUrl(js);
                view.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        mailView = view;
                        back.setVisibility(View.GONE);
                        img.setVisibility(View.GONE);
                    }
                });
            }
        });
        mails.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MailActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                }

                String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setTitle(filename);
                request.allowScanningByMediaScanner();
                request.setMimeType(mimetype);
                request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
                request.addRequestHeader("User-Agent", userAgent);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mailView.loadUrl("javasript: document.getElementsByClassName(\"smartmobile-logout ui-btn-right ui-btn " +
                "ui-btn-up-e ui-shadow ui-btn-corner-all ui-btn-icon-left\")[0].click()");
    }
}