package dev.mmrl;

import android.webkit.JavascriptInterface;

import com.dergoogler.mmrl.platform.model.ModId;
import com.dergoogler.mmrl.webui.interfaces.WXInterface;
import com.dergoogler.mmrl.webui.interfaces.WXOptions;

public class Module extends WXInterface {
    public Module(WXOptions wxOptions) {
        super(wxOptions);
    }

    @Override
    public String getName() {
        return "module";
    }

    @JavascriptInterface
    public String getId() {
        return this.getModId().toString();
    }

    @JavascriptInterface
    public String getAdbDir() {
        ModId id = this.getModId();
        return ModId.Companion.getAdbDir(id).getPath();
    }

    @JavascriptInterface
    public String getConfigDir() {
        ModId id = this.getModId();
        return ModId.Companion.getConfigDir(id).getPath();
    }

    @JavascriptInterface
    public String getModuleConfigDir() {
        ModId id = this.getModId();
        return ModId.Companion.getModuleConfigDir(id).getPath();
    }

    @JavascriptInterface
    public String getModulesDir() {
        ModId id = this.getModId();
        return ModId.Companion.getModulesDir(id).getPath();
    }

    @JavascriptInterface
    public String getModuleDir() {
        ModId id = this.getModId();
        return ModId.Companion.getModuleDir(id).getPath();
    }

    @JavascriptInterface
    public String getWebRootDir() {
        ModId id = this.getModId();
        return ModId.Companion.getWebrootDir(id).getPath();
    }

    @JavascriptInterface
    public String getSystemDir() {
        ModId id = this.getModId();
        return ModId.Companion.getSystemDir(id).getPath();
    }

    @JavascriptInterface
    public String getPropFile() {
        ModId id = this.getModId();
        return ModId.Companion.getAdbDir(id).getPath();
    }
}
