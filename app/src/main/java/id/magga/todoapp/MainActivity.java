package id.magga.todoapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import id.magga.todoapp.db.TaskContract;
import id.magga.todoapp.db.TaskDbHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TaskDbHelper mHelper;

    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    private List<Integer> arrID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new TaskDbHelper(this);

        mTaskListView = (ListView) findViewById(R.id.list_todo);
        arrID = new ArrayList<>();

        DisplayValue();
    }

    @Override
    protected void onResume() {
        super.onResume();

        DisplayValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                final EditText taskEditText = new EditText(this);
//                taskEditText.setPadding(10, 0, 10, 0);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Add a new task")
                        .setMessage("What do you want to do next?")
                        .setView(taskEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String task = String.valueOf(taskEditText.getText());
                                InsertValue(task);
                                DisplayValue();
                                Log.d(TAG, "Task to add: " + task);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

                return true;

            case R.id.action_view_completed:
                Intent intent = new Intent(this, DoneActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void InsertValue(String task) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TaskContract.TaskEntry.COL_TASK_TITLE, task);
        values.put(TaskContract.TaskEntry.COL_TASK_DONE, false);
        db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    private void DisplayValue() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(
                            TaskContract.TaskEntry.TABLE,
                            new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_TASK_TITLE},
                            TaskContract.TaskEntry.COL_TASK_DONE + " = 0 " ,
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

        Log.i(TAG, taskList.toString());

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.item_todo,
                    R.id.task_title,
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

    public void DoneTask(View view) {
        View parent = (View) view.getParent();
        int pos = mTaskListView.getPositionForView(parent);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TaskContract.TaskEntry.COL_TASK_DONE, 1);
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
