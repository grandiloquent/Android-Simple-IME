package psycho.euphoria.k;


import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

// InputServiceHelper
public class InputService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard keyboard_sym;
    private String mCurrentString = "";

    private boolean caps = false;
    private final Pattern mChinese = Pattern.compile("[\\u4e00-\\u9fa5]");

    public static String readAssetAsString(Context context, String assetName) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }

        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        keyboard_sym = new Keyboard(this, R.xml.symbol);
        kv.setKeyboard(keyboard_sym);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                CharSequence selectedText = ic.getSelectedText(0);
                if (TextUtils.isEmpty(selectedText)) {
                    // no selection, so delete previous character
                    ic.deleteSurroundingText(1, 0);
                } else {
                    // delete the selection
                    ic.commitText("", 1);
                }
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case 1000: {
                kv.setKeyboard(keyboard_sym);
                break;
            }
            case 1001: {
                kv.setKeyboard(keyboard);
                break;
            }
            case 1002: {
                Shared.copyBlock(this, ic);
                break;
            }
            case 1003: {
                Shared.comment(this, ic);
                break;
            }
            case 1004: {
                Shared.paste(this, ic);
                break;
            }
            case 1005: {
                Shared.cut(this, ic);
                break;
            }
            case 1006: {
                Shared.eval(this, ic);
                break;
            }

            case 1007: {
                requestHideSelf(0);
                break;
            }

            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code), 1);
        }

    }

    @Override
    public void onPress(int primaryCode) {
        Log.e("SimpleKeyboard", "Hello3 " + primaryCode);

    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {
        Log.e("SimpleKeyboard", "Hello2 " + text);
    }

    @Override
    public void swipeDown() {
        requestHideSelf(0);
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }
}
