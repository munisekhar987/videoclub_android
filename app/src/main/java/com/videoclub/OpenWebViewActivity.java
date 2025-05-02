package com.videoclub;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

/**
 * OpenWebViewActivity for Android TV
 * Displays web content from URLs
 */
public class OpenWebViewActivity extends FragmentActivity {
    private static final String TAG = "OpenWebViewActivity";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView errorTextView;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_web_view);

        // Initialize views
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        errorTextView = findViewById(R.id.error_text);

        // Get URL from intent
        if (getIntent() != null && getIntent().getExtras() != null) {
            url = getIntent().getStringExtra("url");
        }

        if (url != null && !url.isEmpty()) {
            setupWebView();
            loadUrl(url);
        } else {
            showError("Invalid URL");
        }
    }

    /**
     * Configure WebView settings
     */
    private void setupWebView() {
        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        // Set WebView clients
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                showError("Error loading page: " + description);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        // Enable hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    /**
     * Load URL in WebView
     */
    private void loadUrl(String url) {
        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(url);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        webView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(message);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle back button press
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}