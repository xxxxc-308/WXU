package dev.mmrl;

import android.webkit.JavascriptInterface;

import com.dergoogler.mmrl.webui.interfaces.WXInterface;
import com.dergoogler.mmrl.webui.interfaces.WXOptions;

import org.jetbrains.annotations.NotNull;

public class Process  extends WXInterface {
    public Process(WXOptions wxOptions) {
        super(wxOptions);
    }

    @NotNull
    @Override
    public String getName() {
        return "process";
    }

    @JavascriptInterface
    public String platform() {
        return getOptions().getPlatform().name();
    }

    @JavascriptInterface
    public boolean isAlive() {
        return getOptions().isProviderAlive();
    }
}