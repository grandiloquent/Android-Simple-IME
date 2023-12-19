package psycho.euphoria.k;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import com.expression.parser.Parser;
import com.expression.parser.util.ParserResult;

public class Shared {

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

    public static void copyBlock(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
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

    public static void comment(Context context, InputConnection inputConnection) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
        int startIndex = extractedText.startOffset + extractedText.selectionStart;
        int endIndex = extractedText.startOffset + extractedText.selectionEnd;
        int[] points = getContinueLines(currentText.toString(), startIndex, endIndex);
        String block = currentText.subSequence(points[0], points[1]).toString();
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

    public static void paste(Context context, InputConnection inputConnection) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        inputConnection.setComposingText(clipboardManager.getText(), 1);
        inputConnection.finishComposingText();
    }

    public static void cut(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
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

    public static void cutBefore(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
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

    public static void cutAfter(Context context, InputConnection inputConnection, Database database) {
        ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
        CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
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

    public static void eval(Context context, InputConnection inputConnection) {
        CharSequence selectedText = inputConnection.getSelectedText(0);
        if (TextUtils.isEmpty(selectedText)) {
            ExtractedText extractedText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0);
            CharSequence currentText = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text;
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

    public static int[] getLine(String s, int start, int end) {
        while (start - 1 > -1 && s.charAt(start - 1) != '\n') {
            start--;
        }
        while (end + 1 < s.length() && s.charAt(end) != '\n') {
            end++;
        }
        return new int[]{start, end};
    }
}