package kz.talipovsn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.StringTokenizer;

public class MySQLite extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 7; // НОМЕР ВЕРСИИ БАЗЫ ДАННЫХ И ТАБЛИЦ !

    static final String DATABASE_NAME = "phones"; // Имя базы данных

    static final String TABLE_NAME = "emergency_service"; // Имя таблицы
    static final String ID = "id"; // Поле с ID
    static final String NAME = "name"; // Поле с наименованием организации
    static final String PRICE = "price"; // // Поле с наименованием организации в нижнем регистре
    static final String TYPE = "type"; // Поле с телефонным номером
    static final String POWER = "power";
    static final String WEIGHT = "weight";
    static final String COUNTRY = "country";

    static final String ASSETS_FILE_NAME = "vacuumcleaner.txt"; // Имя файла из ресурсов с данными для БД
    static final String DATA_SEPARATOR = "|"; // Разделитель данных в файле ресурсов с телефонами

    private Context context; // Контекст приложения

    public MySQLite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Метод создания базы данных и таблиц в ней
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY,"
                + NAME + " TEXT,"
                + PRICE + " INTEGER,"
                + TYPE + " TEXT,"
                + POWER + " INTEGER,"
                + WEIGHT + " INTEGER,"
                + COUNTRY + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        System.out.println(CREATE_CONTACTS_TABLE);
        loadDataFromAsset(context, ASSETS_FILE_NAME,  db);
    }

    // Метод при обновлении структуры базы данных и/или таблиц в ней
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        System.out.println("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Добавление нового контакта в БД
    public void addData(SQLiteDatabase db, String name, int price, String type, int power, int weight, String country) {
        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(PRICE, price);
        values.put(TYPE, type);
        values.put(POWER, power);
        values.put(WEIGHT, weight);
        values.put(COUNTRY, country);
        db.insert(TABLE_NAME, null, values);
    }

    // Добавление записей в базу данных из файла ресурсов
    public void loadDataFromAsset(Context context, String fileName, SQLiteDatabase db) {
        BufferedReader in = null;

        try {
            // Открываем поток для работы с файлом с исходными данными
            InputStream is = context.getAssets().open(fileName);
            // Открываем буфер обмена для потока работы с файлом с исходными данными
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = in.readLine()) != null) { // Читаем строку из файла
                String strTrim = str.trim(); // Убираем у строки пробелы с концов
                if (!strTrim.equals("")) { // Если строка не пустая, то
                    StringTokenizer st = new StringTokenizer(strTrim, DATA_SEPARATOR); // Нарезаем ее на части
                    String name = st.nextToken().trim(); // Извлекаем из строки название организации без пробелов на концах
                    int price = Integer.parseInt(st.nextToken().trim());
                    String type = st.nextToken().trim();
                    int power = Integer.parseInt(st.nextToken().trim());
                    int weight = Integer.parseInt(st.nextToken().trim());
                    String country = st.nextToken().trim();
                    addData(db, name, price, type, power, weight, country); // Добавляем название и телефон в базу данных
                }
            }

        // Обработчики ошибок
        } catch (IOException ignored) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    // Получение значений данных из БД в виде строки с фильтром
    public String getData(String filter, boolean isName, boolean isCountry, boolean isPrice, boolean isWeight, boolean isPower, boolean isType) {

        String selectQuery; // Переменная для SQL-запроса

        if (filter.equals("")) {
            selectQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + NAME;
        } else {
            if (isName) {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + NAME + " LIKE '%" + filter + "%'" +") ORDER BY " + NAME;
            }  else if (isCountry) {
                   selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + COUNTRY + " LIKE '%" + filter + "%'" +") ORDER BY " + COUNTRY;
            }  else if (isPrice) {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + PRICE + " >= " + filter +") ORDER BY " + PRICE;
            }  else if (isWeight) {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + WEIGHT +  " >= " + filter +") ORDER BY " + WEIGHT;
            }  else if (isPower) {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + POWER +  " >= " + filter +") ORDER BY " + POWER;
            }  else if (isType) {
                   selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + TYPE + " LIKE '%" + filter + "%'" + ") ORDER BY " + TYPE;
            }  else {
                    selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE (" + TYPE + " LIKE '%" + filter + "%'" + " OR " +  POWER + " LIKE '%" + filter + "%'"
                        + " OR " + WEIGHT + " LIKE '%" + filter + "%'" + " OR " + COUNTRY +
                        " LIKE '%" + filter + "%'" + " OR " + NAME + " LIKE '%" + filter + "%'" +") ORDER BY " + NAME;
        }
        }


        SQLiteDatabase db = this.getReadableDatabase(); // Доступ к БД


        try {
            Cursor cursor = db.rawQuery(selectQuery, null); // Выполнение SQL-запроса

            StringBuilder data = new StringBuilder(); // Переменная для формирования данных из запроса

            int num = 0;
            if (cursor.moveToFirst()) { // Если есть хоть одна запись, то
                do { // Цикл по всем записям результата запроса
                    int n = cursor.getColumnIndex(NAME);
                    int t = cursor.getColumnIndex(PRICE);
                    int k = cursor.getColumnIndex(TYPE);
                    int s = cursor.getColumnIndex(POWER);
                    int a = cursor.getColumnIndex(WEIGHT);
                    int d = cursor.getColumnIndex(COUNTRY);
                    String name = cursor.getString(n); // Чтение названия организации
                    int price = cursor.getInt(t); // Чтение телефонного номера
                    String type = cursor.getString(k);
                    int power = cursor.getInt(s);
                    int weight = cursor.getInt(a);
                    String country = cursor.getString(d);
                    data.append(String.valueOf(++num) + ") " + name + ": " + price + ": " + type + ": " + power + ": " + weight + ": " + country + ": " + "\n");
                } while (cursor.moveToNext()); // Цикл пока есть следующая запись
            }
            return data.toString();
            } catch (Exception e) {
            return "";
        }
    }

}