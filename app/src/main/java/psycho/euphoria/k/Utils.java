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
        ColorAdapter colorAdapter=new ColorAdapter(context);
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
}