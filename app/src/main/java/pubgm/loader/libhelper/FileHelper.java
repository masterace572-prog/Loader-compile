package pubgm.loader.libhelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.documentfile.provider.DocumentFile;
import com.blankj.molihuan.utilcode.util.FileUtils;
import pubgm.loader.Config;
import pubgm.loader.BoxApplication;
import pubgm.loader.activity.MainActivity;
import pubgm.loader.utils.ActivityCompat;
import pubgm.loader.utils.FLog;
import pubgm.loader.utils.PermissionUtils;
import pubgm.loader.utils.UiKit;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.BaseProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.molihuan.pathselector.PathSelector;
import com.molihuan.pathselector.entity.FileBean;
import com.molihuan.pathselector.fragment.BasePathSelectFragment;
import com.molihuan.pathselector.listener.CommonItemListener;
import com.molihuan.pathselector.utils.MConstants;
import io.github.rupinderjeet.kprogresshud.KProgressHUD;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import pubgm.loader.R;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

import io.michaelrocks.paranoid.Obfuscate;
import java.util.Locale;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Obfuscate
public class FileHelper {
    static boolean dn = false;
    
    public static boolean downloadLibTask(Activity activity, String url, String outpath) {
        KProgressHUD dialog = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Downloading")
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .show();
        
        new Thread(() -> {
            try {
                dn = false;
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Connection", "close");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new IOException("Request Code Not 200");
                }

                File loaderPath = new File(outpath);

                InputStream bStream = connection.getInputStream();
                FileOutputStream fileOut = new FileOutputStream(loaderPath);

                byte[] data = new byte[1024];
                long total = 0;
                int count;
                while ((count = bStream.read(data)) != -1) {
                    total += count;
                    long finalTotal = total;
                    activity.runOnUiThread(() -> {
                        float curr = (float)(finalTotal / 1024) / 1024;
                        String txt = String.format(Locale.getDefault(),"Downloading ( %.2fMB )", curr);
                        dialog.setLabel(txt);
                    });
                    fileOut.write(data, 0, count);
                }

                bStream.close();
                fileOut.flush();
                fileOut.close();
                connection.disconnect();
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    dn = true;
                    ActivityCompat.getActivityCompat().toastImage(R.drawable.ic_check, "Download done.");
                });
                
            } catch (IOException e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    dn = true;
                    ActivityCompat.getActivityCompat().toastImage(R.drawable.ic_error, "Failed To Download, Please Check your internet connection, and if problem persist, then download a newer loader from Loader");
                });
            }
        }).start();
        return dn;
    }
    
    public interface DownloadListener {
        void onProgress(int progress);
    }
    
    public static boolean downloadFile(String url, File outFile, DownloadListener listener) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        FileOutputStream fos = null;
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != 200) {
                return false;
            }
            ResponseBody body = response.body();
            if (body == null) {
                return false;
            }
            long toal = body.contentLength();
            long sum = 0;

            InputStream inputStream = body.byteStream();
            fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) >= 0) {
                fos.write(buffer, 0, count);
                sum += count;
                int progress = (int) ((sum * 1.0) / toal * 100);
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }
            fos.flush();
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
    public static String installLoader(ActivityCompat activity, ProgressDialog dialog) {

    File loader_path = activity.getFilesDir();

    updateMessage(activity, dialog, "Fetching loader config...");

    boolean downloadResult = downloadFile(
            activity.getPref().read("loader_url_pubgm"),
            new File(loader_path, activity.getPref().read("loader_name_pubgm")),
            (progress) -> updateMessage(activity, dialog, "Downloading loader PUBGM..." + progress + "%")
    );
    if (!downloadResult) {
        return "Download loader failed, please check your network, error: 1";
    }

    downloadResult = downloadFile(
            activity.getPref().read("loader_url_bgmi"),
            new File(loader_path, activity.getPref().read("loader_name_bgmi")),
            (progress) -> updateMessage(activity, dialog, "Downloading loader BGMI..." + progress + "%")
    );
    if (!downloadResult) {
        return "Download loader failed, please check your network, error: 2";
    }

    return null;
}

