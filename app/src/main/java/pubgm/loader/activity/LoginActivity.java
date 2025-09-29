package pubgm.loader.activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pubgm.loader.R;
import pubgm.loader.utils.ActivityCompat;
import pubgm.loader.utils.FLog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class LoginActivity extends ActivityCompat {
    
    static {
        try {
                System.loadLibrary("client");
        } catch(UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }
    
    private static final String USER = "USER";
    public static String USERKEY;
    private CardView btnSignIn;
    private WebView mWebView;
    
    public static void goLogin(Context context) {
        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(i);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isLogin = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitView();
    }
    
    private void InitView() {
    	final Context m_Context = this;
        final TextView textUsername = findViewById(R.id.textUsername);
        String userKey = prefs.read(USER, "");
        if (userKey != null) {
            textUsername.setText(userKey);
        }
        
        //textUsername.setText("dwvLucKo72i8EV4bp3WCTMIFH");
        
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textUsername = findViewById(R.id.textUsername);
                if (!textUsername.getText().toString().isEmpty()) {
                    prefs.write(USER, textUsername.getText().toString());
                    String userKey = textUsername.getText().toString().trim();
                    Login(LoginActivity.this, userKey);
                    USERKEY = userKey;
                }
                if (textUsername.getText().toString().isEmpty()) {
                    textUsername.setError("Please enter Licence Keys");
                }
                if (textUsername.getText().toString().isEmpty()) {
                    textUsername.setError("Please enter Licence Keys");
                }
            }
        });


        final  ImageView paste = findViewById(R.id.icpaste);
        final ImageView showpwd = findViewById(R.id.show_pwd);
        final ImageView pler = findViewById(R.id.vis_pwd);

        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                String ed = clipboard.getText().toString();
                textUsername.setText(ed);
            }
        });


        pler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showpwd.setVisibility(View.VISIBLE);
                pler.setVisibility(View.GONE);
                textUsername.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            }
        });

        showpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showpwd.setVisibility(View.GONE);
                pler.setVisibility(View.VISIBLE);
                textUsername.setTransformationMethod(PasswordTransformationMethod.getInstance());

            }
        });



        TextView getKey = findViewById(R.id.GetKey);
        getKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(GetKey()));
                startActivity(intent);
            }
        });

        /*if (!userKey.isEmpty()) {
            btnSignIn.performClick();
        }*/
        
        initViewPP();
        
        findViewById(R.id.pp).setOnClickListener(v -> {
            findViewById(R.id.lay_login).setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        });
    }
    
    private void initViewPP() {
    	mWebView = findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new HelloWebViewClient());
        mWebView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            Uri source = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(source);
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading File...");
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.enqueue(request);
            toastImage(R.drawable.ic_new_update, "Downloading File");
        });
        mWebView.loadUrl("https://www.app-privacy-policy.com/live.php?token=8FB0KO5pVZoRZlT9r3jWwH1dcKCeKWQb"); //Replace The Link Here
    }
    
    private static class HelloWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url)
        {
            view.loadUrl(url);
            return true;
        }
    }
    
    private static void Login(final LoginActivity m_Context, final String userKey) {
        final ProgressDialog progressDialog = new ProgressDialog(m_Context, 5);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Handler loginHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    MainActivity.goMain(m_Context);
                    m_Context.toastImage(R.drawable.ic_check, "Login Success");
                    m_Context.finishActivity(0);
                } else if (msg.what == 1) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(m_Context);
                    builder.setTitle("Information");
                    builder.setMessage(msg.obj.toString());
                    builder.setCancelable(false);
                    m_Context.toastImage(R.drawable.ic_error, "Login Failed");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                }
                progressDialog.dismiss();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = Check(m_Context, userKey);
                if (result.equals("OK")) {
                    loginHandler.sendEmptyMessage(0);
                } else {
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = result;
                    loginHandler.sendMessage(msg);
                }
            }
        }).start();
    }


    private static native String Check(Context mContext, String userKey);

    private native String GetKey();
    
    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            mWebView.setVisibility(View.GONE);
            findViewById(R.id.lay_login).setVisibility(View.VISIBLE);
        }
    }
    
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            OverlayPermision();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            InstllUnknownApp();
        } else if (requestCode == REQUEST_MANAGE_UNKNOWN_APP_SOURCES) {
            if (!isPermissionGaranted()) {
                takeFilePermissions();
            }
        }
    }
    
}
