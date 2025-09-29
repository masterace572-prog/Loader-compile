package pubgm.loader.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.molihuan.utilcode.util.SnackbarUtils;
import com.blankj.molihuan.utilcode.util.ToastUtils;
import pubgm.loader.activity.MainActivity;
import pubgm.loader.libhelper.FileHelper;
import pubgm.loader.utils.FLog;
import pubgm.loader.utils.PermissionUtils;
import pubgm.loader.utils.UiKit;
import com.google.android.material.snackbar.Snackbar;
import io.michaelrocks.paranoid.Obfuscate;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import android.content.Intent;
import pubgm.loader.floating.FloatLogo;
import pubgm.loader.floating.Overlay;
import pubgm.loader.floating.FloatAim;

import pubgm.loader.R;
import pubgm.loader.libhelper.ApkEnv;
import static pubgm.loader.Config.USER_ID;
import static pubgm.loader.Config.GAME_LIST_PKG;
import static pubgm.loader.Config.GAME_LIST_ICON;

@Obfuscate
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    public MainActivity activity;
    public ArrayList<Integer> imageValues;
    public ArrayList<String> titleValues;
    public ArrayList<String> versionValues;
    public ArrayList<String> statusValues;
    public ArrayList<String> packageValues;

    public RecyclerViewAdapter(MainActivity activity, ArrayList<Integer> imageValues, ArrayList<String> titleValues, ArrayList<String> versionValues, ArrayList<String> statusValues, ArrayList<String> packageValues) {
        this.activity = activity;
        this.imageValues = imageValues;
        this.titleValues = titleValues;
        this.versionValues = versionValues;
        this.statusValues = statusValues;
        this.packageValues = packageValues;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_games, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.gameIcon.setImageResource(imageValues.get(position));
        holder.gameTitle.setText(titleValues.get(position));
        holder.gameVersion.setText(versionValues.get(position));
        holder.gameStatus.setText(statusValues.get(position));
        holder.gamePackage.setText(packageValues.get(position));
        
        doInitGames(holder.okBtn, holder.noBtn, packageValues.get(position));
        
        if (statusValues.get(position).equals("Risk")) {
            holder.gameStatus.setTextColor(Color.RED);
        } else if (statusValues.get(position).equals("Maintenance")) {
            holder.gameStatus.setTextColor(Color.YELLOW);
        } else if (statusValues.get(position).equals("Coming Soon")) {
            holder.gameStatus.setTextColor(Color.GRAY);
        } else {
            holder.gameStatus.setTextColor(Color.GREEN);
        }
        
        holder.okBtn.setOnClickListener(v -> {
            if (statusValues.get(position).equals("Maintenance") || statusValues.get(position).equals("Coming Soon")) {
                activity.toastImage(R.drawable.logo_chart, "App is currently under: " + statusValues.get(position));
            } else {
                activity.doShowProgress(true);
                doInstallAndRun(holder, position);
            }
        });
        
        holder.noBtn.setOnClickListener(v -> {
            activity.doShowProgress(true);
            unInstallWithDellay(packageValues.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return imageValues.size();
    }
    
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView gameIcon;
        private TextView gameTitle;
        private TextView gameVersion;
        private TextView gameStatus;
        private TextView gamePackage;
        private TextView okBtn;
        private TextView noBtn;
        
        public MyViewHolder(View itemView) {
            super(itemView);
            gameIcon = (ImageView)itemView.findViewById(R.id.gameIcon);
            gameTitle = (TextView)itemView.findViewById(R.id.gameTitle);
            gameVersion = (TextView)itemView.findViewById(R.id.gameVersion);
            gameStatus = (TextView)itemView.findViewById(R.id.gameStatus);
            gamePackage = (TextView)itemView.findViewById(R.id.gamePackage);
            okBtn = (TextView)itemView.findViewById(R.id.okBtn);
            noBtn = (TextView)itemView.findViewById(R.id.noBtn);
        }
    }
    
    
    public void doInitGames(TextView okBtn, TextView noBtn, String packageName) {
        activity.runOnUiThread(() -> {
            if (ApkEnv.getInstance().isInstalled(packageName)) {
                if (ApkEnv.getInstance().isRunning(packageName)) {
                    okBtn.setBackground(activity.getDrawable(R.drawable.button_uinstall));
                    okBtn.setText(R.string.stop_game);
                } else {
                    okBtn.setBackground(activity.getDrawable(R.drawable.button_play));
                    okBtn.setText(R.string.play_game);
                }
                noBtn.setBackground(activity.getDrawable(R.drawable.button_uinstall));
                noBtn.setEnabled(true);
            } else {
                okBtn.setBackground(activity.getDrawable(R.drawable.button_install));
                okBtn.setText(R.string.install_game);
                noBtn.setBackground(activity.getDrawable(R.drawable.button_coming));
                noBtn.setEnabled(false);
            }
        });
    }
    
    
    private void doInstallAndRun(MyViewHolder holder, int position) {
    if (activity == null) {
        ToastUtils.showLong("Null Activity");
        return;
    }
    activity.CURRENT_PACKAGE = packageValues.get(position);

    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(() -> {
        if (ApkEnv.getInstance().isInstalled(packageValues.get(position))) {
            activity.doHideProgress();
            if (ApkEnv.getInstance().isRunning(packageValues.get(position))) {
                ApkEnv.getInstance().stopRunningApp(packageValues.get(position));
                holder.okBtn.setBackground(activity.getDrawable(R.drawable.button_play));
                holder.okBtn.setText(R.string.play_game);
                activity.stopService(new Intent(MainActivity.get(), FloatLogo.class));
                activity.stopService(new Intent(MainActivity.get(), Overlay.class));
                FloatAim.AimbotFOV(false);
                activity.stopService(new Intent(MainActivity.get(), FloatAim.class));
            } else {
                // Directly launch game via BlackBox
                activity.launchSelectedGame(packageValues.get(position));
                doInitGames(holder.okBtn, holder.noBtn, packageValues.get(position));
            }
        } else {
            try {
                activity.showBottomSheetDialog(
                    activity.getResources().getDrawable(imageValues.get(position)),
                    "Client: " + titleValues.get(position),
                    "This process may take 1-3 minutes, please do not close the application until the process is complete.",
                    false,
                    v -> {
                        activity.dismissBottomSheetDialog();
                        FileHelper.tryInstallWithCopyObb(activity, activity.getProgresBar(), packageValues.get(position));
                    },
                    v1 -> {
                        activity.doHideProgress();
                        activity.dismissBottomSheetDialog();
                    }
                );
            } catch (Exception err) {
                FLog.error(err.getMessage());
            }
        }
    });
}

    
    private void unInstallWithDellay(String packageName) {
        UiKit.defer().when(() -> {
            long time = System.currentTimeMillis();
            ApkEnv.getInstance().unInstallApp(packageName);
            time = System.currentTimeMillis() - time;
            long delta = 500L - time;
            if (delta > 0) {
                UiKit.sleep(delta);
            }
        }).done((res) -> {
            activity.doInitRecycler();
            activity.doHideProgress();
            activity.toastImage(R.drawable.ic_check, packageName + " was successfully uninstalled.");
        });
    }
}
