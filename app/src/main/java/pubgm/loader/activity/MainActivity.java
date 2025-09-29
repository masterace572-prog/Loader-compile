package pubgm.loader.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.blankj.molihuan.utilcode.util.SnackbarUtils;
import pubgm.loader.Config;
import pubgm.loader.adapter.RecyclerViewAdapter;
import pubgm.loader.ifc.Game;
import pubgm.loader.ifc.MainInterface;
import pubgm.loader.libhelper.FileHelper;
import pubgm.loader.utils.ActivityCompat;

import pubgm.loader.utils.FLog;
import pubgm.loader.utils.PermissionUtils;
import pubgm.loader.utils.UiKit;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import android.app.ActivityManager;
import android.content.Context;
import java.io.FileOutputStream;
import java.io.OutputStream;

import pubgm.loader.libhelper.ApkEnv;
import pubgm.loader.floating.FloatLogo;
import pubgm.loader.floating.Overlay;
import pubgm.loader.floating.FloatAim;

import pubgm.loader.R;

import static pubgm.loader.Config.USER_ID;
import static pubgm.loader.Config.GAME_LIST_PKG;
import static pubgm.loader.Config.GAME_LIST_ICON;

//

import android.app.Activity;
//im//port android.app.ActivityThread;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;


import static top.niunaijun.blackbox.core.env.BEnvironment.getDataFilesDir;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.entity.pm.InstallResult;
import android.os.AsyncTask;
import top.niunaijun.blackbox.core.NativeCore;
import android.util.Log;
import android.widget.Toast;


