package psycho.euphoria.k;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.IpConfiguration;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static psycho.euphoria.k.Shared.getContinueLines;
import static psycho.euphoria.k.Shared.getLine;
import static psycho.euphoria.k.Shared.getString;

public class Utils {
    public static void getIds(Context context, InputConnection inputConnection) {
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        List<String> ids = Shared.collectIds(currentText.toString());
        StringBuilder sb = new StringBuilder();
        for (String s : ids) {
            sb.append(s).append("\n");
        }
        inputConnection.commitText(sb.toString(), 1);
    }

    public static void saveBefore(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        if (startIndex == 0) return;
        String s = currentText.subSequence(0, startIndex).toString();
        s = s.trim();
        String f = Shared.substringBefore(s, "\n");
        String b = Shared.substringAfter(s, "\n");
        database.insertWord(f, b);
        inputConnection.setComposingRegion(0, startIndex);
        inputConnection.setComposingText("", 1);
        inputConnection.finishComposingText();


    }

    public static void showColorsDialog(Context context, InputConnection ic) {
        ColorAdapter colorAdapter = new ColorAdapter(context);
        AlertDialog dialog = new Builder(context)
                .setAdapter(colorAdapter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ic.commitText(colorAdapter.getItem(i), 1);
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
    }

    public static void showNotesDialog(Context context, InputConnection ic, Database database) {
        NoteAdapter noteAdapter = new NoteAdapter(context, database.listNote());
        AlertDialog dialog = new Builder(context)
                .setAdapter(noteAdapter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ic.commitText(database.getContent(noteAdapter.getItem(i)), 1);
                    }
                })
                .setPositiveButton("导入", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s = Shared.readText(context);
                        if (s == null) return;
                        s = s.trim();
                        String f = Shared.substringBefore(s, "\n");
                        String b = Shared.substringAfter(s, "\n");
                        database.insert(f, b);
                    }
                })
                .setNegativeButton("删除", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s = Shared.readText(context);
                        if (s == null) return;
                        s = s.trim();
                        database.delete(s);
                    }
                })
                .create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.show();
    }

    public static void showWordDialog(Context context, InputConnection ic, Database database) {
        WordAdapter wordAdapter = new WordAdapter(context, database.listWord());
        AlertDialog dialog = new Builder(context)
                .setAdapter(wordAdapter, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ic.commitText(database.getWord(wordAdapter.getItem(i)), 1);
                    }
                })
                .setPositiveButton("导出", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String s = database.getAllWord();
                        ic.commitText(s, 1);
                    }
                })
                .setNegativeButton("清空", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        database.removeALlWord();
                    }
                })
                .setNeutralButton("标识", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<String> ids = Shared.collectIds(database.getAllWord());
                        StringBuilder sb = new StringBuilder();
                        for (String s : ids) {
                            sb.append(s).append("\n");
                        }
                        ic.commitText(sb.toString(), 1);
                    }
                })
                .create();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        } else {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        dialog.show();
    }

    public static void formatTime(Context context, InputConnection ic) {
        ExtractedText extractedText = ic.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString().trim();
        if (TextUtils.isEmpty(block)) {
            block = currentText.subSequence(0, startIndex).toString();
            ic.setComposingRegion(0, startIndex);
        } else {
            ic.setComposingRegion(points[0], points[1]);
        }
        String value = "0.0001s";
        if (block.contains(value)) {
            value = ".5s";
        }
        Pattern p1 = Pattern.compile("^[\\d.]+\\s+");
        Matcher matcher = p1.matcher(block);
        if (matcher.find()) {
            value = matcher.group().trim() + "s";
        }
        String finalValue = value;
        Pattern pattern = Pattern.compile("(?<=dur=\")[^\"]+(?=\")");
        block = Shared.replace(pattern, matchResult -> finalValue, block);
        pattern = Pattern.compile("(?<=begin=\")[^\"]+(?=\")");
        block = Shared.replace(pattern, matchResult -> matchResult.group().replaceAll("[\\d.]+s", finalValue), block);
        ic.setComposingText(block, 1);
        ic.finishComposingText();

    }

    public static void copyString(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = Shared.getString(currentText.toString(), startIndex, endIndex);
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String s = currentText.subSequence(points[0], points[1]).toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                s
        ));
        database.insert(s);
    }

    public static void cutString(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getString(currentText.toString(), startIndex, endIndex);
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String s = currentText.subSequence(points[0], points[1]).toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                s
        ));
        database.insert(s);
        inputConnection.setComposingRegion(points[0], points[1]);
        inputConnection.setComposingText("", 1);
        inputConnection.finishComposingText();
    }

    public static void replaceBlock(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = Shared.getContinueLines(currentText.toString(), startIndex, endIndex);
        String s = currentText.subSequence(points[0], points[1]).toString();
        s = s.trim();
        String[] f = Shared.substringBefore(s, "\n").split(" ");
        if (f.length < 2) {
            return;
        }
        String b = Shared.substringAfter(s, "\n");
        try {
            b = b.replaceAll(f[0], f[1]);
        } catch (Exception e) {
        }
        inputConnection.setComposingRegion(points[0], points[1]);
        inputConnection.setComposingText(b, 1);
        inputConnection.finishComposingText();


    }

    public static void removeEmptyLines(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        if (currentText == null && currentText.length() == 0)
            return;
        String s = Shared.removeEmptyLines(currentText.toString());
        inputConnection.setComposingRegion(0, currentText.length());
        inputConnection.setComposingText(s, 1);
        inputConnection.finishComposingText();
    }
}