package edu.umd.cs.pollsternav.service.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nachmi on 4/25/17.
 */
public class PollsterDBHelper extends SQLiteOpenHelper {
    Context context;
    static final String DB_NAME = "user_specifics.db";
    static final int VERSION = 1;

    public PollsterDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    /* Override onCreate(SQLiteDatabase) method and execute a SQL command to create
    the story table */
    @Override
    public void onCreate(SQLiteDatabase database) {

        // Creating Table for the Usernam and PWD
        database.execSQL("create table " + PollsterDBSchema.UserTable.NAME + "(" +
                PollsterDBSchema.UserTable.Columns.USER_NAME + ", " +
                PollsterDBSchema.UserTable.Columns.PWD + ")");

        // Creating Table for the Booleans of Category Preferences
        database.execSQL("create table " + PollsterDBSchema.CategoryPreferences.NAME + "(" +
                PollsterDBSchema.CategoryPreferences.Columns.USER + " VARCHAR(200) NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.ACADEMICS + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.BOOKS + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.ELECTRONICS + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.FOOD + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.MISC + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.MOVIES + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.NATURE + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.SHOPPING + " INTEGER NOT NULL, " +
                PollsterDBSchema.CategoryPreferences.Columns.SPORTS + " INTEGER NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {  }

}