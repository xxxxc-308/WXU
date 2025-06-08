package dev.mmrl;

import android.webkit.JavascriptInterface;

import com.dergoogler.mmrl.webui.interfaces.WXInterface;
import com.dergoogler.mmrl.webui.interfaces.WXOptions;

public class Module extends WXInterface {
    public Module(WXOptions wxOptions) {
        super(wxOptions);
    }

    @JavascriptInterface
    public String getId() {
        return this.getModId().toString();
    }
}
