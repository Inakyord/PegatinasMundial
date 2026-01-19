package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Sticker;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Team;
import es.upm.etsiinf.proyectofinalpegatinas.utils.ShakeDetector;

public class ChecklistFragment extends Fragment {

    private ListView listView;
    private ChecklistAdapter adapter;
    private List<Sticker> masterStickerList; // All stickers
    private List<Sticker> displayedList; // Filtered
    private Spinner spinnerCountry;
    private Switch switchMissing;
    private Switch switchRepeated;
    private List<Team> teamList; // For spinner

    // Shake
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    public ChecklistFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checklist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.lv_checklist);
        spinnerCountry = view.findViewById(R.id.spinner_filter_country);
        switchMissing = view.findViewById(R.id.switch_filter_missing);
        switchRepeated = view.findViewById(R.id.switch_filter_repeated);
        Button btnShare = view.findViewById(R.id.btn_share);

        masterStickerList = new ArrayList<>();
        displayedList = new ArrayList<>();
        adapter = new ChecklistAdapter(getContext(), displayedList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sticker sticker = displayedList.get(position);
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showStickerDetail(sticker);
                }
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareMissingStickers();
            }
        });

        // Initialize Filters Listeners
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        switchMissing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && switchRepeated != null)
                    switchRepeated.setChecked(false); // Mutually exclusive
                applyFilters();
            }
        });

        if (switchRepeated != null) {
            switchRepeated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && switchMissing != null)
                        switchMissing.setChecked(false); // Mutually exclusive
                    applyFilters();
                }
            });
        }

        loadData();
    }

    // Shake Logic
    @Override
    public void onResume() {
        super.onResume();
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();
            mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
                @Override
                public void onShake(int count) {
                    clearFilters();
                }
            });
        }
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        loadData();
    }

    @Override
    public void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        super.onPause();
    }

    private void clearFilters() {
        if (spinnerCountry != null && spinnerCountry.getAdapter() != null) {
            spinnerCountry.setSelection(0); // "Todos"
        }
        if (switchMissing != null) {
            switchMissing.setChecked(false);
        }
        if (switchRepeated != null) {
            switchRepeated.setChecked(false);
        }
        Toast.makeText(getContext(), "Filtros borrados (Shake)", Toast.LENGTH_SHORT).show();
        applyFilters();
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null)
                    return;

                final AlbumLocalDataSource dataSource = new AlbumLocalDataSource(getContext());
                String userIdStr = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        .getString("user_id", "-1");
                int userId = Integer.parseInt(userIdStr);

                // Load all stickers
                final List<Sticker> all = dataSource.getAllStickersWithStatus(userId);

                // Load Teams
                final List<Team> teams = dataSource.getTeams();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            masterStickerList.clear();
                            masterStickerList.addAll(all);

                            setupSpinner(teams);
                            applyFilters();
                        }
                    });
                }
            }
        }).start();
    }

    private void setupSpinner(List<Team> teams) {
        teamList = new ArrayList<>();
        Team all = new Team("Todos", "ALL", "Global", null);
        teamList.add(all);
        teamList.addAll(teams);

        List<String> names = new ArrayList<>();
        for (Team t : teamList) {
            names.add(t.getName());
        }

        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, names);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(spinAdapter);
    }

    private void applyFilters() {
        if (masterStickerList == null)
            return;

        List<Sticker> result = new ArrayList<>();

        // 1. Country Filter
        String selectedCountry = "Todos";
        if (spinnerCountry != null && spinnerCountry.getSelectedItem() != null) {
            selectedCountry = (String) spinnerCountry.getSelectedItem();
        }

        // 2. Missing/Repeated Filter
        boolean onlyMissing = switchMissing != null && switchMissing.isChecked();
        boolean onlyRepeated = switchRepeated != null && switchRepeated.isChecked();

        for (Sticker s : masterStickerList) {
            boolean matchCountry = selectedCountry.equals("Todos") || s.getPais().equals(selectedCountry);

            boolean matchStatus = true;
            if (onlyMissing) {
                matchStatus = (s.getQuantity() == 0);
            } else if (onlyRepeated) {
                matchStatus = (s.getQuantity() > 1);
            }

            if (matchCountry && matchStatus) {
                result.add(s);
            }
        }

        displayedList.clear();
        displayedList.addAll(result);
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    private void shareMissingStickers() {
        if (displayedList == null || displayedList.isEmpty())
            return;

        StringBuilder sb = new StringBuilder();
        sb.append("Checklist filtrado - Proyecto Pegatinas:\n\n");

        int count = 0;
        for (Sticker s : displayedList) {
            if (s.getQuantity() == 0) {
                sb.append("- ").append("#").append(s.getId()).append(" ").append(s.getName()).append(" (")
                        .append(s.getCodigoEquipo()).append(")\n");
                count++;
            }
        }

        if (count == 0 && !displayedList.isEmpty()) {
            sb.append("¡De esta lista los tengo todos! :D");
        } else if (displayedList.isEmpty()) {
            sb.append("Lista vacía.");
        }

        android.content.Intent sendIntent = new android.content.Intent();
        sendIntent.setAction(android.content.Intent.ACTION_SEND);
        sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
        sendIntent.setType("text/plain");

        android.content.Intent shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir...");
        startActivity(shareIntent);
    }

    private class ChecklistAdapter extends BaseAdapter {
        private Context context;
        private List<Sticker> items;
        private LayoutInflater inflater;

        public ChecklistAdapter(Context context, List<Sticker> items) {
            this.context = context;
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Sticker getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(items.get(position).getId());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_checklist, parent, false);
            }

            final Sticker sticker = items.get(position);

            TextView tvInfo = convertView.findViewById(R.id.tv_checklist_info);
            final CheckBox cb = convertView.findViewById(R.id.cb_checklist_item);

            tvInfo.setText("#" + sticker.getId() + " - " + sticker.getName() + " (" + sticker.getTeam() + ")");

            cb.setOnCheckedChangeListener(null);
            cb.setChecked(sticker.getQuantity() > 0);

            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = cb.isChecked();
                    final int newQty = isChecked ? 1 : 0;
                    sticker.setQuantity(newQty);

                    // Update DB
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (getContext() == null)
                                return;
                            String userIdStr = getContext().getSharedPreferences("user_session", Context.MODE_PRIVATE)
                                    .getString("user_id", "-1");
                            int userId = Integer.parseInt(userIdStr);
                            AlbumLocalDataSource ds = new AlbumLocalDataSource(getContext());
                            ds.setUserStickerQuantity(userId, Integer.parseInt(sticker.getId()), newQty);
                        }
                    }).start();

                    if (switchMissing != null && switchMissing.isChecked()) {
                        applyFilters();
                    }
                }
            });

            return convertView;
        }
    }
}
