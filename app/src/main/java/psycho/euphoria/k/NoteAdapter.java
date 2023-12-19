package psycho.euphoria.k;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteAdapter extends BaseAdapter {

    private List<String> mNotes;
    private final Context mContext;

    public NoteAdapter(Context context, List<String> notes) {
        mContext = context;
        mNotes = notes;
        if (mNotes == null) {
            mNotes = new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        return mNotes.size();
    }

    @Override
    public String getItem(int i) {
        return mNotes.get(i);
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
        textView.setTextColor(Color.BLACK);
        textView.setText(mNotes.get(i));
        textView.setTextSize(24);
        return view;
    }

}