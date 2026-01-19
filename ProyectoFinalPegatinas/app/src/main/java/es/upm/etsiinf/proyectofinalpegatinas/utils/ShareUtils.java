package es.upm.etsiinf.proyectofinalpegatinas.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utilidades para compartir contenido y funciones de red.
 */
public class ShareUtils {

    // --------------------------------------------------------------------------
    // Funciones de Compartir (Sharing)
    // --------------------------------------------------------------------------

    public static void shareStickerScreenshot(Context context, View viewToCapture, String stickerName) {
        Bitmap bitmap = getBitmapFromView(viewToCapture);
        if (bitmap == null) {
            Toast.makeText(context, "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Guardar bitmap en cache dir
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs(); // don't forget to make the directory
            File file = new File(cachePath, "sticker_share.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Obtener URI
            Uri contentUri = FileProvider.getUriForFile(context,
                    "es.upm.etsiinf.proyectofinalpegatinas.fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Mira mi cromo de " + stickerName + "!");
                shareIntent.setType("image/png");
                context.startActivity(Intent.createChooser(shareIntent, "Compartir cromo"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al compartir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static Bitmap getBitmapFromView(View view) {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        return returnedBitmap;
    }

    // --------------------------------------------------------------------------
    // Funciones de Red (NetUtils integration)
    // --------------------------------------------------------------------------

    /**
     * Obtiene el contenido de una URL como String.
     *
     * @param urlString URL a descargar
     * @return Contenido como String
     * @throws Exception Si ocurre algún error
     */
    public static String getText(String urlString) throws Exception {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000); // 10 segundos
            connection.setReadTimeout(10000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("Error conexión: " + responseCode);
            }

            inputStream = connection.getInputStream();
            return readStream(inputStream);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
}