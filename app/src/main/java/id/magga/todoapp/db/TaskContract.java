package id.magga.todoapp.db;

import android.provider.BaseColumns;

/**
 * Created by magga on 11/2/2017.
 */

public class TaskContract {
    public static final String DB_NAME = "id.magga.todoapp.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TASK_DONE = "done";
    }
}
