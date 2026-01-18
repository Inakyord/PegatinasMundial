package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.loadThreads.DescargarStickersThread;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Sticker;

/**
 * Actividad principal que actúa como contenedor de navegación.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d("MainActivity", "onCreate started");

        String userIdStr = "-1";

        if (getIntent().hasExtra("intent_user_id")) {
            userIdStr = getIntent().getStringExtra("intent_user_id");
        }
        else {
            userIdStr = getSharedPreferences("user_session", MODE_PRIVATE).getString("user_id", "-1");
        }

        int userId = Integer.parseInt(userIdStr);
        if (userId == -1) {
            // No hay usuario, ir a login
            android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        try {
            setContentView(R.layout.activity_main);
            android.util.Log.d("MainActivity", "setContentView finished");

            androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            if (savedInstanceState == null) {
                // Cargar el menu principal
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new MainMenuFragment());
                transaction.commit();

                android.util.Log.d("MainActivity", "Fragment loaded");

                new Thread(new DescargarStickersThread(this)).start();
            }

            // Listen for changes
            getSupportFragmentManager().addOnBackStackChangedListener(
                    new androidx.fragment.app.FragmentManager.OnBackStackChangedListener() {
                        @Override
                        public void onBackStackChanged() {
                            boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
                            }
                        }
                    });

            if (getSupportActionBar() != null) {
                boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
                getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
            }

        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error in onCreate", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            return true;
        }

        if (item.getItemId() == R.id.action_checklist) {
            loadFragment(new ChecklistFragment());
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            // Logout
            getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply();
            startActivity(new android.content.Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showTeamStickers(String teamName) {
        AlbumFragment fragment = AlbumFragment.newInstance(teamName);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showStickerDetail(Sticker sticker) {
        StickerDetailFragment fragment = StickerDetailFragment.newInstance(sticker);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Permitir volver atrás
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }
}