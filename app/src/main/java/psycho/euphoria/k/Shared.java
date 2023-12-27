package psycho.euphoria.k;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
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

import com.expression.parser.Parser;
import com.expression.parser.util.ParserResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Shared {

    private static volatile Handler sMainThreadHandler;

    public static List<String> collectIds(String s) {
        Pattern idPattern = Pattern.compile("(?<=id=\")[^\"]+(?=\")");
        Matcher matcher = idPattern.matcher(s);
        List<String> array = new ArrayList<>();
        while (matcher.find()) {
            array.add(matcher.group());
        }
        array.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return array;

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

    public static void copyBlock(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String s = currentText.subSequence(points[0], points[1]).toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                s
        ));
        database.insert(s);
    }

    public static void cut(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
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

    public static void cutAfter(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        if (startIndex + 1 == currentText.length()) return;
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String s = currentText.subSequence(startIndex, currentText.length()).toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                s
        ));
        database.insert(s);
        inputConnection.setComposingRegion(startIndex, currentText.length());
        inputConnection.setComposingText("", 1);
        inputConnection.finishComposingText();
    }

    public static void cutBefore(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = extractedText.text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        if (startIndex == 0) return;
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        String s = currentText.subSequence(0, startIndex).toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                s
        ));
        database.insert(s);
        inputConnection.setComposingRegion(0, startIndex);
        inputConnection.setComposingText("", 1);
        inputConnection.finishComposingText();
    }

    public static void eval(Context context, InputConnection inputConnection) {
        CharSequence selectedText = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(selectedText)) {
            ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
            CharSequence currentText = extractedText.text;
            int startIndex = extractedText.startOffset + extractedText.selectionStart;
            int endIndex = extractedText.startOffset + extractedText.selectionEnd;
            int[] points = getLine(currentText.toString(), startIndex, endIndex);
            String block = currentText.subSequence(points[0], points[1]).toString().trim();
            ParserResult result = Parser.eval(block);
            block = String.format("%s\n%s", block, result.getValue());
            inputConnection.setComposingRegion(points[0], points[1]);
            inputConnection.setComposingText(block, 1);
            inputConnection.finishComposingText();
        } else {
            ParserResult result = Parser.eval(selectedText.toString());
            inputConnection.commitText(String.format("%s", result.getValue()), 1);
        }

    }

    public static int[] getContinueLines(String s, int start, int end) {
        while (start - 1 > -1) {
            if (s.charAt(start - 1) != '\n')
                start--;
            else {
                int temp = start;
                while (temp - 1 > -1 && Character.isWhitespace(s.charAt(temp - 1))) {
                    temp--;
                }
                String tempString = s.substring(temp, start);
                int count = 0;
                for (int i = 0; i < tempString.length(); i++) {
                    if (tempString.charAt(i) == '\n') {
                        count++;
                    }
                    if (count > 1) {
                        break;
                    }
                }
                if (count > 1) {
                    break;
                }
                start--;
            }
        }
        while (end + 1 < s.length()) {
            if (s.charAt(end) != '\n')
                end++;
            else {
                int temp = end;
                while (temp + 1 < s.length() && Character.isWhitespace(s.charAt(temp))) {
                    temp++;
                }
                String tempString = s.substring(end, temp);
                int count = 0;
                for (int i = 0; i < tempString.length(); i++) {
                    if (tempString.charAt(i) == '\n') {
                        count++;
                    }
                    if (count > 1) {
                        break;
                    }
                }
                if (count > 1) {
                    break;
                }
                end++;
            }
        }
        return new int[]{start, end};


    }

    public static int[] getLine(String s, int start, int end) {
        while (start - 1 > -1 && s.charAt(start - 1) != '\n') {
            start--;
        }
        while (end + 1 < s.length() && s.charAt(end) != '\n') {
            end++;
        }
        return new int[]{start, end};
    }

    public static int[] getString(String s, int start, int end) {
        if (!Character.isWhitespace(s.charAt(start))) {
            while (start - 1 > -1 && !Character.isWhitespace(s.charAt(start - 1))) {
                start--;
            }
        }
        if (!Character.isWhitespace(s.charAt(end))) {
            while (end < s.length() && !Character.isWhitespace(s.charAt(end))) {
                end++;
            }
        }
        return new int[]{start, end};
    }

    public static Handler getUiThreadHandler() {
        if (sMainThreadHandler == null) {
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }
        return sMainThreadHandler;
    }

    public static int[] getWord(String s, int start, int end) {
        while (start - 1 > -1 && Character.isAlphabetic(s.charAt(start - 1))) {
            start--;
        }
        while (end + 1 < s.length() && Character.isAlphabetic(s.charAt(end))) {
            end++;
        }
        return new int[]{start, end};
    }

    public static boolean isWhiteSpace(String s) {
        if (s == null || s.length() == 0) return true;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) return false;
        }
        return true;
    }

    public static void paste(Context context, InputConnection inputConnection) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        inputConnection.setComposingText(clipboardManager.getText(), 1);
        inputConnection.finishComposingText();
    }

    public static void postOnMainThread(Runnable runnable) {
        getUiThreadHandler().post(runnable);
    }

    public static String readString(HttpURLConnection connection) {
        InputStream in;
        BufferedReader reader = null;
        try {
            String contentEncoding = connection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.equals("gzip")) {
                in = new GZIPInputStream(connection.getInputStream());
            } else {
                in = connection.getInputStream();
            }
            /*
            "implementation group": "org.brotli', name: 'dec', version: '0.1.1",
            else if (contentEncoding != null && contentEncoding.equals("br")) {
                in = new BrotliInputStream(connection.getInputStream());
            } */
            //  if (contentEncoding != null && contentEncoding.equals("br")) {
            //in = new BrotliInputStream(connection.getInputStream());
            //  }
            reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
            return sb.toString();
        } catch (Exception ignored) {
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public static String readText(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            CharSequence c = clipData.getItemAt(0).getText();
            if (c != null)
                return c.toString();
        }
        return null;

    }

    public static String removeEmptyLines(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = s.split("\n");
        for (String ss : lines) {
            if (isWhiteSpace(ss)) continue;
            stringBuilder.append(ss).append('\n');
        }
        return stringBuilder.toString();
    }

    public static String replace(Pattern pattern, Function<MatchResult, String> callback, CharSequence subject) {
        Matcher m = pattern.matcher(subject);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, callback.apply(m.toMatchResult()));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String substring(String string, String first, String second) {
        int start = string.indexOf(first);
        if (start == -1) return null;
        start += first.length();
        int end = string.indexOf(second, start);
        if (end == -1) return null;
        return string.substring(start, end);
    }

    public static String substringAfter(String string, char delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(index + 1);
        return string;
    }

    public static String substringAfter(String string, String delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(index + delimiter.length());
        return string;
    }

    public static String substringAfterLast(String string, char delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(index + 1);
        return string;
    }

    public static String substringAfterLast(String string, String delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(index + delimiter.length());
        return string;
    }

    public static String substringBefore(String string, char delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

    public static String substringBefore(String string, String delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

    public static String substringBeforeLast(String string, char delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

    public static String substringBeforeLast(String string, String delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

}