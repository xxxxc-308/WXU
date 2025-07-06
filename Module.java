package dev.mmrl;

import android.webkit.JavascriptInterface;

import com.dergoogler.mmrl.platform.file.SuFile;
import com.dergoogler.mmrl.platform.model.ModId;
import com.dergoogler.mmrl.webui.interfaces.WXInterface;
import com.dergoogler.mmrl.webui.interfaces.WXOptions;

import org.jetbrains.annotations.NotNull;

public class Module extends WXInterface {
    private final ModId modId;
    private final SuFile webrootDir;
    private final SuFile moduleDir;
    private final SuFile moduleConfigDir;
    private final SuFile systemDir;


    public Module(WXOptions wxOptions) {
        super(wxOptions);

        this.modId = this.getModId();
        this.webrootDir = ModId.Companion.getWebrootDir(modId);
        this.moduleDir = ModId.Companion.getModuleDir(modId);
        this.moduleConfigDir = ModId.Companion.getModuleConfigDir(modId);
        this.systemDir = ModId.Companion.getSystemDir(modId);
    }

    @NotNull
    @Override
    public String getName() {
        return "module";
    }

    @JavascriptInterface
    public String getId() {
        return modId.toString();
    }

    @JavascriptInterface
    public String getAdbDir() {
        return ModId.Companion.getAdbDir(modId).getPath();
    }

    @JavascriptInterface
    public String getConfigDir() {
        return ModId.Companion.getConfigDir(modId).getPath();
    }

    @JavascriptInterface
    public String getModuleConfigDir() {
        return moduleConfigDir.getPath();
    }

    @JavascriptInterface
    public String getModuleConfigDir(String... paths) {
        SuFile file = new SuFile(moduleConfigDir, paths);
        return file.getPath();
    }

    @JavascriptInterface
    public String getModulesDir() {
        return ModId.Companion.getModulesDir(modId).getPath();
    }

    @JavascriptInterface
    public String getModuleDir() {
        return moduleDir.getPath();
    }

    @JavascriptInterface
    public String getModuleDir(String... paths) {
        SuFile file = new SuFile(moduleDir, paths);
        return file.getPath();
    }

    @JavascriptInterface
    public String getWebRootDir() {
        return webrootDir.getPath();
    }

    @JavascriptInterface
    public String getWebRootDir(String... paths) {
        SuFile file = new SuFile(webrootDir, paths);
        return file.getPath();
    }

    @JavascriptInterface
    public String getSystemDir() {
        return systemDir.getPath();
    }

    @JavascriptInterface
    public String getSystemDir(String... paths) {
        SuFile file = new SuFile(systemDir, paths);
        return file.getPath();
    }

    @JavascriptInterface
    public String getPropFile() {
        return ModId.Companion.getAdbDir(modId).getPath();
    }
}
