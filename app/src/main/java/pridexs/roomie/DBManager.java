package pridexs.roomie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;
import java.util.HashMap;

public class DBManager {
    private static DBManager mInstance;

    private static final String TAG = DBManager.class.getSimpleName();

    public static final int DATABASE_VERSION = 1;

    private final Context context;
    private MyDatabaseHelper DBHelper;
    private SQLiteDatabase db;
    // Counts how many Threads have opened the DB so we don't close the instance accidentaly.
    private int counterDB = 0;

    // USER TABLE
    private static final String TABLE_USER = "user";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_LAST_UPDATED = "last_updated";
    private static final String KEY_API_KEY = "api_key";

    //HOUSE TABLE
    private static final String TABLE_HOUSE = "house";
    private static final String KEY_HOUSEID = "houseID";

    //HOUSE_MEMBER
    private static final String TABLE_HOUSE_MEMBER = "house_member";
    private static final String KEY_IS_ADMIN = "isAdmin";

    /*
     * Code from http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
     */
    public static synchronized DBManager getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DBManager(context.getApplicationContext());
        }
        return mInstance;
    }

    private DBManager(Context context)
    {
        this.context = context;
        DBHelper = new MyDatabaseHelper(context);
    }

    private static class MyDatabaseHelper extends SQLiteOpenHelper
    {
        public static final String DATABASE_NAME = "Roomie.db";

        // STRINGS TO CREATE THE TABLES
        private static final String SQL_CREATE_USER =
                "CREATE TABLE " + TABLE_USER + "(" +
                        KEY_NAME + " TEXT NOT NULL," +
                        KEY_EMAIL + " TEXT PRIMARY KEY," +
                        KEY_CREATED_AT + " TEXT NOT NULL," +
                        KEY_API_KEY + " TEXT" + ")";

        private static final String SQL_CREATE_HOUSE =
                "CREATE TABLE " + TABLE_HOUSE + "(" +
                        KEY_HOUSEID + " INTEGER NOT NULL PRIMARY KEY," +
                        KEY_NAME + " TEXT NOT NULL," +
                        KEY_LAST_UPDATED + " TEXT DEFAULT CURRENT_DATE" +
                        ")";

        private static final String SQL_CREATE_HOUSE_MEMBER =
                "CREATE TABLE " + TABLE_HOUSE_MEMBER + "(" +
                        KEY_EMAIL + " TEXT NOT NULL," +
                        KEY_HOUSEID + " INTEGER NOT NULL," +
                        KEY_IS_ADMIN + " INTEGER NOT NULL DEFAULT 0," +
                        "FOREIGN KEY (" + KEY_EMAIL + ") REFERENCES " + TABLE_USER + "(" +
                        KEY_EMAIL + ")," +
                        "FOREIGN KEY (" + KEY_HOUSEID + ") REFERENCES " + TABLE_HOUSE + "(" +
                        KEY_HOUSEID + ")," +
                        "PRIMARY KEY (" + KEY_EMAIL + "," + KEY_HOUSEID + ")" +
                        ")";

        public MyDatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(SQL_CREATE_USER);
            db.execSQL(SQL_CREATE_HOUSE);
            db.execSQL(SQL_CREATE_HOUSE_MEMBER);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            // DROP ALL TABLES
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSE_MEMBER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSE);

            // CREATE TABLES AGAIN
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    public DBManager open() throws SQLException
    {
        counterDB++;
        if (counterDB == 1) {
            db = DBHelper.getWritableDatabase();
        }
        return this;
    }

    public void close()
    {
        counterDB--;
        if (counterDB == 0) {
            DBHelper.close();
        }
    }

    /*
     * BEGIN - LOGIN / REGISTER
     */
    public long addUser(String name, String email, String created_at, String api_key) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_API_KEY, api_key); // Api key

        return db.insert(TABLE_USER, null, values);
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(0));
            user.put("email", cursor.getString(1));
            user.put("created_at", cursor.getString(2));
            user.put("api_key", cursor.getString(3));
        }
        cursor.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public void deleteUsers() {
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
    }
    /*
     * END - LOGIN / REGISTER
     */

    public void deleteHouse() {
        // DELETE ALL HOUSE INFO SYNCE IT NEEDS SYNC
        db.delete(TABLE_HOUSE_MEMBER, null, null);
        db.delete(TABLE_HOUSE, null, null);
    }

    public long addHouse(int houseID, String name) {
        ContentValues values = new ContentValues();
        values.put(KEY_HOUSEID, houseID); // houseID
        values.put(KEY_NAME, name); // Name

        return db.insert(TABLE_HOUSE, null, values);
    }

    public long addHouseMember(int houseID, String email, int isAdmin) {
        ContentValues values = new ContentValues();
        values.put(KEY_HOUSEID, houseID); // houseID
        values.put(KEY_EMAIL, email); // Name
        values.put(KEY_IS_ADMIN, isAdmin);

        return db.insert(TABLE_HOUSE_MEMBER, null, values);
    }

    public HashMap<String, String> getHouseDetails() {
        HashMap<String, String> house = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_HOUSE;

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            house.put("houseID", cursor.getString(0));
            house.put("name", cursor.getString(1));
            house.put("last_updated", cursor.getString(2));
        }
        cursor.close();
        // return user
        Log.d(TAG, "Fetching house from Sqlite: " + house.toString());

        return house;
    }

    public void synchronyze() {

    }

}
