package psycho.euphoria.k;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;

public class Utils {
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
                        ic.commitText(noteAdapter.getItem(i), 1);
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

}