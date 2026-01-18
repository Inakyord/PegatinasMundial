package es.upm.etsiinf.proyectofinalpegatinas.data.loadThreads;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumDbHelper;
import es.upm.etsiinf.proyectofinalpegatinas.data.remote.EquipoData;
import es.upm.etsiinf.proyectofinalpegatinas.data.remote.JugadorData;
import es.upm.etsiinf.proyectofinalpegatinas.data.remote.TorneoData;
import android.content.Intent;
import es.upm.etsiinf.proyectofinalpegatinas.utils.ShareUtils;

/**
 * Hilo que descarga el JSON de cromos y lo inserta en SQLite.
 */
public class DescargarStickersThread implements Runnable {

    private static final String JSON_URL = "https://raw.githubusercontent.com/Inakyord/PegatinasMundial/main/jugadores.json";
    private static final String TAG = "DescargarStickers";

    private Context context;

    public DescargarStickersThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.d(TAG, "Iniciando descarga de stickers...");
        SQLiteDatabase db = null;
        try {
            // Inicializa notificaciones
            es.upm.etsiinf.proyectofinalpegatinas.utils.NotificationHelper.createNotificationChannel(context);

            AlbumDbHelper dbHelperCheck = new AlbumDbHelper(context);
            SQLiteDatabase dbCheck = dbHelperCheck.getReadableDatabase();
            long countRows = android.database.DatabaseUtils.queryNumEntries(dbCheck, AlbumDbHelper.TABLE_STICKER);

            if (countRows > 0) {
                Log.d(TAG, "La base de datos ya tiene " + countRows + " stickers. Saltando descarga.");
                sendBroadcast();
                return;
            }

            // 1. Descargar JSON
            String jsonResponse = ShareUtils.getText(JSON_URL);
            Log.d(TAG, "JSON descargado, tamaño: " + jsonResponse.length());

            // 2. Parsear JSON con Gson
            Gson gson = new Gson();
            TorneoData torneo = gson.fromJson(jsonResponse, TorneoData.class);

            if (torneo == null || torneo.equipos == null) {
                Log.e(TAG, "Error: JSON vacío o estructura incorrecta");
                return;
            }

            // 3. Insertar en SQLite
            AlbumDbHelper dbHelper = new AlbumDbHelper(context);
            db = dbHelper.getWritableDatabase();


            db.beginTransaction();
            int totalInserted = 0;
            try {
                int teamIndex = 0;

                for (EquipoData equipo : torneo.equipos) {
                    // Calculo de Grupo
                    int groupIdx = teamIndex / 4;
                    char groupChar = (char) ('A' + groupIdx);
                    String groupName = "Grupo " + groupChar;

                    // Insertar Equipo en TABLE_TEAMS
                    ContentValues teamValues = new ContentValues();
                    teamValues.put(AlbumDbHelper.COLUMN_TEAM_CODE, equipo.codigo);
                    teamValues.put(AlbumDbHelper.COLUMN_TEAM_NAME, equipo.pais);
                    teamValues.put(AlbumDbHelper.COLUMN_TEAM_GROUP, groupName);
                    teamValues.put(AlbumDbHelper.COLUMN_TEAM_ISO, equipo.isoPais);
                    // ID autogenerado mantiene el orden y sirve para numero de cromo
                    db.insert(AlbumDbHelper.TABLE_TEAMS, null, teamValues);

                    teamIndex++;

                    for (JugadorData jugador : equipo.jugadores) {
                        ContentValues values = new ContentValues();
                        values.put(AlbumDbHelper.COLUMN_CODIGO_EQUIPO, equipo.codigo);
                        values.put(AlbumDbHelper.COLUMN_PAIS, equipo.pais);
                        values.put(AlbumDbHelper.COLUMN_ISO_PAIS, equipo.isoPais);
                        values.put(AlbumDbHelper.COLUMN_NOMBRE, jugador.nombre);
                        values.put(AlbumDbHelper.COLUMN_POSICION, jugador.posicion);

                        String descripcionPos = null;
                        if (torneo.referenciaPosiciones != null) {
                            descripcionPos = torneo.referenciaPosiciones.get(jugador.posicion);
                        }
                        values.put(AlbumDbHelper.COLUMN_POSICION_DESC, descripcionPos);

                        values.put(AlbumDbHelper.COLUMN_EDAD, jugador.edad);
                        values.put(AlbumDbHelper.COLUMN_CLUB, jugador.club);
                        values.put(AlbumDbHelper.COLUMN_ESTATURA, jugador.estatura);

                        db.insert(AlbumDbHelper.TABLE_STICKER, null, values);
                        totalInserted++;
                    }
                }
                db.setTransactionSuccessful();
                Log.d(TAG, "Inserción completada. Total stickers: " + totalInserted);

                // Mostrar notificación de éxito
                es.upm.etsiinf.proyectofinalpegatinas.utils.NotificationHelper.showSyncNotification(
                        context,
                        "Sincronización Completada",
                        "Se han descargado " + totalInserted + " stickers.");

            } finally {
                db.endTransaction();
            }

            sendBroadcast();

        } catch (Exception e) {
            Log.e(TAG,
                    "FATAL ERROR in DescargarStickersThread: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                Log.e(TAG, "    at " + element.toString());
            }
            e.printStackTrace();
            if (db != null && db.isOpen()) {
                db.close();
            }
            sendBroadcast();
        }
    }

    private void sendBroadcast() {
        Intent intent = new Intent("es.upm.etsiinf.proyectofinalpegatinas.DATA_UPDATED");
        context.sendBroadcast(intent);
    }
}
