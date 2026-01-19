package es.upm.etsiinf.proyectofinalpegatinas.data.local;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Fuente de datos local (SQLite) para acceder a los cromos.
 */
public class AlbumLocalDataSource {

    private AlbumDbHelper dbHelper;

    public AlbumLocalDataSource(Context context) {
        dbHelper = new AlbumDbHelper(context);
    }

    /**
     * Obtiene todos los usuarios registrados.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AlbumDbHelper.TABLE_USERS, null, null, null, null, null,
                AlbumDbHelper.COLUMN_USER_NAME + " ASC");

        while (cursor.moveToNext()) {
            User user = new User();
            user.setId(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_USER_ID))));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_USER_NAME)));
            users.add(user);
        }
        cursor.close();
        return users;
    }

    /**
     * Registra un nuevo usuario con contraseña.
     *
     */
    public User registerUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(AlbumDbHelper.COLUMN_USER_NAME, username);
        values.put(AlbumDbHelper.COLUMN_USER_PASSWORD, password);

        try {
            long id = db.insertOrThrow(AlbumDbHelper.TABLE_USERS, null, values);
            User user = new User();
            user.setId(String.valueOf(id));
            user.setName(username);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Intenta hacer login verificando contraseña.
     *
     */
    public User loginUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AlbumDbHelper.TABLE_USERS,
                null,
                AlbumDbHelper.COLUMN_USER_NAME + "=? AND " + AlbumDbHelper.COLUMN_USER_PASSWORD + "=?",
                new String[] { username, password }, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(String.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_USER_ID))));
            user.setName(username);
        }
        cursor.close();
        return user;
    }

    /**
     * Obtiene la lista de equipos ordenados por inserción (grupos).
     */
    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(AlbumDbHelper.TABLE_TEAMS,
                null, null, null, null, null, AlbumDbHelper.COLUMN_TEAM_ID + " ASC");

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_NAME));
            String code = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_CODE));
            String group = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_GROUP));

            String iso = null;
            int isoIdx = cursor.getColumnIndex(AlbumDbHelper.COLUMN_TEAM_ISO);
            if (isoIdx != -1) {
                iso = cursor.getString(isoIdx);
            }

            teams.add(new Team(name, code, group, iso));
        }
        cursor.close();
        return teams;
    }

    public Team getTeamByIso(String isoCode) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AlbumDbHelper.TABLE_TEAMS,
                null,
                AlbumDbHelper.COLUMN_TEAM_ISO + "=? COLLATE NOCASE",
                new String[] { isoCode }, null, null, null);
        Team team = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_NAME));
            String code = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_CODE));
            String group = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_GROUP));
            String iso = cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_TEAM_ISO));
            team = new Team(name, code, group, iso);
        }
        cursor.close();
        return team;
    }

    /**
     * Obtiene todos los stickers con su estado para un usuario especifico.
     */
    public List<Sticker> getAllStickersWithStatus(int userId) {
        return getAllStickersWithStatus(userId, false);
    }

    public List<Sticker> getAllStickersWithStatus(int userId, boolean orderByName) {
        List<Sticker> stickers = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String orderBy = null;
        if (orderByName) {
            orderBy = "s." + AlbumDbHelper.COLUMN_NOMBRE + " ASC";
        }

        String rawQuery = "SELECT s.*, us." + AlbumDbHelper.COLUMN_US_CANTIDAD + ", us."
                + AlbumDbHelper.COLUMN_US_CUSTOM_IMAGE_URI +
                " FROM " + AlbumDbHelper.TABLE_STICKER + " s " +
                " LEFT JOIN " + AlbumDbHelper.TABLE_USER_STICKER + " us " +
                " ON s." + AlbumDbHelper.COLUMN_ID + " = us." + AlbumDbHelper.COLUMN_US_STICKER_ID +
                " AND us." + AlbumDbHelper.COLUMN_US_USER_ID + " = ?";

        if (orderBy != null) {
            rawQuery += " ORDER BY " + orderBy;
        }

        Cursor cursor = db.rawQuery(rawQuery, new String[] { String.valueOf(userId) });

        return parseStickersCursor(cursor);
    }

    public void updateStickerImage(int userId, int stickerId, String imageUri) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AlbumDbHelper.COLUMN_US_USER_ID + "=? AND " + AlbumDbHelper.COLUMN_US_STICKER_ID + "=?";
        String[] args = { String.valueOf(userId), String.valueOf(stickerId) };

        Cursor c = db.query(AlbumDbHelper.TABLE_USER_STICKER, null, selection, args, null, null, null);
        boolean exists = c.moveToFirst();
        c.close();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(AlbumDbHelper.COLUMN_US_USER_ID, userId);
        values.put(AlbumDbHelper.COLUMN_US_STICKER_ID, stickerId);
        values.put(AlbumDbHelper.COLUMN_US_CUSTOM_IMAGE_URI, imageUri);

        if (exists) {
            db.update(AlbumDbHelper.TABLE_USER_STICKER, values, selection, args);
        } else {
            // cantidad 0
            values.put(AlbumDbHelper.COLUMN_US_CANTIDAD, 0);
            db.insert(AlbumDbHelper.TABLE_USER_STICKER, null, values);
        }
    }

    /**
     * Actualiza la cantidad de un cromo para un usuario.
     */
    public void setUserStickerQuantity(int userId, int stickerId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = AlbumDbHelper.COLUMN_US_USER_ID + "=? AND " + AlbumDbHelper.COLUMN_US_STICKER_ID + "=?";
        String[] args = { String.valueOf(userId), String.valueOf(stickerId) };

        Cursor c = db.query(AlbumDbHelper.TABLE_USER_STICKER, null, selection, args, null, null, null);
        boolean exists = c.moveToFirst();
        c.close();

        android.content.ContentValues values = new android.content.ContentValues();
        values.put(AlbumDbHelper.COLUMN_US_USER_ID, userId);
        values.put(AlbumDbHelper.COLUMN_US_STICKER_ID, stickerId);
        values.put(AlbumDbHelper.COLUMN_US_CANTIDAD, quantity);

        if (exists) {
            db.update(AlbumDbHelper.TABLE_USER_STICKER, values, selection, args);
        } else {
            db.insert(AlbumDbHelper.TABLE_USER_STICKER, null, values);
        }
    }

    public Sticker getStickerById(int stickerId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(AlbumDbHelper.TABLE_STICKER, null,
                AlbumDbHelper.COLUMN_ID + "=?",
                new String[] { String.valueOf(stickerId) }, null, null, null);

        Sticker sticker = null;
        List<Sticker> result = parseStickersCursor(cursor);
        if (!result.isEmpty()) {
            sticker = result.get(0);
        }
        return sticker;
    }

    private List<Sticker> parseStickersCursor(Cursor cursor) {
        List<Sticker> stickers = new ArrayList<>();
        while (cursor.moveToNext()) {
            Sticker sticker = new Sticker();
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_ID));
            sticker.setId(String.valueOf(id));
            sticker.setName(cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_NOMBRE)));
            sticker.setPais(cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_PAIS)));
            sticker.setPosicion(cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_POSICION)));
            sticker.setPosicionDescripcion(
                    cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_POSICION_DESC)));
            sticker.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_EDAD)));
            sticker.setClub(cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_CLUB)));
            sticker.setEstatura(cursor.getInt(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_ESTATURA)));
            sticker.setCodigoEquipo(cursor.getString(cursor.getColumnIndexOrThrow(AlbumDbHelper.COLUMN_CODIGO_EQUIPO)));
            sticker.setTeam(sticker.getPais());

            // Busca custom
            int customUriIdx = cursor.getColumnIndex(AlbumDbHelper.COLUMN_CUSTOM_IMAGE_URI);
            if (customUriIdx != -1 && !cursor.isNull(customUriIdx)) {
                sticker.setCustomImageUri(cursor.getString(customUriIdx));
            }

            // Busca cantidad
            int quantityIdx = cursor.getColumnIndex(AlbumDbHelper.COLUMN_US_CANTIDAD);
            if (quantityIdx != -1 && !cursor.isNull(quantityIdx)) {
                sticker.setQuantity(cursor.getInt(quantityIdx));
            }

            stickers.add(sticker);
        }
        cursor.close();
        return stickers;
    }

    /**
     * Verifica si el álbum completo está completo.
     */
    public boolean isAlbumComplete(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long total = android.database.DatabaseUtils.queryNumEntries(db, AlbumDbHelper.TABLE_STICKER);

        String query = "SELECT COUNT(*) FROM " + AlbumDbHelper.TABLE_USER_STICKER +
                " WHERE " + AlbumDbHelper.COLUMN_US_USER_ID + "=? AND " + AlbumDbHelper.COLUMN_US_CANTIDAD + ">0";

        Cursor c = db.rawQuery(query, new String[] { String.valueOf(userId) });
        long owned = 0;
        if (c.moveToFirst()) {
            owned = c.getLong(0);
        }
        c.close();

        return total > 0 && total == owned;
    }

    public void resetDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.reset(db);
    }
}
