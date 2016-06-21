package petrov.dmitry.testtask.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import petrov.dmitry.testtask.App;

public final class AppDataBase {

    private static AppDataBase instance = null;

    private static final String DB_NAME = "TestTaskDB";
    private static final int DB_VERSION = 1;

    // Таблицы
    private static final String TABLE_CLIENTS = "clients";
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Общие поля
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";

    // Поля записи клиент
    public static final String COLUMN_FIRST_NAME = "firstName";
    public static final String COLUMN_LAST_NAME = "lastName";
    public static final String COLUMN_MIDDLE_NAME = "middleName";
    public static final String COLUMN_PHONE_NUMBER = "phoneNumber";
    public static final String COLUMN_IMAGE = "image";

    // Поля для движения средств
    public static final String COLUMN_CLIENT_ID = "clientId";
    public static final String COLUMN_COST = "cost";

    // Создание таблиц
    private static final String DB_CREATE_CLIENTS =
            "create table " + TABLE_CLIENTS + "(" +
                    COLUMN_ID           + " integer primary key autoincrement, " +
                    COLUMN_FIRST_NAME	+ " text, " +
                    COLUMN_LAST_NAME	+ " text, " +
                    COLUMN_MIDDLE_NAME	+ " text, " +
                    COLUMN_PHONE_NUMBER + " text, " +
                    COLUMN_DATE			+ " text, " +
                    COLUMN_IMAGE		+ " blob " +
                    ");";

    private static final String DB_CREATE_TRANSACTIONS =
            "create table " + TABLE_TRANSACTIONS + "(" +
                    COLUMN_ID           + " integer primary key autoincrement, " +
                    COLUMN_CLIENT_ID	+ " integer, " +
                    COLUMN_COST     	+ " integer, " +
                    COLUMN_DATE			+ " text " +
                    ");";

    private final Context context;

    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase DB;

    public AppDataBase() {
        this.context = App.getAppContext();
        open();
    }

    public static synchronized AppDataBase getInstance(){
        if(instance == null){
            instance = new AppDataBase();
        }
        return instance;
    }

    // Открыть подключение
    public void open() {
        dataBaseHelper = new DataBaseHelper(context, DB_NAME, null, DB_VERSION);
        DB = dataBaseHelper.getWritableDatabase();
    }

    // Закрыть подключение
    public void close() {
        if (dataBaseHelper != null)
            dataBaseHelper.close();
    }

    @Nullable
    public Cursor getClient(long id) {
        if(DB.isOpen()) {
            Cursor cursor = DB.rawQuery("SELECT * FROM " + TABLE_CLIENTS  +
                    " where " + COLUMN_ID + " = ?", new String[] {Long.toString(id)});
            cursor.moveToFirst();
            return cursor;
        }
        return null;
    }

    @Nullable
    public Cursor getClients() {
        if(DB.isOpen()) {
            return DB.rawQuery("SELECT * FROM " + TABLE_CLIENTS, null);
        }
        return null;
    }

    @Nullable
    public Cursor getClients(String firstName) {
        if(DB.isOpen()) {
            return DB.query(true, TABLE_CLIENTS,
                    new String[] { COLUMN_ID, COLUMN_FIRST_NAME, COLUMN_LAST_NAME, COLUMN_MIDDLE_NAME, COLUMN_PHONE_NUMBER, COLUMN_DATE, COLUMN_IMAGE },
                    COLUMN_FIRST_NAME + " like '%" + firstName + "%'",
                    null, null, null, null, null);
        }
        return null;
    }


    @Nullable
    public Cursor getTransactions(long clientID) {
        if(DB.isOpen()) {
            return DB.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS  +
                    " where " + COLUMN_CLIENT_ID + " = ?", new String[] {Long.toString(clientID)});
        }
        return null;
    }

    public void clear() {
        if(DB.isOpen()) {
            DB.delete(TABLE_CLIENTS, null, null);
            DB.delete(TABLE_TRANSACTIONS, null, null);
        }
    }

    public void addClient(String firstName, String lastName, String middleName, String phone, String date, byte[] image) {
        if(DB.isOpen()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_FIRST_NAME, firstName);
            contentValues.put(COLUMN_LAST_NAME, lastName);
            contentValues.put(COLUMN_MIDDLE_NAME, middleName);
            contentValues.put(COLUMN_PHONE_NUMBER, phone);
            contentValues.put(COLUMN_DATE, date);
            contentValues.put(COLUMN_IMAGE, image);
            DB.insert(TABLE_CLIENTS, null, contentValues);

        }
    }

    public void addTransaction(long clientID, long cost) {
        if(DB.isOpen()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_CLIENT_ID, clientID);
            contentValues.put(COLUMN_COST, cost);
            SimpleDateFormat df = new SimpleDateFormat("dd.MMM.yyyy", Locale.ROOT);
            contentValues.put(COLUMN_DATE, df.format(Calendar.getInstance().getTime()));
            DB.insert(TABLE_TRANSACTIONS, null, contentValues);
        }
    }
    public boolean isOpen() {
        return DB.isOpen();
    }

    public void deleteClient(long id) {
        if(DB.isOpen())
            DB.delete(TABLE_CLIENTS, COLUMN_ID + " = " + id, null);
    }

    // Класс по созданию и управлению БД
    private class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Создаем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_CLIENTS);
            db.execSQL(DB_CREATE_TRANSACTIONS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
