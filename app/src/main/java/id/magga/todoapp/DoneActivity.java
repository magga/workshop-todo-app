package id.magga.todoapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id.magga.todoapp.db.TaskContract;
import id.magga.todoapp.db.TaskDbHelper;

public class DoneActivity extends AppCompatActivity {
    private static final String TAG = "DoneActivity";

    private TaskDbHelper mHelper;

    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    private List<Integer> arrID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done);

        mHelper = new TaskDbHelper(this);

        mTaskListView = (ListView) findViewById(R.id.list_todo_done);
        arrID = new ArrayList<>();

        setTitle("Completed Task");

        DisplayValue();
    }

    private void DisplayValue() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(
                TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                TaskContract.TaskEntry.COL_TASK_DONE + " = 1 " ,
                null,
                null,
                null,
                null
        );

        arrID.clear();

        while (cursor.moveToNext()) {
            int curTitle = cursor.getColumnIndex(TaskContract.TaskEntry.COL_TASK_TITLE);
            int curID = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
            taskList.add(cursor.getString(curTitle));
            arrID.add(cursor.getInt(curID));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo_done,
                    R.id.task_title_done,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    public void DeleteTask(final View view) {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Are You Sure?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActualDelete(view);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create();
        alert.show();


    }

    public void ActualDelete(View view) {
        View parent = (View) view.getParent();
        final int pos = mTaskListView.getPositionForView(parent);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry._ID + " = " + arrID.get(pos),
                null);
        db.close();
        DisplayValue();
    }

    public void UndoneTask(View view) {
        View parent = (View) view.getParent();
        int pos = mTaskListView.getPositionForView(parent);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TaskContract.TaskEntry.COL_TASK_DONE, 0);
        db.update(
                TaskContract.TaskEntry.TABLE,
                cv,
                TaskContract.TaskEntry._ID + " = " + arrID.get(pos),
                null
        );
        db.close();
        DisplayValue();
    }
}
