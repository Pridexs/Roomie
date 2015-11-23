/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

/*
 * Manages the Databases.
 */
public class DBManager {
    private static DBManager mInstance;

    public static final int DATABASE_VERSION = 1;

    private final Context context;
    private MyDatabaseHelper DBHelper;
    private SQLiteDatabase db;

    private static final String KEY_ID = "_id";

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

    //NOTE
    private static final String TABLE_NOTE = "note";
    private static final String KEY_CREATED_BY = "createdBy";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_WAS_DELETED = "wasDeleted";

    /*
     * Code from http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
     * Basically, there is only one instance of DBManager so we can control better when the DB is opened/closed
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
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        KEY_NAME + " TEXT NOT NULL," +
                        KEY_EMAIL + " TEXT NOT NULL," +
                        KEY_CREATED_AT + " TEXT," +
                        KEY_LAST_UPDATED + " TEXT DEFAULT \"0000-00-00 00:00:00\"," +
                        KEY_API_KEY + " TEXT," +
                        "UNIQUE(" + KEY_EMAIL + "," + KEY_ID + ")" +
                        ")";

        private static final String SQL_CREATE_HOUSE =
                "CREATE TABLE " + TABLE_HOUSE + "(" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        KEY_HOUSEID + " INTEGER NOT NULL," +
                        KEY_NAME + " TEXT NOT NULL," +
                        KEY_LAST_UPDATED + " TEXT DEFAULT CURRENT_DATE," +
                        "UNIQUE(" + KEY_HOUSEID + "," + KEY_ID + ")" +
                        ")";

        private static final String SQL_CREATE_HOUSE_MEMBER =
                "CREATE TABLE " + TABLE_HOUSE_MEMBER + "(" +
                        KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        KEY_EMAIL + " TEXT NOT NULL," +
                        KEY_HOUSEID + " INTEGER NOT NULL," +
                        KEY_IS_ADMIN + " INTEGER NOT NULL DEFAULT 0," +
                        "FOREIGN KEY (" + KEY_EMAIL + ") REFERENCES " + TABLE_USER + "(" +
                        KEY_EMAIL + ")," +
                        "FOREIGN KEY (" + KEY_HOUSEID + ") REFERENCES " + TABLE_HOUSE + "(" +
                        KEY_HOUSEID + ")," +
                        "UNIQUE (" + KEY_EMAIL + "," + KEY_HOUSEID + ")" +
                        ")";

        private static final String SQL_CREATE_NOTE =
                "CREATE TABLE " + TABLE_NOTE + "(" +
                        KEY_ID + " INTEGER NOT NULL," +
                        KEY_CREATED_BY + " TEXT NOT NULL," +
                        KEY_HOUSEID + " INTEGER NOT NULL," +
                        KEY_NAME + " TEXT NOT NULL," +
                        KEY_DESCRIPTION + " TEXT," +
                        KEY_LAST_UPDATED + " TEXT DEFAULT CURRENT_DATE," +
                        KEY_CREATED_AT + " TEXT NOT NULL," +
                        KEY_WAS_DELETED + " INT DEFAULT 0," +
                        "FOREIGN KEY (" + KEY_HOUSEID + ") REFERENCES " + TABLE_HOUSE + "(" +
                        KEY_HOUSEID + ")," +
                        "FOREIGN KEY (" + KEY_CREATED_BY + ") REFERENCES " + TABLE_USER + "(" +
                        KEY_EMAIL + ")," +
                        "UNIQUE (" + KEY_ID + ")" +
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
            db.execSQL(SQL_CREATE_NOTE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            // DROP ALL TABLES
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
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
        if (db == null) {
            db = DBHelper.getWritableDatabase();
        }
        return this;
    }

    public long addUser(String name, String email, String created_at, String api_key) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_API_KEY, api_key); // Api key

        return db.insert(TABLE_USER, null, values);
    }

    public long addUser(String name, String email) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email

        return db.insert(TABLE_USER, null, values);
    }

    // Gets the app user, he is the only one that has API_KEY set.
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " WHERE " + KEY_API_KEY + " IS NOT null";

        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(cursor.getColumnIndexOrThrow("name")));
            user.put("email", cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.put("created_at", cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
            user.put("last_updated", cursor.getString(cursor.getColumnIndexOrThrow("last_updated")));
            user.put("api_key", cursor.getString(cursor.getColumnIndexOrThrow("api_key")));
        }
        cursor.close();

        return user;
    }

    public void deleteUsers() {
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
    }

    public long updateHouseMember(String email, int isAdmin) {
        ContentValues values = new ContentValues();
        values.put(KEY_IS_ADMIN, isAdmin);

        return db.update(TABLE_HOUSE_MEMBER, values, KEY_EMAIL + "=?", new String[]{email});
    }

    public long updateUser(String email, String name) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME , name);

        return db.update(TABLE_USER, values, KEY_EMAIL + "=?", new String[]{email});
    }

    public void updateLastUpdated() {
        db.execSQL("UPDATE " + TABLE_USER + " SET " + KEY_LAST_UPDATED + " =DATETIME(\'now\') WHERE " +
                KEY_API_KEY + " IS NOT null");
    }

    public void deleteHouse() {
        db.delete(TABLE_NOTE, null , null);
        db.delete(TABLE_HOUSE_MEMBER, null, null);
        db.delete(TABLE_HOUSE, null, null);
    }

    public long addHouse(int houseID, String name, String last_updated) {
        ContentValues values = new ContentValues();
        values.put(KEY_HOUSEID, houseID); // houseID
        values.put(KEY_NAME, name); // Name
        values.put(KEY_LAST_UPDATED, last_updated);

        return db.insert(TABLE_HOUSE, null, values);
    }

    public long updateHouse(int houseID, String name, String last_updated) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_LAST_UPDATED, last_updated);

        return db.update(TABLE_HOUSE, values, KEY_HOUSEID + "=?", new String[]{Integer.toString(houseID)});
    }

    public long updateNote(int noteId, String name, String description,
                           String last_updated) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_LAST_UPDATED, last_updated);

        return db.update(TABLE_NOTE, values, KEY_ID + "=?", new String[]{Integer.toString(noteId)});
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
            house.put("houseID", cursor.getString(cursor.getColumnIndexOrThrow("houseID")));
            house.put("name", cursor.getString(cursor.getColumnIndexOrThrow("name")));
            house.put("last_updated", cursor.getString(cursor.getColumnIndexOrThrow("last_updated")));
        }
        cursor.close();

        return house;
    }

    public Cursor getCursorHouseMembers() {
        String selectQuery = "SELECT hm." + KEY_ID + ", u." + KEY_EMAIL + ", u." + KEY_NAME + ", hm." + KEY_IS_ADMIN +
                " FROM " + TABLE_HOUSE_MEMBER + " as hm INNER JOIN " +
                TABLE_USER + " as u ON hm." + KEY_EMAIL + " = " +  " u." + KEY_EMAIL;

        return db.rawQuery(selectQuery, null);
    }

    public boolean isUserOnDb(String email) {
        String[] values = new String[] { email };
        Cursor c = db.query(TABLE_USER, null, KEY_EMAIL + "= ?", values, null, null, null);
        return c.moveToFirst();
    }

    public boolean isUserAdmin(String email) {
        String[] values = new String[] { email, Integer.toString(1) };
        String whereStatement =  KEY_EMAIL + "=? AND " + KEY_IS_ADMIN + "=?";
        Cursor c = db.query(TABLE_HOUSE_MEMBER, null, whereStatement, values, null, null, null);
        return c.moveToFirst();
    }

    public boolean isNoteOnDb(int noteId) {
        String[] values = new String[]{Integer.toString(noteId)};
        Cursor c = db.query(TABLE_NOTE, null, KEY_ID + "=?", values, null, null, null);

        return c.moveToFirst();
    }

    public long addNote(int noteId, String name, String description, String createdBy,
                        String created_at, String last_updated, int houseId) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, noteId);
        values.put(KEY_NAME, name);
        values.put(KEY_DESCRIPTION, description);
        values.put(KEY_CREATED_BY, createdBy);
        values.put(KEY_CREATED_AT, created_at);
        values.put(KEY_LAST_UPDATED, last_updated);
        values.put(KEY_HOUSEID, houseId);

        return db.insert(TABLE_NOTE, null, values);
    }

    public Cursor getCursorNotes() {
        String selectQuery = "SELECT u." + KEY_NAME + " as memberName, n.*" +
                " FROM " + TABLE_NOTE + " as n INNER JOIN " +
                TABLE_USER + " as u ON n." + KEY_CREATED_BY + " = " +  " u." + KEY_EMAIL +
                " ORDER BY " + KEY_LAST_UPDATED + " DESC";
        return db.rawQuery(selectQuery, null);
    }

    public int deleteNote(int noteId) {
        String whereClause = KEY_ID + "=?";
        String[] args = {Integer.toString(noteId)};
        return db.delete(TABLE_NOTE, whereClause, args);
    }

}
