package psycho.euphoria.k;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;


public class Database extends SQLiteOpenHelper {
    public Database(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE if not EXISTS \"text\" (\n" +
                "\t\"id\"\tINTEGER NOT NULL UNIQUE,\n" +
                "\t\"content\"\tTEXT,\n" +
                "\t\"create_at\"\tINTEGER,\n" +
                "\t\"update_at\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                ");");
        sqLiteDatabase.execSQL("CREATE TABLE if not EXISTS \"note\" (\n" +
                "\t\"id\"\tINTEGER NOT NULL UNIQUE,\n" +
                "\t\"title\"\tUNIQUE,\n" +
                "\t\"content\"\tTEXT,\n" +
                "\t\"create_at\"\tINTEGER,\n" +
                "\t\"update_at\"\tINTEGER,\n" +
                "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                ");");

    }

    public void insert(String text) {
        ContentValues values = new ContentValues();
        values.put("content", text.trim());
        values.put("create_at", System.currentTimeMillis());
        values.put("update_at", System.currentTimeMillis());
        getWritableDatabase().insert("text", null, values);
    }

    public List<String> listText() {
        List<String> texts = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT content FROM text ORDER BY update_at DESC LIMIT 20", null);
        while (cursor.moveToNext()) {
            texts.add(cursor.getString(0));
        }
        cursor.close();
        return texts;
    }

    public List<String> listNote() {

        List<String> texts = new ArrayList<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT title FROM note ORDER BY update_at DESC", null);
        while (cursor.moveToNext()) {
            texts.add(cursor.getString(0));
        }
        cursor.close();
        return texts;
    }

    public void insert(String title, String content) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("create_at", System.currentTimeMillis());
        values.put("update_at", System.currentTimeMillis());
        getWritableDatabase().insert("note", null, values);
    }
    public void delete(String title){
        getWritableDatabase().delete("note","title = ?",new String[]{
                title
        });
    }

    public String getText() {
        String text = null;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT content FROM text ORDER BY update_at DESC LIMIT 1", null);
        if (cursor.moveToNext()) {
            text = cursor.getString(0);
        }
        cursor.close();
        return text;
    }
    public String getContent(String title) {
        String text = null;
        Cursor cursor = getReadableDatabase().rawQuery("SELECT content FROM note where title = ?", new String[]{
                title
        });
        if (cursor.moveToNext()) {
            text = cursor.getString(0);
        }
        cursor.close();
        return text;
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}