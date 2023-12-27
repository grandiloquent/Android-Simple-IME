package psycho.euphoria.k;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

// InputServiceHelper
public class InputService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard keyboard_op;
    //
//    private Keyboard keyboard_sym;
//    private Keyboard keyboard_num;
    private Keyboard keyboard_trans;
    private boolean caps = false;
    private Database mDatabase;

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
        mDatabase = new Database(this, new File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "text.db"
        ).getAbsolutePath());
    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
//        keyboard_sym = new Keyboard(this, R.xml.symbol);
//        keyboard_num = new Keyboard(this, R.xml.num);
        keyboard_trans = new Keyboard(this, R.xml.trans);
        keyboard_op = new Keyboard(this, R.xml.op);
        kv.setKeyboard(keyboard_op);
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
//            case 1000: {
//                kv.setKeyboard(keyboard_sym);
//                break;
//            }
            case 1001: {
                kv.setKeyboard(keyboard);
                break;
            }
            case 1002: {
                Shared.copyBlock(this, ic, mDatabase);
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
                Shared.cut(this, ic, mDatabase);
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
            case 1008: {
                Shared.cutBefore(this, ic, mDatabase);
                break;
            }
            // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/view/inputmethod/InputConnection.java
            case 1009: {
                Shared.cutAfter(this, ic, mDatabase);
                break;
            }
            case 1010: {
                String[] items = mDatabase.listText().toArray(new String[0]);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.text);
                adapter.addAll(items);
                AlertDialog dialog = new Builder(this)
                        .setAdapter(adapter, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ic.commitText(adapter.getItem(i), 1);
                            }
                        })
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                } else {
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }
                dialog.show();
                break;
            }
            case 1011: {
                Utils.showColorsDialog(this, ic);
                break;
            }
            case 1012: {
                Utils.showNotesDialog(this, ic, mDatabase);
                break;
            }
            case 1013: {
                Utils.getIds(this, ic);
                break;
            }
            case 1014: {
                Utils.saveBefore(this, ic, mDatabase);
                break;
            }
            case 1015: {
                Utils.showWordDialog(this, ic, mDatabase);
                break;
            }
            case 1016: {
                Utils.formatTime(this, ic);
                break;
            }
            case 1017: {
                kv.setKeyboard(keyboard_op);
                break;
            }
            case 1018: {
                kv.setKeyboard(keyboard_trans);
                break;
            }
            case 1019: {
                Utils.copyString(this, ic, mDatabase);
                break;
            }
            case 1020: {
                Utils.cutString(this, ic, mDatabase);
                break;
            }
            case 1021: {
                Utils.replaceBlock(this, ic);
                break;
            }
            case 1022: {
                Utils.removeEmptyLines(this, ic);
                break;
            }
            case 1023: {
                ic.commitText("()", 1);
                break;
            }
            case 1024: {
                ic.commitText("{}", 1);
                break;
            }
            case 1025: {
                ic.commitText("let x = 0;", 1);
                break;
            }
            case 1026: {
                ic.commitText("\"\"", 1);
                break;
            }
            case 1027: {
                ic.commitText("''", 1);
                break;
            }
            case 1028: {
                Utils.copyLine(this, ic, mDatabase);
                break;
            }
            case 1029: {
                Utils.cutLine(this, ic, mDatabase);
                break;
            }
            case 1030: {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showInputMethodPicker();
                break;

            }
            case 1031: {
                Utils.commentJavaScript(this, ic);
                break;
            }
            case 1032: {
                Utils.translate(this, ic);
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
