package pubgm.loader.utils;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.airbnb.lottie.LottieAnimationView;
import com.blankj.molihuan.utilcode.util.FileUtils;
import com.blankj.molihuan.utilcode.util.ToastUtils;
import pubgm.loader.BuildConfig;
import pubgm.loader.Config;
import pubgm.loader.BoxApplication;
import pubgm.loader.activity.CrashHandler;
import pubgm.loader.activity.MainActivity;
import pubgm.loader.libhelper.FileHelper;
import pubgm.loader.libhelper.Loader;
import pubgm.loader.service.MainService;
//import pubgm.loader.overlay.MenuFloatingView;
import java.io.IOException;
import android.os.Handler;
import android.app.ProgressDialog;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import pubgm.loader.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import android.content.Context;

import org.jdeferred.android.AndroidDeferredManager;

import pubgm.loader.libhelper.ApkEnv;
import static pubgm.loader.Config.USER_ID;
import static pubgm.loader.Config.GAME_LIST_PKG;
import static pubgm.loader.Config.GAME_LIST_ICON;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class ActivityCompat extends AppCompatActivity {
    private static ActivityCompat activityCompat;
    public static int REQUEST_OVERLAY_PERMISSION = 5469;
    public static int PERMISSION_REQUEST_STORAGE = 100;
    public static int REQUEST_MANAGE_UNKNOWN_APP_SOURCES = 200;
    public boolean isLogin = false;
    public FPrefs prefs;
    private BottomSheetDialog bottomSheetDialog;
    public FirebaseAuth currentAuth;
    public static String gamename;
    public static String name;
    public static int version;
    public static String url;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
   
    public static ActivityCompat getActivityCompat() {
        return activityCompat;
    }
    
    public FPrefs getPref() {
        return FPrefs.with(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityCompat = this;
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setNavBar(R.color.background);
        
        prefs = getPref();
        currentAuth = FirebaseAuth.getInstance();
        
        ManageFiles();
    }
    
    public void setNavBar(int color){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,color));
    }
    
    public void restartApp(String clazz) {
        Intent lauchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        lauchIntent.addFlags(335577088);
        lauchIntent.putExtra("restartApp", clazz);
        startActivity(lauchIntent);
        Runtime.getRuntime().exit(0);
    }
    
    public void toast(CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(R.mipmap.icon);
        //_toast.setGravity(Gravity.BOTTOM, Gravity.CENTER, Gravity.CENTER);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        //_toast.setBgResource(R.drawable.button_coming);
        _toast.show(msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
    public void toastImage(int id, CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(id);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        _toast.show(msg);
    }
    
    public void RestartAppp() {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void ShowRestartApp() {
        showBottomSheetDialog(
                getResources().getDrawable(R.drawable.ic_check),
                "Download Success: Restart Loader",
                "The loader has been downloaded successfully. Please restart the loader now.",
                false,
                v -> {
                    MainActivity.get().doShowProgress(true);
                    RestartAppp();
                    dismissBottomSheetDialog();
                },
                null);
    }
    
    public void takeFilePermissions() {
        new MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.file_access_title)
            .setMessage(R.string.file_access_message)
            .setPositiveButton(
                R.string.grant_permission,
                (d, w) -> {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                  } else {
                    androidx.core.app.ActivityCompat.requestPermissions(
                        this,
                        new String[] {
                          Manifest.permission.READ_EXTERNAL_STORAGE,
                          Manifest.permission.MANAGE_EXTERNAL_STORAGE
                        },
                        1);
                  }
                })
            .setNegativeButton(
                R.string.exit,
                (d, w) -> {
                  finish();
                  System.exit(0);
                })
            .show();
    }
    
    public boolean isPermissionGaranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          return Environment.isExternalStorageManager();
        } else {
          return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
    
    public void InstllUnknownApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setMessage("Please allow Install Unknown App Source");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_MANAGE_UNKNOWN_APP_SOURCES);
                    }
                });
                builder.setCancelable(false);
                builder.show();
            } else {
                if (!isPermissionGaranted()) {
                    takeFilePermissions();
                }
            }
        }
    }
    
    public void OverlayPermision() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setMessage("Please allow permision floating");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface p1, int p2) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
                    }
                });
                builder.setCancelable(false);
                builder.show();
            } else {
                InstllUnknownApp();
            }
        }
    }
    
    public void ManageFiles() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_STORAGE);
            } else {
                OverlayPermision();
            }
        }
    }
    
    protected AndroidDeferredManager defer() {
        return UiKit.defer();
    }
    
    private long backPressedTime = 0; 
    
    @Override
    public void onBackPressed() {
        if (isLogin) {
            long t = System.currentTimeMillis();
            if (t - backPressedTime > 2000) {    // 2 secs
                backPressedTime = t;
                toast("Press back again to exit");
            } else {
                super.onBackPressed();
            }
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }
    
    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    
    private void doActionAnimation(LottieAnimationView lottie, TextView txt, String pkg) {
        txt.setText("Starting Client " + pkg + " ...");
        lottie.setAnimation(R.raw.anim_robot);
        lottie.animate().setStartDelay(5000);
        lottie.playAnimation();
    }
    
    public void launch(AlertDialog dialog, String pkg) {
        UiKit.defer().when(() -> {
            long startTime = System.currentTimeMillis();
            dialog.dismiss();
            long elapsedTime = System.currentTimeMillis() - startTime;
            long delta = 500L - elapsedTime;
            if (delta > 0) {
                UiKit.sleep(delta);
            }
        }).done((ree) -> {
            ApkEnv.getInstance().launchApk(pkg);
        });
    }

    public void launchSplash(String pkg) {
        try {
            View view = getLayoutInflater().inflate(R.layout.launcher, null);
            CardView cv = view.findViewById(R.id.cv_lauch);
            TextView txt = view.findViewById(R.id.start_client);
            LottieAnimationView lottie = view.findViewById(R.id.animationRobot);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            builder.setCancelable(false)
                   .setView(view)
                   .setBackground(this.getResources().getDrawable(R.drawable.background_trans));

            AlertDialog dialog = builder.create();
            dialog.show();

            defer().when(() -> {
                long startTime = System.currentTimeMillis();
                doActionAnimation(lottie, txt, pkg);
                long elapsedTime = System.currentTimeMillis() - startTime;
                long delta = 5000L - elapsedTime;
                if (delta > 0) {
                    UiKit.sleep(delta);
                }
            }).done((ree) -> launch(dialog, pkg)).fail(fa -> dialog.dismiss());

        } catch(Exception err) {
            FLog.error(err.getCause().getMessage());
        }
    }
    
    public void tryUpdateLoader() {
        FirebaseDatabase.getInstance().getReference("loader").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Loader loader = snapshot.getValue(Loader.class);
                    gamename = loader.getGameName();
                    name = loader.getName();
                    version = loader.getVersion();
                    url = loader.getUrl();

                    getPref().write("loader_name_" + gamename.toLowerCase(), name);
                    getPref().write("loader_url_" + gamename.toLowerCase(), url);

                    int currentversion = getPref().readInt("loader_version_" + gamename.toLowerCase(), 0);
                    if (getPref().contains("loader_version_" + gamename.toLowerCase()) && version > currentversion) {
                        tryAskVersionLoader(gamename, name, version, url);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void tryAskUpdateLoader() {
        if (!getPref().contains("loader_version_pubgm") && !getPref().contains("loader_version_bgmi") && !getPref().contains("loader_version_farlight")) {
            showBottomSheetDialog(getResources().getDrawable(R.drawable.ic_new_update), "Loader update available", "Download now", false, v -> {
                MainActivity.get().doShowProgress(true);
                checkLoader();
                dismissBottomSheetDialog();
            }, null);
        }
    }

    public void tryAskVersionLoader(String GameName, String Name, int Version, String Url) {
        showBottomSheetDialog(getResources().getDrawable(R.drawable.ic_new_update), GameName + " update available (Version : " + Version + " )", "Download now", false, v -> {
            MainActivity.get().doShowProgress(true);
            checkLatestLoader(GameName, Name, Version, Url);
            dismissBottomSheetDialog();
        }, null);
    }

    public void checkLoader() {
        ProgressDialog progressDialog = new ProgressDialog(this, 5);
        progressDialog.setCancelable(false);
        progressDialog.show();
        executorService.submit(() -> {
            String failMsg = FileHelper.installLoader(this, progressDialog);
            FLog.info("install loader result: " + failMsg);
            if (failMsg != null) {
                toastImage(R.drawable.ic_error, failMsg);
                runOnUiThread(() ->ShowUpdateError());
            } else {
                getPref().writeInt("loader_version_" + gamename.toLowerCase(), version);
                //toastImage(R.drawable.ic_check, "Download Successful \nYou Are Up To Date");
                runOnUiThread(() ->ShowRestartApp());
            }
            try {
                progressDialog.dismiss();
                MainActivity.get().doHideProgress();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public void checkLatestLoader(String GameName, String Name, int Version, String Url) {
        ProgressDialog progressDialog = new ProgressDialog(this, 5);
        progressDialog.setCancelable(false);
        progressDialog.show();
        executorService.submit(() -> {
            String failMsg = FileHelper.DownloadLatestVersion(this, progressDialog, GameName, Name, Version, Url);
            FLog.info("install loader result: " + failMsg);
            if (failMsg != null) {
                toastImage(R.drawable.ic_error, failMsg);
                runOnUiThread(() ->ShowLatestUpdateError(GameName, Name, Version, Url));
            } else {
                toastImage(R.drawable.ic_check, "Download Successful \nYou Are Up To Date");
                getPref().writeInt("loader_version_" + GameName.toLowerCase(), Version);
            }
            try {
                progressDialog.dismiss();
                MainActivity.get().doHideProgress();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public void ShowUpdateError(){
        showBottomSheetDialog(getResources().getDrawable(R.drawable.ic_error),"Network Issue", "Download loader failed, please check your network connection and download again", false, v -> {
            MainActivity.get().doShowProgress(true);
            checkLoader();
            dismissBottomSheetDialog();
        }, null);
    }

    public void ShowLatestUpdateError(String GameName, String Name, int Version, String Url){
        showBottomSheetDialog(getResources().getDrawable(R.drawable.ic_error),"Network Issue", "Download " + GameName + " (Version : " + Version + " ) failed, please check your network connection and download again", false, v -> {
            MainActivity.get().doShowProgress(true);
            checkLatestLoader(GameName, Name, Version, Url);
            dismissBottomSheetDialog();
        }, null);
    }
    
    public void setCurrentLoaderVersion() {
       FirebaseDatabase.getInstance().getReference("loader").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Loader loader = snapshot.getValue(Loader.class);
                    int version = loader.getVersion();
                    getPref().writeInt("loader_current_version", version);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
        /*if (getPref().readInt("loader_version") != getPref().readInt("loader_current_version")) {
            UiKit.defer().when(() -> {
                long time = System.currentTimeMillis();
                toast("Check update, please wait...");
                MainActivity.get().doShowProgress(true);
                tryUpdateLoader();
                time = System.currentTimeMillis() - time;
                long delta = 3000L - time;
                if (delta > 0) {
                    UiKit.sleep(delta);
                }
            }).done((res) -> {
                MainActivity.get().doHideProgress();
            });
        }*/
    }
    
    public void showBottomSheetDialog(Drawable icon, String title, String msg, boolean cancelable, View.OnClickListener listener, View.OnClickListener listenerCancle) {
        if (BuildConfig.VERSION_CODE == 200) {
            return;
        }
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setCancelable(cancelable);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout);
        
        ImageView img = bottomSheetDialog.findViewById(R.id.icon);
        if (icon != null) {
            img.setImageDrawable(icon);
        }
        TextView title_tv = bottomSheetDialog.findViewById(R.id.title);
        title_tv.setText(title);
        TextView msg_tv = bottomSheetDialog.findViewById(R.id.msg);
        msg_tv.setText(msg);
        
        MaterialButton download = bottomSheetDialog.findViewById(R.id.btn);
        if (listener != null) {
            download.setOnClickListener(listener);
        }
        
        MaterialButton cancle = bottomSheetDialog.findViewById(R.id.btn_cancle);
        if (listenerCancle != null) {
            cancle.setOnClickListener(listenerCancle);
        } else {
            cancle.setVisibility(View.GONE);
        }
        
        bottomSheetDialog.show();
    }
    
    public void dismissBottomSheetDialog() {
        try {
        	if (bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
                bottomSheetDialog = null;
            }
        } catch(Exception err) {
        	FLog.error(err.getMessage());
        }
    }
    
}
