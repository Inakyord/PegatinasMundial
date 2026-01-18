package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Sticker;

/**
 * Fragmento que muestra la lista de cromos (el álbum).
 */
public class AlbumFragment extends Fragment {

    private static final String ARG_TEAM = "arg_team";
    private String teamFilter;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    private StickerRecyclerAdapter adapter;

    public AlbumFragment() { }

    public static AlbumFragment newInstance(String teamName) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEAM, teamName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teamFilter = getArguments().getString(ARG_TEAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_stickers);

        loadStickers();

        setupQuickSwitchButton(view);
    }

    private void setupQuickSwitchButton(View view) {
        android.widget.Button btnSwitch = view.findViewById(R.id.btn_quick_switch);
        if (btnSwitch != null) {
            btnSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Navegación a vista de equipos
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack(null,
                                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        androidx.fragment.app.FragmentTransaction transaction = getActivity()
                                .getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, new TeamsFragment());
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            });
        }
    }

    // Shake Detection
    private es.upm.etsiinf.proyectofinalpegatinas.utils.ShakeDetector mShakeDetector;
    private android.hardware.SensorManager mSensorManager;
    private android.hardware.Sensor mAccelerometer;
    private boolean isAlphabeticalOrder = false;

    @Override
    public void onResume() {
        super.onResume();
        if (mSensorManager == null) {
            mSensorManager = (android.hardware.SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new es.upm.etsiinf.proyectofinalpegatinas.utils.ShakeDetector();
            mShakeDetector
                    .setOnShakeListener(new es.upm.etsiinf.proyectofinalpegatinas.utils.ShakeDetector.OnShakeListener() {
                        @Override
                        public void onShake(int count) {
                            handleShakeEvent();
                        }
                    });
        }
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                android.hardware.SensorManager.SENSOR_DELAY_UI);

        loadStickers();
    }

    @Override
    public void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    }

    private void handleShakeEvent() {
        isAlphabeticalOrder = !isAlphabeticalOrder;
        String msg = isAlphabeticalOrder ? "Orden Alfabético" : "Orden por Grupos";
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        loadStickers();
    }

    private void loadStickers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();
                if (context == null)
                    return;

                AlbumLocalDataSource dataSource = new AlbumLocalDataSource(context);
                final List<Sticker> stickers;

                android.content.SharedPreferences prefs = context.getSharedPreferences("user_session",
                        Context.MODE_PRIVATE);
                int userId = Integer.parseInt(prefs.getString("user_id", "-1"));

                List<Sticker> allStickers = dataSource.getAllStickersWithStatus(userId, isAlphabeticalOrder);

                final List<Object> displayList = new java.util.ArrayList<>();

                if (teamFilter != null) {
                    displayList.add(teamFilter);
                    for (Sticker s : allStickers) {
                        if (teamFilter.equals(s.getPais())) {
                            displayList.add(s);
                        }
                    }
                } else if (isAlphabeticalOrder) {
                    displayList.addAll(allStickers);
                } else {
                    // Por pais
                    String lastCountry = "";
                    for (Sticker s : allStickers) {
                        String currentCountry = s.getPais();
                        if (!currentCountry.equals(lastCountry)) {
                            displayList.add(currentCountry);
                            lastCountry = currentCountry;
                        }
                        displayList.add(s);
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new StickerRecyclerAdapter(getContext(), displayList,
                                    new StickerRecyclerAdapter.OnStickerClickListener() {
                                        @Override
                                        public void onStickerClick(Sticker sticker) {
                                            if (getActivity() instanceof MainActivity) {
                                                ((MainActivity) getActivity()).showStickerDetail(sticker);
                                            }
                                        }

                                        @Override
                                        public void onStickerToggle(final Sticker sticker, boolean isChecked) {
                                            final int newQty = isChecked ? 1 : 0;
                                            sticker.setQuantity(newQty);

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (getContext() == null)
                                                        return;
                                                    String userIdStr = getContext()
                                                            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
                                                            .getString("user_id", "-1");
                                                    int userId = Integer.parseInt(userIdStr);
                                                    AlbumLocalDataSource ds = new AlbumLocalDataSource(getContext());
                                                    ds.setUserStickerQuantity(userId, Integer.parseInt(sticker.getId()),
                                                            newQty);
                                                }
                                            }).start();
                                        }
                                    });

                            int spanCount = 2;
                            if (getResources()
                                    .getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                                spanCount = 4;
                            }

                            final int finalSpanCount = spanCount;

                            androidx.recyclerview.widget.GridLayoutManager layoutManager = new androidx.recyclerview.widget.GridLayoutManager(
                                    getContext(), spanCount);

                            layoutManager.setSpanSizeLookup(
                                    new androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                                        @Override
                                        public int getSpanSize(int position) {
                                            return adapter
                                                    .getItemViewType(position) == StickerRecyclerAdapter.TYPE_HEADER
                                                            ? finalSpanCount
                                                            : 1;
                                        }
                                    });
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                }
            }
        }).start();
    }
}
