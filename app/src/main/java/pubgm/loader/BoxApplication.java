package pubgm.loader;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.blankj.molihuan.utilcode.util.ToastUtils;
import pubgm.loader.activity.CrashHandler;
import pubgm.loader.utils.BuildCompat;
import pubgm.loader.utils.FLog;
import pubgm.loader.utils.FPrefs;
import pubgm.loader.utils.NetworkConnection;
import com.google.android.material.color.DynamicColors;
import com.google.firebase.FirebaseApp;
import com.topjohnwu.superuser.Shell;
import java.io.IOException;

import static pubgm.loader.Config.USER_ID;
import static pubgm.loader.Config.GAME_LIST_PKG;
import static pubgm.loader.Config.GAME_LIST_ICON;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.configuration.AppLifecycleCallback;
import top.niunaijun.blackbox.app.configuration.ClientConfiguration;

import top.niunaijun.blackbox.core.system.api.blackboxapi;

import android.content.pm.PackageInfo;

import java.io.File;

@org.lsposed.lsparanoid.Obfuscate
public class BoxApplication extends Application {
    public static final String STATUS_BY = "online";
    public static BoxApplication gApp;
    private boolean isNetworkConnected = false;

    public static BoxApplication get() {
        return gApp;
    }

    public boolean isInternetAvailable() {
        return isNetworkConnected;
    }

    public void setInternetAvailable(boolean b) {
        isNetworkConnected = b;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);

        try {
            BlackBoxCore.get().doAttachBaseContext(base, new ClientConfiguration() {
                @Override
                public String getHostPackageName() {
                    return base.getPackageName();
                }

                @Override
                public boolean isHideRoot() {
                    return true;
                }

                @Override
                public boolean isHideXposed() {
                    return true;
                }

                @Override
                public boolean isEnableDaemonService() {
                    return false;
                }

                @Override
                public boolean requestInstallPackage(File file) {
                    PackageInfo packageInfo = base.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(), 0);
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gApp = this;

        BlackBoxCore.get().doCreate();
        
        
        
        
        blackboxapi.activateSdk("BLACKBOXBYZENINTRIAL");/// black box sdk key paste here 👈👈
        
        
        

        BlackBoxCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {
            public void beforeCreateApplication() {}
            public void beforeApplicationOnCreate() {}
            public void afterApplicationOnCreate() {}
        });

        DynamicColors.applyToActivitiesIfAvailable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

    
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            FLog.info("Android 11 below");
        } else {
            FLog.info("Android 12 or above");
        }

        FLog.info("SDK INT: " + Build.VERSION.SDK_INT);
        FLog.info("SDK RELEASE: " + Build.VERSION.RELEASE);

        NetworkConnection.CheckInternet network = new NetworkConnection.CheckInternet(this);
        network.registerNetworkCallback();
    }

    public boolean checkRootAccess() {
        if (Shell.rootAccess()) {
            FLog.info("Root granted");
            return true;
        } else {
            FLog.info("Root not granted");
            return false;
        }
    }

    public void doExe(String shell) {
        if (checkRootAccess()) {
            Shell.su(shell).exec();
        } else {
            try {
                Runtime.getRuntime().exec(shell);
                FLog.info("Shell: " + shell);
            } catch (IOException e) {
                FLog.error(e.getMessage());
            }
        }
    }

    public void doExecute(String shell) {
        doChmod(shell, 777);
        doExe(shell);
    }

    public void doChmod(String shell, int mask) {
        doExe("chmod " + mask + " " + shell);
    }

    public void toast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastWithImage(int id, CharSequence msg) {
        ToastUtils _toast = ToastUtils.make();
        _toast.setBgColor(android.R.color.white);
        _toast.setLeftIcon(id);
        _toast.setTextColor(android.R.color.black);
        _toast.setNotUseSystemToast();
        _toast.show(msg);
    }
}
