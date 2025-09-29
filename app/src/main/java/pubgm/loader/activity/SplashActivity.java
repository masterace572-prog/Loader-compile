package pubgm.loader.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import pubgm.loader.Config;
import pubgm.loader.R;
import pubgm.loader.utils.FLog;
import pubgm.loader.utils.UiKit;
import pubgm.loader.utils.FPrefs;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class SplashActivity extends AppCompatActivity {
    LottieAnimationView animationView;
    TextView descTitle;
    
    public FPrefs getPref() {
        return FPrefs.with(this);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setContentView(R.layout.activity_splash);
        animationView = findViewById(R.id.animationView);
        descTitle = findViewById(R.id.descTitle);
        
        UiKit.defer().when(() -> {
            long time = System.currentTimeMillis();
            doActionAnimation();
            time = System.currentTimeMillis() - time;
            if (getPref().readBoolean("first_time") == false) {
                getPref().writeInt("loader_version", 0);
            }
            long delta = (getPref().readBoolean("first_time") == false ? 10000L : 5000L) - time;
            if (delta > 0) {
                UiKit.sleep(delta);
            }
        }).done((res) -> {
            getPref().writeBoolean("first_time", true);
            LoginActivity.goLogin(this);
            //overridePendingTransition(0,0);
            finishActivity(0);
        });
    }
    
    private void doActionAnimation() {
        if (!getPref().readBoolean("first_time")) {
            descTitle.setText("Initialize The Application For The First Time");
        } else {
            descTitle.setText("Welcome Back");
        }
        animationView.setAnimation(R.raw.anim_robot);
        animationView.animate().setStartDelay(10000);
        animationView.playAnimation();
        
 
    }
    
    @Override
    public void onBackPressed() {
        
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
}
