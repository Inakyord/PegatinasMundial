package es.upm.etsiinf.proyectofinalpegatinas.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper para la base de datos SQLite.
 */
public class AlbumDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AlbumMundial.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla Sticker
    public static final String TABLE_STICKER = "sticker";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CODIGO_EQUIPO = "codigo_equipo";
    public static final String COLUMN_PAIS = "pais";
    public static final String COLUMN_ISO_PAIS = "iso_pais";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_POSICION = "posicion";
    public static final String COLUMN_POSICION_DESC = "posicion_descripcion";
    public static final String COLUMN_EDAD = "edad";
    public static final String COLUMN_CLUB = "club";
    public static final String COLUMN_ESTATURA = "estatura";
    public static final String COLUMN_CUSTOM_IMAGE_URI = "custom_image_uri";

    // Tabla Users
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Tabla UserSticker
    public static final String TABLE_USER_STICKER = "user_sticker";
    public static final String COLUMN_US_ID = "id";
    public static final String COLUMN_US_USER_ID = "user_id"; // FK to users
    public static final String COLUMN_US_STICKER_ID = "sticker_id";
    public static final String COLUMN_US_CANTIDAD = "cantidad";
    public static final String COLUMN_US_CUSTOM_IMAGE_URI = "custom_image_uri";

    // Tabla Teams
    public static final String TABLE_TEAMS = "teams";
    public static final String COLUMN_TEAM_ID = "id";
    public static final String COLUMN_TEAM_CODE = "code";
    public static final String COLUMN_TEAM_NAME = "name";
    public static final String COLUMN_TEAM_GROUP = "group_name";
    public static final String COLUMN_TEAM_ISO = "iso_code";

    // --- SQL ---

    private static final String SQL_CREATE_TABLE_STICKER = "CREATE TABLE " + TABLE_STICKER + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CODIGO_EQUIPO + " TEXT," +
            COLUMN_PAIS + " TEXT," +
            COLUMN_ISO_PAIS + " TEXT," +
            COLUMN_NOMBRE + " TEXT," +
            COLUMN_POSICION + " TEXT," +
            COLUMN_POSICION_DESC + " TEXT," +
            COLUMN_EDAD + " INTEGER," +
            COLUMN_CLUB + " TEXT," +
            COLUMN_ESTATURA + " INTEGER," +
            COLUMN_CUSTOM_IMAGE_URI + " TEXT)";

    private static final String SQL_CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USER_NAME + " TEXT UNIQUE," +
            COLUMN_USER_PASSWORD + " TEXT)";

    // Includes the URI column by default now
    private static final String SQL_CREATE_TABLE_USER_STICKER = "CREATE TABLE " + TABLE_USER_STICKER + " (" +
            COLUMN_US_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_US_USER_ID + " INTEGER," +
            COLUMN_US_STICKER_ID + " INTEGER," +
            COLUMN_US_CANTIDAD + " INTEGER," +
            COLUMN_US_CUSTOM_IMAGE_URI + " TEXT," +
            "FOREIGN KEY(" + COLUMN_US_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")," +
            "FOREIGN KEY(" + COLUMN_US_STICKER_ID + ") REFERENCES " + TABLE_STICKER + "(" + COLUMN_ID + "))";

    private static final String SQL_CREATE_TABLE_TEAMS = "CREATE TABLE " + TABLE_TEAMS + " (" +
            COLUMN_TEAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_TEAM_CODE + " TEXT," +
            COLUMN_TEAM_NAME + " TEXT," +
            COLUMN_TEAM_GROUP + " TEXT," +
            COLUMN_TEAM_ISO + " TEXT)";

    public AlbumDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_STICKER);
        db.execSQL(SQL_CREATE_TABLE_USERS);
        db.execSQL(SQL_CREATE_TABLE_USER_STICKER);
        db.execSQL(SQL_CREATE_TABLE_TEAMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        reset(db);
    }

    /**
     * Drops all known tables and recreates them.
     */
    public void reset(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STICKER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STICKER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEAMS);
        onCreate(db);
    }
}