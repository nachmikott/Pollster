package edu.umd.cs.pollsternav.service.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import edu.umd.cs.pollsternav.CategoriesFragment;
import edu.umd.cs.pollsternav.service.UserSpecificsServiceInterface;

import java.util.ArrayList;
/**
 * Created by nachmi on 4/25/17.
 */
public class UserSpecificsService implements UserSpecificsServiceInterface {
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    public UserSpecificsService(Context context) {
        this.context = context;

        sqLiteDatabase = new PollsterDBHelper(context).getWritableDatabase();
    }
    protected SQLiteDatabase getDatabase() {
        return sqLiteDatabase;
    }

    private class StoryCursorWrapper extends CursorWrapper {
        public StoryCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        // This will return an arrayList of Category Enums from a cursor that was created from a sql call
        public ArrayList<CategoriesFragment.Categories> getCategories() {
            ArrayList<CategoriesFragment.Categories> categoryPreferences = new ArrayList<>();

            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.ACADEMICS)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.ACADEMICS);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.BOOKS)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.BOOKS);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.ELECTRONICS)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.ELECTRONICS);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.FOOD)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.FOOD);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.MISC)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.MISC);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.MOVIES)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.MOVIES);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.NATURE)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.NATURE);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.SHOPPING)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.SHOPPING);
            if(getInt(getColumnIndex(PollsterDBSchema.CategoryPreferences.Columns.SPORTS)) == 1)
                categoryPreferences.add(CategoriesFragment.Categories.SPORTS);

            return categoryPreferences;
        }

        // This will return arraylist<string> of (0) the UserName, (1) the Pwd from the cursor made from a sql call.
        public ArrayList<String> getUserNameAndPwd() {
            ArrayList<String> userAndPwd = new ArrayList<>();
            userAndPwd.add(getString(getColumnIndex(PollsterDBSchema.UserTable.Columns.USER_NAME)));
            userAndPwd.add(getString(getColumnIndex(PollsterDBSchema.UserTable.Columns.PWD)));

            return userAndPwd;
        }
    }

    // This query's the categories table
    private ArrayList<CategoriesFragment.Categories> queryCategories(String userName) {
        //String sqlStatement = "SELECT * FROM " + PollsterDBSchema.CategoryPreferences.NAME + " WHERE USER = ? ";


        //Cursor cursor = sqLiteDatabase.rawQuery(sqlStatement, new String[]{userName});
        Cursor cursor = sqLiteDatabase.query(PollsterDBSchema.CategoryPreferences.NAME, null, "USER=?", new String[]{userName}, null, null, null);

        ArrayList<CategoriesFragment.Categories> result = null;
        StoryCursorWrapper wrapper = new StoryCursorWrapper(cursor);
        try {
            wrapper.moveToFirst();
            result = wrapper.getCategories();
        } finally {
            wrapper.close();
            return result;
        }
    }

    // This query's the user table. Note: There should be at most ONE row or ZERO rows in this table at all times!
    private ArrayList<String> queryUsers() {
        String[] cols = {"USER_NAME", "PWD"};
        Cursor cursor = sqLiteDatabase.query("USER", cols, null, null, null, null, null);
        ArrayList<String> result = null;

        StoryCursorWrapper wrapper = new StoryCursorWrapper(cursor);
        try {
            wrapper.moveToFirst();
            result = wrapper.getUserNameAndPwd();
        } finally {
            wrapper.close();
            return result;
        }
    }

    // If we query the users, and theres a row in the table, that means were logged in, if there is
    // zero rows in the table, it means there is no user logged in on this phone
    public boolean loggedIn() {
        if(queryUsers() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static ContentValues getContentValues(ArrayList<CategoriesFragment.Categories> categoryPreferences) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.ACADEMICS, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.BOOKS, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.ELECTRONICS, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.FOOD, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.MISC, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.MOVIES, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.NATURE, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.SHOPPING, 0);
        contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.SPORTS, 0);


        for(CategoriesFragment.Categories category : categoryPreferences) {
            if(category.equals(CategoriesFragment.Categories.ACADEMICS))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.ACADEMICS, 1);
            if(category.equals(CategoriesFragment.Categories.BOOKS))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.BOOKS, 1);
            if(category.equals(CategoriesFragment.Categories.ELECTRONICS))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.ELECTRONICS, 1);
            if(category.equals(CategoriesFragment.Categories.FOOD))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.FOOD, 1);
            if(category.equals(CategoriesFragment.Categories.MISC))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.MISC, 1);
            if(category.equals(CategoriesFragment.Categories.MOVIES))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.MOVIES, 1);
            if(category.equals(CategoriesFragment.Categories.NATURE))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.NATURE, 1);
            if(category.equals(CategoriesFragment.Categories.SHOPPING))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.SHOPPING, 1);
            if(category.equals(CategoriesFragment.Categories.SPORTS))
                contentValues.put(PollsterDBSchema.CategoryPreferences.Columns.SPORTS, 1);
        }

        return contentValues;
    }

    // Returns an Enum arraylist of the category preferences this user has
    public ArrayList<CategoriesFragment.Categories> getCategoryPreferences(String userName) {
        return queryCategories(userName);
    }

    // Returns username of the currently logged in user.
    public String getUserName() {

        ArrayList<String> string = queryUsers();
        return string.get(0);
    }

    //This will add af User. Note: There will always be only ONE row or ZERO rows in the UserTable,
    // ONE rows = user is logged in, ZERO rows = no user is logged in
    public void addUser(String user_name, String pwd) {
        ContentValues contentValuesUser = new ContentValues();
        contentValuesUser.put(PollsterDBSchema.UserTable.Columns.USER_NAME, user_name);
        contentValuesUser.put(PollsterDBSchema.UserTable.Columns.PWD, pwd);

        ContentValues contentValuesDefaultCategories = new ContentValues();

        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.USER, user_name);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.ACADEMICS, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.BOOKS, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.ELECTRONICS, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.FOOD, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.MISC, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.MOVIES, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.NATURE, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.SHOPPING, 1);
        contentValuesDefaultCategories.put(PollsterDBSchema.CategoryPreferences.Columns.SPORTS, 1);

        sqLiteDatabase.insert(PollsterDBSchema.UserTable.NAME, null, contentValuesUser);
        sqLiteDatabase.insert(PollsterDBSchema.CategoryPreferences.NAME, null, contentValuesDefaultCategories);
    }

    //This updates the category preference for the user.
    public void updateCategoryPreferences(ArrayList<CategoriesFragment.Categories> categoryPreferences, String userName) {
        sqLiteDatabase.update(PollsterDBSchema.CategoryPreferences.NAME, getContentValues(categoryPreferences), "USER=?", new String[]{userName});
    }

    public void signOut() {
        String userName = getUserName();
        sqLiteDatabase.delete(PollsterDBSchema.UserTable.NAME, PollsterDBSchema.UserTable.Columns.USER_NAME + "=?", new String[]{userName});

    }
}
