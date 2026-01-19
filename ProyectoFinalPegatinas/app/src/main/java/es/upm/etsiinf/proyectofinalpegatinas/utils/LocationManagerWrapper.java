package es.upm.etsiinf.proyectofinalpegatinas.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationManagerWrapper {

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;

    public interface LocationCallback {
        void onCountryFound(String countryCode, String countryName);

        void onError(String error);
    }

    public LocationManagerWrapper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void detectCountry(final LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);
                                    String countryCode = address.getCountryCode(); // e.g., "ES"
                                    String countryName = address.getCountryName(); // e.g., "España"
                                    callback.onCountryFound(countryCode, countryName);
                                } else {
                                    callback.onError("Address not found");
                                }
                            } catch (IOException e) {
                                callback.onError("Geocoder error: " + e.getMessage());
                            }
                        } else {
                            callback.onError("Location is null");
                        }
                    }
                });
    }
}
