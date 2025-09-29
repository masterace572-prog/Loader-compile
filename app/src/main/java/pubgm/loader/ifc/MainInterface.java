package pubgm.loader.ifc;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public interface MainInterface {
    void doFirstStart();
    void doReload();
    void doInitDashboard();
    void doInitSetting();
    void doShowProgress(boolean indeterminate);
    void doHideProgress();
}
