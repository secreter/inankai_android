package cn.redream.www.redream;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class TvplaerActivity extends AppCompatActivity implements View.OnClickListener{
    private WebView webView;
    private String link;
    long firstClickTime = 0;
    long secondClickTime = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvplaer);
        link=getIntent().getStringExtra("link");
        webView= (WebView) findViewById(R.id.web_tvplayer);
        webView.loadUrl(link);
        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
//  下面这一行保留的时候，原网页仍报错，新网页正常.所以注释掉后，也就没问题了
//          view.loadUrl(url);
                return true;
            }

        });
        webView.setOnClickListener(this);
    }
    @Override
    protected void onPause ()
    {
        webView.reload();

        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (firstClickTime > 0) {
            secondClickTime = SystemClock.uptimeMillis();
            if (secondClickTime - firstClickTime < 500) {
                if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                Toast.makeText(this, "第一种双击方式", Toast.LENGTH_SHORT).show();
            }
            firstClickTime = 0;
            return ;
        }
        firstClickTime = SystemClock.uptimeMillis();

        new Thread(new Runnable() {

            @Override
            public void run() {
                //
                try {
                    Thread.sleep(500);
                    firstClickTime = 0;
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();

    }
}
