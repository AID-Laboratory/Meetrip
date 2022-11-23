package com.hsu.aidlab.meetrip.Util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper
{
    private static String DB_PATH = Constants.DB_PATH;
    private SQLiteDatabase sqLiteDatabase;
    private final Context context;

    /**
     * * Constructor of DBHelper object
     * @param context
     */
    public DBHelper(Context context)
    {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        /**
         * Getting database path from system directory
         * */
        DB_PATH = (Build.VERSION.SDK_INT > 15) ? context.getApplicationInfo().dataDir + "/databases/"
                                               : Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
        this.context = context;
        checkDB();
    }

    /**
     * This method checks if database file exists in the database folder
     * if database doesn't exists in the system directory we need to copy database file into database folder
     * */
    private void checkDB()
    {
        File database = context.getDatabasePath(Constants.DB_NAME);

        if(false == database.exists())
        {
            sqLiteDatabase = this.getReadableDatabase();
            copyDatabase();
        }
        else
        {
            sqLiteDatabase = this.getReadableDatabase();
        }
    }

    /**
     * Opening SQLite database. Another hand, making connection with database and getting permissions for READ and WRITE
     * We need to check condition that if database is opened or not
     * if we try to open database that is already opened, exception throws
     */
    public void openDb()
    {
        if(!sqLiteDatabase.isOpen())
        {
            sqLiteDatabase = SQLiteDatabase.openDatabase(DB_PATH+ Constants.DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        }
    }

    /**
     * This function copies database file from Asset folder into database folder in system directory
     * @return
     */
    public boolean copyDatabase()
    {
        try
        {
            /**
             * Open Asset folder and creating InputStream from database file
             * */
            InputStream inputStream = context.getAssets().open(Constants.DB_NAME);

            /**
             * This variable takes directory path + file name + file extension
             * */
            String outfilename = DB_PATH + Constants.DB_NAME;

            /**
             * Creating Outputstream by reading Inputstream
             * FileOutputStream object takes Absolute path where we going to write file on its constructor
             * */
            OutputStream outputStream = new FileOutputStream(outfilename);
            byte [] buff = new byte[1024];
            int lenght = 0;

            while ((lenght = inputStream.read(buff))>0)
            {

                outputStream.write(buff,0,lenght);

            }
            /*
            * Flush Outputstream stream. means that writing new file on the disk
            * */
            outputStream.flush();
            outputStream.close();

            /*
            * If operation complete without exception then return true
            * */
            return true;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * This method executing SQL query for getting data from database and returns Cursor data
     *
     * @param query
     * @return
     */
    public Cursor getData(String query)
    {
        /**
         * Need to check if database is opened because SQL query cannot be executed when database is closed
         * */
        return (sqLiteDatabase.isOpen()) ? sqLiteDatabase.rawQuery(query, null) : null;
    }

    /**
     * This method putting new data into database by executing SQL query which is passed by parameter
     *
     * @param query
     */
    public void putData(String query)
    {
        this.openDb();
        sqLiteDatabase.execSQL(query);
        this.close();
    }

    /**
     * This method will be called after we finished getting data from database and closing database
     */
    public synchronized void close()
    {
        if(sqLiteDatabase != null) sqLiteDatabase.close();
        super.close();
    }

    /**
     * This method fetch data from database
     * Takes following values on parameter : name of table, name of columns want to select
     * If selecting data from table that has flag or type, need to pass column name of flag and its value
     * Return List contains HashMap
     *
     * @param tableName
     * @param columnsToSelect
     * @param orderColumn
     * @param flagColumn
     * @param flag
     * @return
     */
    public List<HashMap<String, String>> getMapListFromEntity(String tableName, String[] columnsToSelect, String orderColumn, String flagColumn, int flag)
    {

        List<HashMap<String, String>> returnData = new ArrayList<>();
        /*
        * open database if closed
        * */
        openDb();
        Integer limit = 0;
        /**
         *  Getting total row count from table for limiting return data size
         * */
        Integer totalCount = getCount(tableName, null, null);
        Log.d("totalcount", totalCount+"");

        /**
         * Column names are comes in String array
         * So we need to convert it to String variable supplying by comma
         * */
        String columns = Arrays.toString(columnsToSelect);
        columns = columns.substring(1, columns.length() - 1);

        /**
         * SQLite Cursor cannot allocate memory on disk if data size is greater than 2048kb, Standard heap size is limited by 2mb
         * if database returned data bigger than 2mb, system can't create cache on the memory and than heap size exception will be thrown
         * So we need to select all the data bit by bit
         *
         * Main logic is trying to avoid from heap size exception
         *
         * if total row count we going to select is greater than 100, we setting the limit for return data size by 100
         *
         * For example: Just pretend that database returning absolute row count,
         * We going to select 1000 rows from database
         * so we divide total row count by 100 and loop for result count
         * while loop is running, we select only 100 rows from database and add each row data into the List
         *
         * After fetching all data from database into List than return
         */
        while (limit < totalCount)
        {
            String query = null;

            if (flag >= 0 && flagColumn != null)
            {
                query = "select "+ columns +" from "+ tableName +" where "+ flagColumn +" = '" + flag + "' ORDER BY "+ orderColumn +" ASC LIMIT "+ limit +", 100";
            }
            else
            {
                query = "select "+ columns +" from "+ tableName + " ORDER BY "+ orderColumn +" ASC LIMIT "+ limit +", 100";
            }

            if(query != null)
            {
                /**
                 * selecting data from database and store into cursor
                 */
                Cursor cursor = getData(query);
                if(cursor != null)
                {
                    /**
                     * if query execution returned not null cursor we need to loop for all over the cursor for getting data from it
                     * Cursor will iterate by itself and we get data from cursor while iterating
                     * means that, the loop will continue if cursor has next row to move
                     */
                    while (cursor.moveToNext())
                    {
                        HashMap<String, String> mappedData = new HashMap<>();
                        String sensorCode = cursor.getString(cursor.getColumnIndex(columnsToSelect[0]));
                        String sensorValue = cursor.getString(cursor.getColumnIndex(columnsToSelect[1]));
                        mappedData.put(sensorCode, sensorValue);
                        returnData.add(mappedData);
                    }
                    cursor.close();
                    cursor = null;
                }
                limit += 100;
            }
        }
        close();
        return returnData;
    }

    /**
     * This method operating same logic as above method getMapListFromEntity()
     * Only return type is different
     * @param oldList
     * @param tableName
     * @param columnToSelect
     * @param columnName
     * @param columnValue
     * @param orderColumn
     * @return List<String>
     */
    public List<String> getStringList(List<String> oldList, String tableName, String columnToSelect, String columnName, String columnValue, String orderColumn)
    {
        openDb();
        Integer limit = 0;
        Integer totalCount = getCount(tableName, columnName, columnValue);

        if(oldList.size() == totalCount)
        {
            close();
            return oldList;
        }
        else
        {
            oldList.clear();
            while (limit < totalCount)
            {
                String query = "select "+ columnToSelect +" from "+ tableName +" where "+ columnName +" = '" + columnValue + "' ORDER BY "+ orderColumn +" ASC LIMIT "+ limit +", 100";
                Cursor cursor = getData(query);
                if(cursor != null)
                {
                    while (cursor.moveToNext())
                    {
                        String sensorValue = cursor.getString(cursor.getColumnIndex(columnToSelect));
                        oldList.add(CommonUtils.checkNull(sensorValue));
                    }
                    cursor.close();
                    cursor = null;
                }
                limit += 100;
            }
            close();
            return oldList;
        }
    }

    /**
     * This method operating almost same logic as above method getStringList()
     * The difference is selecting data from database using SQL Like operator on any field
     * @param oldList
     * @param tableName
     * @param columnToSelect
     * @param columnName
     * @param columnValue
     * @param orderColumn
     * @return List<String>
     */
    public List<String> getStringListLike(List<String> oldList, String tableName, String columnToSelect, String columnName, String columnValue, String orderColumn)
    {
        openDb();
        Integer limit = 0;
        Integer totalCount = getCountLike(tableName, columnName, columnValue);

        if(oldList.size() == totalCount)
        {
            close();
            return oldList;
        }
        else
        {
            oldList.clear();
            while (limit < totalCount)
            {
                String query = "select "+ columnToSelect +" from "+ tableName +" where "+ columnName +" like '" + columnValue + "%' ORDER BY "+ orderColumn +" ASC LIMIT "+ limit +", 100";
                Cursor cursor = getData(query);
                if(cursor != null)
                {
                    while (cursor.moveToNext())
                    {
                        String sensorValue = cursor.getString(cursor.getColumnIndex(columnToSelect));
                        oldList.add(CommonUtils.checkNull(sensorValue));
                    }
                    cursor.close();
                    cursor = null;
                }
                limit += 100;
            }
            close();
            return oldList;
        }
    }

    /**
     *  This method returns total row count of table in database
     * @param table_name
     * @param columnName
     * @param columnValue
     * @return
     */
    public Integer getCount(String table_name, String columnName, String columnValue)
    {
        Integer count = 0;
        String query = null;

        /**
         * If name of the table and the column name and it's value passed by parameter, we select row count by using SQL where clause
         */
        if(table_name != null && columnName != null && columnValue != null)
        {
            query = "select count(*) as count from "+table_name+" where "+ columnName +" = '"+ columnValue +"' ";
        }
        else
        {
            query = "select count(*) as count from "+table_name+"";
        }
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                count += cursor.getInt(cursor.getColumnIndex("count"));
            }
            cursor.close();
            cursor = null;
        }
        return (count != null) ? count : 0;
    }

    public Integer getCountLike(String table_name, String columnName, String columnValue)
    {
        Integer count = 0;
        String query = null;

        query = "select count(*) as count from "+table_name+" where "+ columnName +" like '"+ columnValue +"%' ";

        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if(cursor != null)
        {
            while (cursor.moveToNext())
            {
                count += cursor.getInt(cursor.getColumnIndex("count"));
            }
            cursor.close();
            cursor = null;
        }
        return (count != null) ? count : 0;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {}
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