//
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class MainActivity extends ActivityCompat implements MainInterface {
    static MainActivity instance;
    private LinearProgressIndicator progres;
    public String CURRENT_PACKAGE = "";
    DatabaseReference ref;
    ValueEventListener valueListener;
    
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private native String GetKey();
    private boolean isDisplay;
    private String daemonPath;
    public static String socket;

    
    static {
        try {
                System.loadLibrary("client");
        } catch(UnsatisfiedLinkError w) {
            FLog.error(w.getMessage());
        }
    }
    
    public static native String exdate();
    
    public static MainActivity get() {
    	return instance;
    }
    
    public static void goMain(Context context) {
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(i);
    }
    
    public String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void doFirstStart() {
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        doInitDashboard();
        findViewById(R.id.main).setAnimation(anim);
        anim.start();
    }
    
    @Override
    public void doReload() {
        
    }
    
    public LinearProgressIndicator getProgresBar() {
        if (progres == null) {
            progres = findViewById(R.id.progress);
        }
    	return progres;
    }
    
    @Override
    public void doShowProgress(boolean indeterminate) {
        if (progres == null) {
            return;
        }
 
        progres.setVisibility(View.VISIBLE);
        progres.setIndeterminate(indeterminate);
        
        if (!indeterminate) {
            progres.setMin(0);
            progres.setMax(100);
        }
    }
    
    @Override
    public void doHideProgress() {
        if (progres == null) {
            return;
        }

        progres.setIndeterminate(true);
        progres.setVisibility(View.GONE);
    }
    
public void launchSelectedGame(String packageName) {

    if (packageName != null && ApkEnv.getInstance().isInstalled(packageName)) {
        try {
            BlackBoxCore.get().launchApk(packageName, 0);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to launch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    } else {
        Toast.makeText(this, "Game not installed. Please install first.", Toast.LENGTH_SHORT).show();
    }
}

    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLogin = true;
        instance = this;
        setContentView(R.layout.activity_main);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        
        progres = findViewById(R.id.progress);

        tryUpdateLoader();
        tryAskUpdateLoader();
        
        doFirstStart();
        doCountTimerAccout();
        
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        
        findViewById(R.id.layout_dashboard).setOnClickListener(v -> {
            doInitDashboard();
            findViewById(R.id.main).setAnimation(anim);
            anim.start();
        });
        
        findViewById(R.id.layout_setting).setOnClickListener(v -> {
            doInitSetting();
            findViewById(R.id.setting).setAnimation(anim);
            anim.start();
        });
      

    }
    
    
    public void doInitRecycler() {
        doShowProgress(true);
    	ArrayList<Integer> imageValues = new ArrayList<Integer>();
        ArrayList<String> titleValues = new ArrayList<String>();
        ArrayList<String> versionValues = new ArrayList<String>();
        ArrayList<String> statusValues = new ArrayList<String>();
        ArrayList<String> packageValues = new ArrayList<String>();
        
        FirebaseDatabase.getInstance().getReference("aplikasi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Game game = snapshot.getValue(Game.class);
                    imageValues.add(GAME_LIST_ICON[game.getId()]);
                    titleValues.add(game.getTitle());
                    versionValues.add(game.getVersion());
                    statusValues.add(game.getStatus());
                    packageValues.add(game.getPackage());
                }
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(MainActivity.this, imageValues, titleValues, versionValues, statusValues, packageValues);
                RecyclerView myView =  (RecyclerView)findViewById(R.id.recyclerview);
                myView.setHasFixedSize(true);
                myView.setAdapter(adapter);
                LinearLayoutManager llm = new LinearLayoutManager(MainActivity.this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                myView.setLayoutManager(llm);
                doHideProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                doHideProgress();
                toastImage(R.drawable.ic_error, databaseError.getMessage());
            }
        });
      
    }
    
    @Override
    public void doInitDashboard() {
        findViewById(R.id.main).setVisibility(View.VISIBLE);
        findViewById(R.id.setting).setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.icon_dashboard)).setImageDrawable(getDrawable(R.mipmap.ic_dashboard));
        ((ImageView)findViewById(R.id.icon_setting)).setImageDrawable(getDrawable(R.mipmap.ic_settings_adaptive_fore));
        ((TextView)findViewById(R.id.txt_dashboard)).setTextColor(Color.parseColor("#FFFFFFFF"));
        ((TextView)findViewById(R.id.txt_setting)).setTextColor(Color.parseColor("#FF535353"));
        doInitRecycler();
    }
    
    // ========================================================== //

   
    
      public void addAdditionalApp(boolean system, String packageName) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (ApkEnv.getInstance().isInstalled(packageName)) {
                    doHideProgress();
                    ApkEnv.getInstance().launchApk(packageName);
                } else {
                    try {
                        if (ApkEnv.getInstance().installByPackage(packageName)) {
                            doHideProgress();
                            ApkEnv.getInstance().launchApk(packageName);
                        }
                    } catch(Exception err) {
                        FLog.error(err.getMessage());
                        doHideProgress();
                    }
                }
            }
        });
    }
    
    
    
    
    public void doInitSettingView() {
        
        final Switch sw_hide_root = findViewById(R.id.sw_hide_root);
        sw_hide_root.setChecked(getPref().readBoolean("hide_root"));
        sw_hide_root.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked){
						getPref().writeBoolean("hide_root", true);
					}else{
						getPref().writeBoolean("hide_root", false);
					}
                    SnackbarUtils.with(buttonView).setBgColor(R.color.background).setMessage("Restart to take effect").setMessageColor(Color.WHITE).setAction("Restart", v-> {
                        restartApp(MainActivity.class.getSimpleName());
                    }).show();
				}
			});
        
        final Switch sw_anti_crash = findViewById(R.id.sw_anti_crash);
        sw_anti_crash.setChecked(getPref().readBoolean("anti_crash"));
        sw_anti_crash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked){
						getPref().writeBoolean("anti_crash", true);
					}else{
						getPref().writeBoolean("anti_crash", false);
					}
                    SnackbarUtils.with(buttonView).setBgColor(R.color.background).setMessage("Restart to take effect").setMessageColor(Color.WHITE).setAction("Restart", v-> {
                        restartApp(MainActivity.class.getSimpleName());
                    }).show();
				}
			});
        
        final Switch hide_recorder = findViewById(R.id.hide_recorder);
        hide_recorder.setChecked(getPref().readBoolean("anti_recorder"));
        hide_recorder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked){
						getPref().writeBoolean("anti_recorder", true);
					}else{
						getPref().writeBoolean("anti_recorder", false);
					}
                    SnackbarUtils.with(buttonView).setBgColor(R.color.background).setMessage("Restart to take effect").setMessageColor(Color.WHITE).setAction("Restart", v-> {
                        restartApp(MainActivity.class.getSimpleName());
                    }).show();
				}
			});
        
        findViewById(R.id.logOut).setOnClickListener(v -> {
            if (currentAuth != null) {
                currentAuth.signOut();
                LoginActivity.goLogin(MainActivity.this);
                finishActivity(0);
            }
        });
        
        findViewById(R.id.facebook).setOnClickListener(v -> {
            doShowProgress(true);
            addAdditionalApp(false, "com.facebook.katana");
        });
        
        findViewById(R.id.facebook).setOnLongClickListener(v -> {
            showBottomSheetDialog(getResources().getDrawable(android.R.drawable.ic_dialog_alert), "Confirm", "Want remove it ?", false, sv -> {
                ApkEnv.getInstance().unInstallApp("com.facebook.katana");
                dismissBottomSheetDialog();
            }, v1 -> {
                dismissBottomSheetDialog();
            });
            return true;
        });
        
        
        
        
        findViewById(R.id.twitter).setOnClickListener(v -> {
            doShowProgress(true);
            addAdditionalApp(false, "com.twitter.android");
        });
        
        findViewById(R.id.twitter).setOnLongClickListener(v -> {
            showBottomSheetDialog(getResources().getDrawable(android.R.drawable.ic_dialog_alert), "Confirm", "Want remove it ?", false, sv -> {
                ApkEnv.getInstance().unInstallApp("com.twitter.android");
                dismissBottomSheetDialog();
            }, v1 -> {
                dismissBottomSheetDialog();
            });
            return true;
        });
        
        
        
        
        
        findViewById(R.id.wechat).setOnClickListener(v -> {
            doShowProgress(true);
            addAdditionalApp(false, "com.fast.free.unblock.secure.vpn");
        });
        
  
      
        findViewById(R.id.wechat).setOnLongClickListener(v -> {
            showBottomSheetDialog(getResources().getDrawable(android.R.drawable.ic_dialog_alert), "Confirm", "Want remove it ?", false, sv -> {
                ApkEnv.getInstance().unInstallApp("com.fast.free.unblock.secure.vpn");
                dismissBottomSheetDialog();
            }, v1 -> {
                dismissBottomSheetDialog();
            });
            return true;
        });
        
        
        
        
        
        
        findViewById(R.id.qq).setOnClickListener(v -> {
            doShowProgress(true);
            addAdditionalApp(false, "com.guoshi.httpcanary");
        });
        
        
   
        findViewById(R.id.qq).setOnLongClickListener(v -> {
            showBottomSheetDialog(getResources().getDrawable(android.R.drawable.ic_dialog_alert), "Confirm", "Want remove it ?", false, sv -> {
                ApkEnv.getInstance().unInstallApp("com.guoshi.httpcanary");
                dismissBottomSheetDialog();
            }, v1 -> {
                dismissBottomSheetDialog();
            });
            return true;
        });
        
        findViewById(R.id.checkUpdate).setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(this, 5);
            progressDialog.setCancelable(false);
            progressDialog.show();
            executorService.submit(() -> {
                String failMsg = FileHelper.installLoader(this, progressDialog);
                FLog.info("install loader result: " + failMsg);
                if (failMsg != null) {
                    toastImage(R.drawable.ic_error, failMsg);
                }
                try {
                    progressDialog.dismiss();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            });
        });
        
    }
    
    @Override
    public void doInitSetting() {
        findViewById(R.id.main).setVisibility(View.GONE);
        findViewById(R.id.setting).setVisibility(View.VISIBLE);
        ((ImageView)findViewById(R.id.icon_dashboard)).setImageDrawable(getDrawable(R.mipmap.ic_dashboard_adaptive_fore));
        ((ImageView)findViewById(R.id.icon_setting)).setImageDrawable(getDrawable(R.mipmap.ic_settings));
        ((TextView)findViewById(R.id.txt_dashboard)).setTextColor(Color.parseColor("#FF535353"));
        ((TextView)findViewById(R.id.txt_setting)).setTextColor(Color.parseColor("#FFFFFFFF"));
        doInitSettingView();
    }
    
    // ========================================================== //
    
    private void doCountTimerAccout() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 1000);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date expiryDate = dateFormat.parse(exdate());
                    long now = System.currentTimeMillis();
                    long distance = expiryDate.getTime() - now;
                    long days = distance / (24 * 60 * 60 * 1000);
                    long hours = distance / (60 * 60 * 1000) % 24;
                    long minutes = distance / (60 * 1000) % 60;
                    long seconds = distance / 1000 % 60;
                    if (distance < 0) {
                    } else {
                        TextView Hari = findViewById(R.id.tv_d);
                        TextView Jam = findViewById(R.id.tv_h);
                        TextView Menit = findViewById(R.id.tv_m);
                        TextView Detik = findViewById(R.id.tv_s);
                        if (days > 0) {
                            Hari.setText(" " + String.format("%02d", days));
                        }
                        if (hours > 0) {
                            Jam.setText(" " + String.format("%02d", hours));
                        }
                        if (minutes > 0) {
                            Menit.setText(" " + String.format("%02d", minutes));
                        }
                        if (seconds > 0) {
                            Detik.setText(" " + String.format("%02d", seconds));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 0);
    }
    
    private void CheckFloatViewPermission() {
        if (!Settings.canDrawOverlays(MainActivity.get())) {
            toastImage(R.drawable.ic_error, "Requires permission of 'Display over other apps' to show floating.");
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (FloatLogo.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void startPatcher() {
        if (!Settings.canDrawOverlays(MainActivity.get())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 123);
        } else {
            startFloater();
        }
    }

    private void startFloater() {
        if (!isServiceRunning()) {
            startService(new Intent(MainActivity.get(), FloatLogo.class));
        } else {
            toastImage(R.drawable.ic_error, "Service is already running.");
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.get(), FloatLogo.class));
        stopService(new Intent(MainActivity.get(), Overlay.class));
        FloatAim.AimbotFOV(false);
		stopService(new Intent(MainActivity.get(), FloatAim.class));
    }
    
    private void loadAssets() {
        InputStream in = getResources().openRawResource(R.raw.sock);
        FileOutputStream out ;
        try {
            out = new FileOutputStream(getFilesDir() + "/sock");
            byte[] buff = new byte[1024];
            int read ;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        daemonPath = getFilesDir() + "/sock";
		try {
			Runtime.getRuntime().exec("chmod 777 " + daemonPath);
		} catch (IOException e) {}
    }
    
    @Override
    protected void onResume() {
        doFirstStart();
        doCountTimerAccout();
        super.onResume();
    }
    
}
