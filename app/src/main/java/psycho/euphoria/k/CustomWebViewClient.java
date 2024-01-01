package psycho.euphoria.k;

import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class CustomWebViewClient extends WebViewClient {


    private final MainActivity mContext;

    public CustomWebViewClient(MainActivity context) {
        mContext = context;
    }

    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return super.shouldInterceptRequest(view, url);

    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if ((url.startsWith("https://") || url.startsWith("http://") || url.startsWith("file://"))) {
            view.loadUrl(url);
        }
        return true;
    }

}