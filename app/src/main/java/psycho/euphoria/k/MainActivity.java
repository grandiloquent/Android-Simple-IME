package psycho.euphoria.k;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.util.List;


public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean overlayEnabled = Settings.canDrawOverlays(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !overlayEnabled) {
            openOverlaySettings();
        }
//        List<InputMethodInfo> inputMethodInfos = inputMethodManager.getInputMethodList();
//
//        for (InputMethodInfo inputMethodInfo : inputMethodInfos) {
//            Log.e("B5aOx2", String.format("onCreate, %s", inputMethodInfo.getId()));
//            if (inputMethodInfo.getId().contains("pinyin")) {
//
//            }
//        }
        // psycho.euphoria.k/.InputService
//        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
//        final IBinder token = this.getWindow().getAttributes().token;
//        imm.setInputMethod(token, "psycho.euphoria.k/.InputService");
    }

    private void openOverlaySettings() {
        final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e("MainActivity", e.getMessage());
        }
    }
}