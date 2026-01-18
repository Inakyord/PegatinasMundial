package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Team;
import es.upm.etsiinf.proyectofinalpegatinas.utils.LocationManagerWrapper;

public class MainMenuFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    public MainMenuFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnFullList = view.findViewById(R.id.btn_full_list);
        Button btnFavorite = view.findViewById(R.id.btn_favorite_country);
        Button btnInput = view.findViewById(R.id.btn_input_stickers);
        Button btnNivel = view.findViewById(R.id.btn_nivel);

        btnFullList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir al album
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showTeamStickers(null);
                }
            }
        });

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                if (prefs.contains("favorite_country_name")) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("País Favorito")
                            .setMessage("¿Qué quieres hacer?")
                            .setPositiveButton("Ver Cromos", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkFavoriteCountry(true);
                                }
                            })
                            .setNegativeButton("Cambiar Equipo", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    prefs.edit().remove("favorite_country_name").apply();
                                    showManualCountrySelection();
                                }
                            })
                            .show();
                } else {
                    checkFavoriteCountry(true);
                }
            }
        });

        btnFavorite.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
                prefs.edit().remove("favorite_country_name").apply();
                Toast.makeText(getContext(), "Selecciona nuevo equipo favorito", Toast.LENGTH_SHORT).show();
                showManualCountrySelection();
                return true;
            }
        });

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new InputStickersFragment());
                transaction.addToBackStack(null);
                transaction.commit();

                if (getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null) {
                    ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });

        btnNivel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new NivelFragment());
                transaction.addToBackStack(null);
                transaction.commit();

                if (getActivity() != null && ((MainActivity) getActivity()).getSupportActionBar() != null) {
                    ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        });
        checkFavoriteCountry(false);
    }

    private void checkFavoriteCountry(boolean navigate) {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String savedCountry = prefs.getString("favorite_country_name", null);

        Button btnFavorite = null;
        if (getView() != null) {
            btnFavorite = getView().findViewById(R.id.btn_favorite_country);
        }

        if (savedCountry != null) {
            if (btnFavorite != null) {
                btnFavorite.setText("País Favorito: " + savedCountry + " ⭐");
            }
            if (navigate) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showTeamStickers(savedCountry);
                }
            }
        } else {
            if (btnFavorite != null) {
                btnFavorite.setText("Seleccionar País Favorito");
            }
            if (navigate) {
                startLocationDetection();
            }
        }
    }

    private void startLocationDetection() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_CODE);
            return;
        }

        Toast.makeText(getContext(), R.string.detecting_location, Toast.LENGTH_SHORT).show();
        LocationManagerWrapper locationManager = new LocationManagerWrapper(requireContext());
        locationManager.detectCountry(new LocationManagerWrapper.LocationCallback() {
            @Override
            public void onCountryFound(String countryCode, String countryName) {
                handleCountryFound(countryCode, countryName);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "GPS Error: " + error, Toast.LENGTH_SHORT).show();
                // Fallback to manual selection
                showManualCountrySelection();
            }
        });
    }

    private void handleCountryFound(String countryCode, String countryName) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                AlbumLocalDataSource dataSource = new AlbumLocalDataSource(requireContext());

                // First try by ISO Code
                Team matchedTeam = dataSource.getTeamByIso(countryCode);

                if (matchedTeam == null) {
                    // Fallback to name matching
                    List<Team> teams = dataSource.getTeams();
                    for (Team t : teams) {
                        if (t.getName().equalsIgnoreCase(countryName) || t.getCode().equalsIgnoreCase(countryCode)) {
                            matchedTeam = t;
                            break;
                        }
                    }
                }

                final Team finalMatch = matchedTeam;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finalMatch != null) {
                                saveFavoriteCountry(finalMatch.getName());
                                Toast.makeText(getContext(),
                                        getString(R.string.favorite_country_found, finalMatch.getName()),
                                        Toast.LENGTH_LONG).show();
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).showTeamStickers(finalMatch.getName());
                                }
                            } else {
                                Toast.makeText(getContext(), R.string.favorite_country_not_found, Toast.LENGTH_LONG)
                                        .show();
                                showManualCountrySelection();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void showManualCountrySelection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AlbumLocalDataSource dataSource = new AlbumLocalDataSource(requireContext());
                final List<Team> teams = dataSource.getTeams();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showCountryDialog(teams);
                        }
                    });
                }
            }
        }).start();
    }

    private void showCountryDialog(final List<Team> teams) {
        String[] teamNames = new String[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamNames[i] = teams.get(i).getName();
        }

        new AlertDialog.Builder(getContext())
                .setTitle(R.string.select_favorite_country)
                .setItems(teamNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = teams.get(which).getName();
                        saveFavoriteCountry(selected);
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).showTeamStickers(selected);
                        }
                    }
                })
                .show();
    }

    private void saveFavoriteCountry(String countryName) {
        SharedPreferences prefs = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        prefs.edit().putString("favorite_country_name", countryName).apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationDetection();
            } else {
                Toast.makeText(getContext(), "Permiso denegado. Selecciona manualmente.", Toast.LENGTH_SHORT).show();
                showManualCountrySelection();
            }
        }
    }
}
