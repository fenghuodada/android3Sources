package com.example.chapter15;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

public class TbsWebpageActivity extends AppCompatActivity {
    private final static String TAG = "TbsWebpageActivity";
    private LinearLayout ll_top;
    private LinearLayout ll_bottom;
    private EditText et_web_url; // 声明一个用于输入网址的编辑框对象
    private WebView wv_web; // 声明一个网页视图对象
    private ProgressDialog mDialog; // 声明一个进度对话框对象
    private String mUrl="http://www.szzg.gov.cn/2020/ttxw/202103/t20210319_5552739.htm"; // 完整的网页地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tbs_webpage);
        ll_top = findViewById(R.id.ll_top);
        ll_bottom = findViewById(R.id.ll_bottom);
        et_web_url = findViewById(R.id.et_web_url);
        et_web_url.setText(mUrl);
        // 从布局文件中获取名叫wv_web的网页视图
        wv_web = findViewById(R.id.wv_web);
        findViewById(R.id.iv_go).setOnClickListener(v -> openWebpage());
        findViewById(R.id.iv_back).setOnClickListener(v -> {
            if (wv_web.canGoBack()) { // 如果能够后退
                wv_web.goBack(); // 回到上一个网页
            } else {
                Toast.makeText(this, "已经是最后一页了", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.iv_forward).setOnClickListener(v -> {
            if (wv_web.canGoForward()) { // 如果能够前进
                wv_web.goForward(); // 去往下一个网页
            } else {
                Toast.makeText(this, "已经是最前一页了", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.iv_refresh).setOnClickListener(v -> wv_web.reload());
        findViewById(R.id.iv_close).setOnClickListener(v -> finish());
        initWebViewSettings(); // 初始化网页视图的网页设置
        MainApplication.getInstance().qbSdkInit(); // 初始化TBS服务
    }

    // 初始化网页视图的网页设置
    private void initWebViewSettings() {
        // 获取网页视图的网页设置
        WebSettings settings = wv_web.getSettings();
        // 设置是否自动加载图片。该方法在x5上失效
        settings.setLoadsImagesAutomatically(true);
        // 设置默认的文本编码
        settings.setDefaultTextEncodingName("utf-8");
        // 设置是否支持Javascript
        settings.setJavaScriptEnabled(true);
        // 设置是否允许js自动打开新窗口（window.open()）
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        // 设置是否支持缩放
        settings.setSupportZoom(true);
        // 设置是否出现缩放工具
        settings.setBuiltInZoomControls(true);
        // 当容器超过页面大小时，是否放大页面大小到容器宽度
        settings.setUseWideViewPort(true);
        // 当页面超过容器大小时，是否缩小页面尺寸到页面宽度
        settings.setLoadWithOverviewMode(true);
        // 设置自适应屏幕。4.2.2及之前版本自适应时可能会出现表格错乱的情况
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 开启会话存储功能
        settings.setDomStorageEnabled(true);
    }

    private void openWebpage() {
        // 从系统服务中获取输入法管理器
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        // 关闭输入法软键盘
        imm.hideSoftInputFromWindow(et_web_url.getWindowToken(), 0);
        mUrl = et_web_url.getText().toString();
        Log.d(TAG, "url=" + mUrl);
        // 命令网页视图加载指定路径的网页
        wv_web.loadUrl(mUrl);
        // 给网页视图设置自定义的网页浏览客户端
        wv_web.setWebViewClient(mWebViewClient);
        // 给网页视图设置自定义的网页交互客户端
        wv_web.setWebChromeClient(mWebChrome);
        // 给网页视图设置文件下载监听器
        wv_web.setDownloadListener(mDownloadListener);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
    }

    // 在按下返回键时触发
    @Override
    public void onBackPressed() {
        Log.d(TAG, "getUrl="+wv_web.getUrl());
        if (wv_web.canGoBack() && !wv_web.getUrl().equals(mUrl)) { // 还能返回到上一个网页
            wv_web.goBack(); // 回到上一个网页
        } else { // 已经是最早的网页，无路返回了
            finish(); // 关闭当前页面
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wv_web != null) {
            // 再次打开页面时，若界面没有消亡，会导致进度条不显示并且界面崩溃
            wv_web.stopLoading();
            wv_web.onPause();
//            wv_web.clearCache(true);
//            wv_web.clearHistory();
            wv_web.removeAllViews();
            wv_web.destroyDrawingCache();
            ViewGroup parent = (ViewGroup) wv_web.getParent();
            if (parent != null) {
                parent.removeView(wv_web);
            }
            wv_web.removeAllViews();
            wv_web.destroy();
        }
    }

    // 定义一个网页浏览客户端
    private WebViewClient mWebViewClient = new WebViewClient() {
        // 收到SSL错误时触发
        @Override
        public void onReceivedSslError(WebView view,
                                       SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }

        // 页面开始加载时触发
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "onPageStarted:" + url);
            if (mDialog == null || !mDialog.isShowing()) {
                // 下面弹出提示网页正在加载的进度对话框
                mDialog = new ProgressDialog(TbsWebpageActivity.this);
                mDialog.setTitle("稍等");
                mDialog.setMessage("页面加载中……");
                mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mDialog.show(); // 显示进度对话框
            }
        }

        // 页面加载结束时触发
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished:" + url);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss(); // 关闭进度对话框
            }
        }

        // 收到错误信息时触发
        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d(TAG, "onReceivedError: url=" + failingUrl + ", errorCode=" + errorCode + ", description=" + description);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss(); // 关闭进度对话框
            }
            Toast.makeText(TbsWebpageActivity.this,
                    "页面加载失败，请稍候再试", Toast.LENGTH_LONG).show();
        }

        // 发生网页跳转时触发
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url); // 在当前的网页视图内部跳转
            return true;
        }
    };

    // 定义一个网页交互客户端
    private WebChromeClient mWebChrome = new WebChromeClient() {
        private View mView;

        // 页面加载进度发生变化时触发
        @Override
        public void onProgressChanged(WebView view, int progress) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.setProgress(progress); // 更新进度对话框的加载进度
            }
        }

        // 网页请求定位权限时触发
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissionsCallback callback) {
            callback.invoke(origin, true, false); // 不弹窗就允许网页获得定位权限
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        // 点击网页的视频全屏按钮，会触发该方法
        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {
            super.onShowCustomView(view, customViewCallback);
            ViewGroup parent = (ViewGroup) wv_web.getParent();
            parent.removeView(wv_web);
            view.setBackgroundColor(Color.BLACK);
            parent.addView(view);
            mView = view;
            showFullScreen(); // 显示全屏
        }

        // 点击网页的退出全屏按钮，会触发该方法
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mView != null) {
                ViewGroup parent = (ViewGroup) mView.getParent();
                parent.removeView(mView);
                parent.addView(wv_web);
                mView = null;
                quitFullScreen(); // 退出全屏
            }
        }
    };

    // 定义一个文件下载监听器
    private DownloadListener mDownloadListener = (url, userAgent, contentDisposition, mimetype, contentLength) -> {
        // 此处操作文件下载
    };

    // 显示全屏
    private void showFullScreen(){
        ll_top.setVisibility(View.GONE);
        ll_bottom.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    // 退出全屏
    private void quitFullScreen() {
        ll_top.setVisibility(View.VISIBLE);
        ll_bottom.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

}