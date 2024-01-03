package psycho.euphoria.k;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.IpConfiguration;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static psycho.euphoria.k.Shared.getContinueLines;
import static psycho.euphoria.k.Shared.getLine;
import static psycho.euphoria.k.Shared.getString;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static psycho.euphoria.k.Shared.getWord;

public class Utils {
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

    public static void getIds(Context context, InputConnection inputConnection) {
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        List<String> ids = Shared.collectIds(currentText.toString());
        StringBuilder sb = new StringBuilder();
        for (String s : ids) {
            sb.append(s).append("\n");
        }
        inputConnection.commitText(sb.toString(), 1);
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

    public static void replaceBlock(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = Shared.getContinueLines(currentText.toString(), startIndex, endIndex);
        String s = currentText.subSequence(points[0], points[1]).toString();
        s = s.trim();
        String[] f = Shared.substringBefore(s, "\n").split(" ");
        if (f.length < 1) {
            return;
        }
        if (f.length == 1) {
            f = new String[]{f[0], ""};
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

    public static void comment(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString().trim();
        if (block.startsWith("<!--")) {
            block = block.substring("<!--".length());
            if (block.endsWith("-->")) {
                block = block.substring(0, block.length() - "<!--".length() + 1);
            }
        } else {
            block = "<!--" + block + "-->";
        }
        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            inputConnection.replaceText(points[0], points[1], block,
                    startIndex + block.length(), null);
        } else {
            inputConnection.setComposingRegion(points[0], points[1]);
            inputConnection.setComposingText(block, 1);
            inputConnection.finishComposingText();
        }
    }

    public static void commentLine(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getLine(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString().trim();
        if (block.startsWith("// ")) {
            block = block.substring("// ".length());
        } else {
            block = "// " + block;
        }
        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            inputConnection.replaceText(points[0], points[1], block,
                    startIndex + block.length(), null);
        } else {
            inputConnection.setComposingRegion(points[0], points[1]);
            inputConnection.setComposingText(block, 1);
            inputConnection.finishComposingText();
        }
    }

    public static void search(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getWord(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString().trim();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("q",block);
        context.startActivity(intent);
    }

    public static void copyLine(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getLine(currentText.toString(), startIndex, endIndex);
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String s = currentText.subSequence(points[0], points[1]).toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                s
        ));
        database.insert(s);
    }

    public static void cutLine(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getLine(currentText.toString(), startIndex, endIndex);
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

    public static void commentJavaScript(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString().trim();
        Log.e("B5aOx2", String.format("commentJavaScript, %s", block));
        if (block.startsWith("/*")) {
            block = block.substring("/*".length());
            if (block.endsWith("*/")) {
                block = block.substring(0, block.length() - "*/".length());
            }
        } else {
            block = "/*" + block + "*/";
        }
        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            inputConnection.replaceText(points[0], points[1], block,
                    startIndex + block.length(), null);
        } else {
            inputConnection.setComposingRegion(points[0], points[1]);
            inputConnection.setComposingText(block, 1);
            inputConnection.finishComposingText();
        }
    }

    public static void createFloatView(Context context, String s) {
        final WindowManager.LayoutParams params;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获取屏幕的高度
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        //设置window type
        //设置图片格式，效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        //调整悬浮窗显示的停靠位置为右侧侧置顶，方便实现触摸滑动
        params.gravity = Gravity.LEFT | Gravity.TOP;
        // 以屏幕左上角为原点，设置x、y初始值
//        params.width = width;
//        params.x = width / 6 / 2;
//        params.height = height;
//        params.y = height / 3 / 2;
        //设置悬浮窗口长宽数据
        LayoutInflater inflater = LayoutInflater.from(context);
        //获取浮动窗口视图所在布局
        View layout = inflater.inflate(R.layout.float_layout, null);
//        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        layout.measure(w, h);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        int margin = max(width, height) / 8; //dpToPx(context, 30);
        if (width > height)
            layoutParams.setMargins(margin << 1, margin, margin << 1, margin);
        else
            layoutParams.setMargins(margin >> 1, margin << 1, margin >> 1, margin << 1);
        layout.findViewById(R.id.layout).setLayoutParams(layoutParams);
        //添加mFloatLayout
        windowManager.addView(layout, params);
        ((TextView) layout.findViewById(R.id.dst)).setText(s);
        layout.findViewById(R.id.layout)
                .setOnClickListener(v -> {
                    ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    manager.setPrimaryClip(ClipData.newPlainText(null, s));
                    Toast.makeText(context, "已复制到剪切板", Toast.LENGTH_SHORT).show();
                });
        layout.setOnClickListener(v -> windowManager.removeView(layout));
//        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        //设置layout大小
//        mFloatLayout.measure(w, h);
        //设置监听浮动窗口的触摸移动
    }

    public static void translate(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getWord(currentText.toString(), startIndex, endIndex);
        String s = currentText.subSequence(points[0], points[1]).toString();
        new Thread(() -> {
            String sv = null;
            try {
                sv = TranslatorApi.translate(s);
            } catch (Exception e) {
                Log.e("B5aOx2", String.format("translate, %s", e.getMessage()));
            }
            String finalS = sv;
            Shared.postOnMainThread(() -> {
                Utils.createFloatView(context, finalS);
            });
        }).start();
    }

    public static void format(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString().trim();
        StringBuilder stringBuilder = new StringBuilder();
        String[] array = block.split("\n");
        for (int i = 0; i < array.length; i++) {
            if (!array[i].trim().isEmpty())
                stringBuilder.append(array[i].trim()).append(' ');
        }
        block = stringBuilder.toString().replaceAll("\\s+-\\s+", "");
        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            inputConnection.replaceText(points[0], points[1], block,
                    startIndex + block.length(), null);
        } else {
            inputConnection.setComposingRegion(points[0], points[1]);
            inputConnection.setComposingText(block, 1);
            inputConnection.finishComposingText();
        }
    }

}