package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Team;

public class TeamsFragment extends Fragment {

    private ListView listView;
    private ProgressBar progressBar;
    private SectionedAdapter adapter;
    private List<Object> displayList;

    public TeamsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.lv_teams);
        progressBar = view.findViewById(R.id.loading_teams);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = displayList.get(position);
                if (item instanceof Team) {
                    Team team = (Team) item;
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showTeamStickers(team.getName());
                    }
                }
            }
        });
    }

    private android.content.BroadcastReceiver dataReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if ("es.upm.etsiinf.proyectofinalpegatinas.DATA_UPDATED".equals(intent.getAction())) {
                Toast.makeText(context, "Datos actualizados, recargando...", Toast.LENGTH_SHORT).show();
                loadTeams();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            android.content.IntentFilter filter = new android.content.IntentFilter(
                    "es.upm.etsiinf.proyectofinalpegatinas.DATA_UPDATED");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                getContext().registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                getContext().registerReceiver(dataReceiver, filter);
            }
        }
        loadTeams();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            getContext().unregisterReceiver(dataReceiver);
        }
    }

    private void loadTeams() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = getContext();
                    if (context == null)
                        return;

                    AlbumLocalDataSource dataSource = new AlbumLocalDataSource(context);
                    List<Team> teams = dataSource.getTeams();
                    if (teams == null)
                        teams = new java.util.ArrayList<>();

                    final List<Object> processed = new java.util.ArrayList<>();
                    String currentGroup = "";
                    for (Team t : teams) {
                        if (!t.getGroup().equals(currentGroup)) {
                            currentGroup = t.getGroup();
                            processed.add(currentGroup); // Header
                        }
                        processed.add(t);
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (processed.isEmpty()) {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Sincronizando datos...", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    // Retry automatico por si se perdió el broadcast
                                    if (getView() != null && getContext() != null) {
                                        getView().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (isAdded() && getContext() != null) {
                                                    loadTeams();
                                                }
                                            }
                                        }, 2000);
                                    }
                                } else {
                                    if (progressBar != null)
                                        progressBar.setVisibility(View.GONE);
                                    displayList = processed;
                                    adapter = new SectionedAdapter(getContext(), displayList);
                                    if (listView != null)
                                        listView.setAdapter(adapter);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class SectionedAdapter extends android.widget.BaseAdapter {
        private Context context;
        private List<Object> items;
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        public SectionedAdapter(Context context, List<Object> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
        }

        @Override
        public boolean isEnabled(int position) {
            return (getItemViewType(position) == TYPE_ITEM);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (convertView == null) {
                if (type == TYPE_HEADER) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
                } else {
                    convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent,
                            false);
                }
            }

            if (type == TYPE_HEADER) {
                android.widget.TextView tv = (android.widget.TextView) convertView.findViewById(R.id.tv_header_title);
                tv.setText((String) items.get(position));
            } else {
                android.widget.TextView tv = (android.widget.TextView) convertView.findViewById(android.R.id.text1);
                Team team = (Team) items
                        .get(position);
                tv.setText(team.getName());
            }
            return convertView;
        }
    }
}
