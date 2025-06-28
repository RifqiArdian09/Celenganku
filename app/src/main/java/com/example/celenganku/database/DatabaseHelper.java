package com.example.celenganku.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.celenganku.models.Target;
import com.example.celenganku.models.Transaksi;
import com.example.celenganku.utils.DateHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "celenganku.db";
    private static final int DATABASE_VERSION = 2;

    // Table and column names
    private static final String TABLE_TRANSAKSI = "transaksi";
    private static final String TABLE_TARGET = "target";

    // Common column
    private static final String COLUMN_ID = "id";

    // Transaction columns
    private static final String COLUMN_JENIS = "jenis";
    private static final String COLUMN_NOMINAL = "nominal";
    private static final String COLUMN_DESKRIPSI = "deskripsi";
    private static final String COLUMN_TANGGAL = "tanggal";

    // Target columns
    private static final String COLUMN_NAMA_TARGET = "nama";
    private static final String COLUMN_TARGET_NOMINAL = "target_nominal";
    private static final String COLUMN_TERKUMPUL = "terkumpul";
    private static final String COLUMN_TARGET_DATE = "target_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create transactions table
        String CREATE_TABLE_TRANSAKSI = "CREATE TABLE " + TABLE_TRANSAKSI + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_JENIS + " TEXT,"
                + COLUMN_NOMINAL + " INTEGER,"
                + COLUMN_DESKRIPSI + " TEXT,"
                + COLUMN_TANGGAL + " TEXT)";
        db.execSQL(CREATE_TABLE_TRANSAKSI);

        // Create targets table
        String CREATE_TABLE_TARGET = "CREATE TABLE " + TABLE_TARGET + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAMA_TARGET + " TEXT,"
                + COLUMN_TARGET_NOMINAL + " INTEGER,"
                + COLUMN_TERKUMPUL + " INTEGER DEFAULT 0,"
                + COLUMN_TARGET_DATE + " TEXT)";
        db.execSQL(CREATE_TABLE_TARGET);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSAKSI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TARGET);
        onCreate(db);
    }

    // TRANSACTION OPERATIONS
    public long addTransaksi(Transaksi transaksi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JENIS, transaksi.getJenis());
        values.put(COLUMN_NOMINAL, transaksi.getNominal());
        values.put(COLUMN_DESKRIPSI, transaksi.getDeskripsi());
        values.put(COLUMN_TANGGAL, DateHelper.dateToString(transaksi.getTanggal()));
        return db.insert(TABLE_TRANSAKSI, null, values);
    }

    public List<Transaksi> getAllTransaksi() {
        List<Transaksi> transaksiList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TRANSAKSI + " ORDER BY " + COLUMN_TANGGAL + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Transaksi transaksi = new Transaksi(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3),
                        DateHelper.stringToDate(cursor.getString(4))
                );
                transaksiList.add(transaksi);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transaksiList;
    }

    // TARGET OPERATIONS
    public long addTarget(Target target) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAMA_TARGET, target.getNama());
        values.put(COLUMN_TARGET_NOMINAL, target.getTargetNominal());
        values.put(COLUMN_TARGET_DATE, DateHelper.dateToString(target.getTargetDate()));
        return db.insert(TABLE_TARGET, null, values);
    }

    public List<Target> getAllTarget() {
        List<Target> targetList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_TARGET + " ORDER BY " + COLUMN_TARGET_DATE + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Target target = new Target(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        DateHelper.stringToDate(cursor.getString(4))
                );
                targetList.add(target);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return targetList;
    }

    public Target getTargetById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TARGET,
                new String[]{COLUMN_ID, COLUMN_NAMA_TARGET, COLUMN_TARGET_NOMINAL, COLUMN_TERKUMPUL, COLUMN_TARGET_DATE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Target target = new Target(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getInt(2),
                cursor.getInt(3),
                DateHelper.stringToDate(cursor.getString(4))
        );
        cursor.close();
        return target;
    }

    public int updateTerkumpul(int targetId, int newTerkumpul) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TERKUMPUL, newTerkumpul);

        return db.update(TABLE_TARGET,
                values,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(targetId)});
    }

    public int deleteTarget(int targetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_TARGET,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(targetId)});
    }

    // BALANCE CALCULATION
    public int getTotalSaldo() {
        SQLiteDatabase db = this.getReadableDatabase();
        int saldo = 0;

        // Calculate total income
        String queryIncome = "SELECT SUM(" + COLUMN_NOMINAL + ") FROM " + TABLE_TRANSAKSI +
                " WHERE " + COLUMN_JENIS + " = 'masuk'";
        Cursor cursorIncome = db.rawQuery(queryIncome, null);
        if (cursorIncome.moveToFirst()) {
            saldo = cursorIncome.getInt(0);
        }
        cursorIncome.close();

        // Subtract total expenses
        String queryExpense = "SELECT SUM(" + COLUMN_NOMINAL + ") FROM " + TABLE_TRANSAKSI +
                " WHERE " + COLUMN_JENIS + " = 'keluar'";
        Cursor cursorExpense = db.rawQuery(queryExpense, null);
        if (cursorExpense.moveToFirst()) {
            saldo -= cursorExpense.getInt(0);
        }
        cursorExpense.close();

        return saldo;
    }

    // Add money to specific target
    public int addToTarget(int targetId, int amount) {
        Target target = getTargetById(targetId);
        int newTerkumpul = target.getTerkumpul() + amount;
        return updateTerkumpul(targetId, newTerkumpul);
    }
}