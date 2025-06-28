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
        ModId.getAdbDir(id).getPath();
    }

  /*  @JavascriptInterface
    public String getConfigDir() {
        return this.getModId().getConfigDir().getPath();
    }

    @JavascriptInterface
    public String getModuleConfigDir() {
        return this.getModId().getModuleConfigDir().getPath();
    }
    
    @JavascriptInterface
    public String getModulesDir() {
        return this.getModId().getModulesDir().getPath();
    }
    
    @JavascriptInterface
    public String getModuleDir() {
        return this.getModId().getModuleDir().getPath();
    }
    
    @JavascriptInterface
    public String getWebRootDir() {
        return this.getModId().getWebrootDir().getPath();
    }
    
    @JavascriptInterface
    public String getSystemDir() {
        return this.getModId().getSystemDir().getPath();
    }
    
    @JavascriptInterface
    public String getPropFile() {
        return this.getModId().getAdbDir().getPath();
    }*/
}
