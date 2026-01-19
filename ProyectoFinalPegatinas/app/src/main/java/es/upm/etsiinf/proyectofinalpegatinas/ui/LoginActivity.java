package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.User;

/**
 * Pantalla de inicio de sesión.
 */
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private ListView listView;
    private TextView tvEmpty;
    private Button btnRegister;
    private List<User> userList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Buscamos sesión iniciada
        String savedUserId = getSharedPreferences("user_session", MODE_PRIVATE).getString("user_id", null);
        if (savedUserId != null) {
            // Recuerdame
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        listView = findViewById(R.id.lv_users);
        tvEmpty = findViewById(R.id.tv_empty_users);
        btnRegister = findViewById(R.id.btn_register_main);

        loadUsers();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLoginDialog(userList.get(position));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });

        // Reset
        Button btnReset = findViewById(R.id.btn_reset_app);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("¡Cuidado!")
                        .setMessage(
                                "¿Estás seguro de restablecer la aplicación? Se borrarán todos los usuarios y progresos.")
                        .setPositiveButton("Sí, borrar todo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                performFactoryReset();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }

    private void performFactoryReset() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                        LoginActivity.this);
                dataSource.resetDatabase();

                // Clear all prefs
                getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply();
                getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, "Aplicación restablecida", Toast.LENGTH_SHORT).show();
                        loadUsers(); // Should be empty now
                    }
                });
            }
        }).start();
    }

    private void loadUsers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                        LoginActivity.this);
                userList = dataSource.getAllUsers();

                final List<String> names = new ArrayList<>();
                for (User u : userList) {
                    names.add(u.getName());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (names.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                            adapter = new ArrayAdapter<>(LoginActivity.this,
                                    android.R.layout.simple_list_item_1, names);
                            listView.setAdapter(adapter);
                        }
                    }
                });
            }
        }).start();
    }

    private void showLoginDialog(final User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Acceder: " + user.getName());

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_login, null);
        final EditText input = viewInflated.findViewById(R.id.et_login_password);
        final android.widget.CheckBox cbRemember = viewInflated.findViewById(R.id.cb_remember_me);
        builder.setView(viewInflated);

        builder.setPositiveButton("Entrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean remember = cbRemember.isChecked();
                attemptLogin(user.getName(), input.getText().toString(), remember);
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo Usuario");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        final EditText inputName = viewInflated.findViewById(R.id.et_reg_username);
        final EditText inputPass = viewInflated.findViewById(R.id.et_reg_password);
        builder.setView(viewInflated);

        builder.setPositiveButton("Crear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                attemptRegister(inputName.getText().toString(), inputPass.getText().toString());
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void attemptLogin(final String username, final String password, final boolean remember) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                        LoginActivity.this);

                final User user = dataSource.loginUser(username, password);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null) {
                            if (remember) {
                                getSharedPreferences("user_session", MODE_PRIVATE).edit()
                                        .putString("user_id", user.getId())
                                        .putString("user_name", user.getName())
                                        .apply();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            } else {
                                // Clear any previous session
                                getSharedPreferences("user_session", MODE_PRIVATE).edit().clear().apply();
                                // Pass session via Intent
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("intent_user_id", user.getId());
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void attemptRegister(final String username, final String password) {
        if (username.isEmpty() || password.isEmpty())
            return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                        LoginActivity.this);

                final User user = dataSource.registerUser(username,
                        password);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (user != null) {
                            Toast.makeText(LoginActivity.this, "Usuario creado", Toast.LENGTH_SHORT).show();
                            loadUsers(); // Refresh list
                        } else {
                            Toast.makeText(LoginActivity.this, "El usuario ya existe", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }
}