public static String DownloadLatestVersion(ActivityCompat activity, ProgressDialog dialog, String GameName, String Name, int Version, String Url) {

    File loader_path = activity.getFilesDir();

    updateMessage(activity, dialog, "Fetching loader config...");

    boolean downloadResult = downloadFile(
            activity.getPref().read("loader_url_pubgm"),
            new File(loader_path, activity.getPref().read("loader_name_pubgm")),
            (progress) -> updateMessage(activity, dialog, "Downloading loader PUBGM..." + progress + "%")
    );
    if (!downloadResult) {
        return "Download loader failed, please check your network, error: 1";
    }

    downloadResult = downloadFile(
            activity.getPref().read("loader_url_bgmi"),
            new File(loader_path, activity.getPref().read("loader_name_bgmi")),
            (progress) -> updateMessage(activity, dialog, "Downloading loader BGMI..." + progress + "%")
    );
    if (!downloadResult) {
        return "Download loader failed, please check your network, error: 2";
    }

    return null;
}
    
    private static void updateMessage(Activity activity, ProgressDialog dialog, String msg) {
        if (activity == null || dialog == null || TextUtils.isEmpty(msg)) {
            return;
        }
        FLog.info("update dialog message: " + msg);
        activity.runOnUiThread(() -> {
            dialog.setMessage(msg);
        });
    }
    
    public static void tryInstallWithCopyObb(MainActivity activity, LinearProgressIndicator prog, String packageName) {
        new Thread(() -> {
            PackageInfo info = null;
            try {
                info = activity.getPackageManager().getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException err) {
                FLog.error(err.getMessage());
            }

            if (info == null) {
                handleInstallationError(activity, prog, "Please Install Game first.");
                return;
            }

            if (!info.applicationInfo.nativeLibraryDir.contains("64")) {
                handleInstallationError(activity, prog, "Please Install Game 64Bit version.");
                return;
            }

            String gameObb = "main." + info.versionCode + "." + info.packageName + ".obb";
            File obbDest = new File("storage/emulated/0/Android/obb/" + packageName, gameObb);

            if (!obbDest.exists()) {
                handleInstallationError(activity, prog, "Obb File not found Restart Game");
                return;
            }

            File virObbDir = ApkEnv.getInstance().getObbContainerPath(packageName);
            if (!virObbDir.exists()) virObbDir.mkdirs();

            File virObbDest = new File(virObbDir, gameObb);

            activity.runOnUiThread(() -> {
                activity.doHideProgress();
                activity.doShowProgress(true);
            });

            try {
                FileUtils.copy(obbDest.toString(), virObbDest.toString());
            } catch (Exception err) {
                FLog.error(err.getMessage());
                return;
            }

            if (!ApkEnv.getInstance().isInstalled(packageName)) {
                boolean installResult = ApkEnv.getInstance().installBySystem(packageName);
                if (!installResult) {
                    handleInstallationError(activity, prog, "Failed Add Games");
                    return;
                }
            }

            ApplicationInfo applicationInfo = ApkEnv.getInstance().getApplicationInfo(packageName);
            if (applicationInfo == null) {
                handleInstallationError(activity, prog, "Error, Application Info");
                return;
            }

            activity.runOnUiThread(() -> {
                MainActivity.get().doInitRecycler();
                prog.setIndeterminate(false);
                activity.doHideProgress();
                ActivityCompat.getActivityCompat().toastImage(R.drawable.ic_check, "Installation is complete.");
            });

            if (BoxApplication.get().checkRootAccess()) {
                File listAbi = new File(applicationInfo.nativeLibraryDir);
                if (listAbi.exists() && listAbi.isDirectory()) {
                    File[] files = listAbi.listFiles();
                    if (files != null) {
                        for (File abi : files) {
                            BoxApplication.get().doChmod(abi.toString(), 755);
                        }
                    }
                }
            }
        }).start();
    }

    public static void tryInstallWithCopyObb32(MainActivity activity, LinearProgressIndicator prog, String packageName) {
        new Thread(() -> {
            PackageInfo info = null;
            try {
                info = activity.getPackageManager().getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException err) {
                FLog.error(err.getMessage());
            }

            if (info == null) {
                handleInstallationError(activity, prog, "Please Install Game first.");
                return;
            }

            if (!info.applicationInfo.nativeLibraryDir.contains("32")) {
                handleInstallationError(activity, prog, "Please Install Game 32Bit version.");
                return;
            }

            String gameObb = "main." + info.versionCode + "." + info.packageName + ".obb";
            File obbDest = new File("storage/emulated/0/Android/obb/" + packageName, gameObb);

            if (!obbDest.exists()) {
                handleInstallationError(activity, prog, "Obb File not found Restart Game");
                return;
            }

            File virObbDir = ApkEnv.getInstance().getObbContainerPath(packageName);
            if (!virObbDir.exists()) virObbDir.mkdirs();

            File virObbDest = new File(virObbDir, gameObb);

            activity.runOnUiThread(() -> {
                activity.doHideProgress();
                activity.doShowProgress(true);
            });

            try {
                FileUtils.copy(obbDest.toString(), virObbDest.toString());
            } catch (Exception err) {
                FLog.error(err.getMessage());
                return;
            }

            if (!ApkEnv.getInstance().isInstalled(packageName)) {
                boolean installResult = ApkEnv.getInstance().installBySystem(packageName);
                if (!installResult) {
                    handleInstallationError(activity, prog, "Failed Add Games");
                    return;
                }
            }

            ApplicationInfo applicationInfo = ApkEnv.getInstance().getApplicationInfo(packageName);
            if (applicationInfo == null) {
                handleInstallationError(activity, prog, "Error, Application Info");
                return;
            }

            activity.runOnUiThread(() -> {
                MainActivity.get().doInitRecycler();
                prog.setIndeterminate(false);
                activity.doHideProgress();
                ActivityCompat.getActivityCompat().toastImage(R.drawable.ic_check, "Installation is complete.");
            });

            if (BoxApplication.get().checkRootAccess()) {
                File listAbi = new File(applicationInfo.nativeLibraryDir);
                if (listAbi.exists() && listAbi.isDirectory()) {
                    File[] files = listAbi.listFiles();
                    if (files != null) {
                        for (File abi : files) {
                            BoxApplication.get().doChmod(abi.toString(), 755);
                        }
                    }
                }
            }
        }).start();
    }

    private static void handleInstallationError(MainActivity activity, LinearProgressIndicator prog, String errorMessage) {
        activity.runOnUiThread(() -> {
            activity.doHideProgress();
            ActivityCompat.getActivityCompat().toastImage(R.drawable.ic_error, errorMessage);
        });
    }
}
