package psycho.euphoria.k;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ColorAdapter extends BaseAdapter {
    private String[] mColors = new String[]{
            "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#9E9E9E", "#607D8B",
            "#B71C1C", "#880E4F", "#4A148C", "#311B92", "#1A237E", "#0D47A1", "#01579B", "#006064", "#004D40", "#1B5E20", "#33691E", "#827717", "#F57F17", "#FF6F00", "#E65100", "#BF360C", "#3E2723", "#212121", "#263238",
            "#D50000", "#C51162", "#AA00FF", "#6200EA", "#304FFE", "#2962FF", "#0091EA", "#00B8D4", "#00BFA5", "#00C853", "#64DD17", "#AEEA00", "#FFD600", "#FFAB00", "#FF6D00", "#DD2C00"
    };
    private final Context mContext;

    public ColorAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mColors.length;
    }

    @Override
    public String getItem(int i) {
        return mColors[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView;
        if (view == null) {
            textView = new TextView(mContext);
            view = textView;
        } else {
            textView = (TextView) view;
        }
        textView.setBackgroundColor(Color.parseColor(mColors[i]));
        textView.setTextColor(Color.WHITE);
        textView.setText(mColors[i]);
        textView.setTextSize(32);

        return view;
    }

    private class ViewHolder {
        TextView textView;
    }
}