package psycho.euphoria.k;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.List;


public class MainActivity extends Activity {
    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";
    private WebView mWebView;

    public static WebView initializeWebView(MainActivity context) {
        WebView webView = new WebView(context);
        webView.addJavascriptInterface(new WebAppInterface(context), "NativeAndroid");
        webView.setWebViewClient(new CustomWebViewClient(context));
        webView.setWebChromeClient(new CustomWebChromeClient(context));
        context.setContentView(webView);
        return webView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void setWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(USER_AGENT);
        settings.setSupportZoom(false);
    }

    private void openOverlaySettings() {
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("MainActivity", e.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean overlayEnabled = Settings.canDrawOverlays(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !overlayEnabled) {
            openOverlaySettings();
        }
//        List<InputMethodInfo> inputMethodInfos = inputMethodManager.getInputMethodList();
//
//        for (InputMethodInfo inputMethodInfo : inputMethodInfos) {
//            Log.e("B5aOx2", String.format("onCreate, %s", inputMethodInfo.getId()));
//            if (inputMethodInfo.getId().contains("pinyin")) {
//
//            }
//        }
        // psycho.euphoria.k/.InputService
//        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
//        final IBinder token = this.getWindow().getAttributes().token;
//        imm.setInputMethod(token, "psycho.euphoria.k/.InputService");
        //getSystemService(InputService.class).switchInputMethod("psycho.euphoria.k/.InputService");
        mWebView = initializeWebView(this);
        setWebView(mWebView);
        mWebView.loadUrl(String.format("http://%s:8500/app.html", Shared.getDeviceIP(this)));
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                mWebView.reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, 0, 0, "刷新");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
}