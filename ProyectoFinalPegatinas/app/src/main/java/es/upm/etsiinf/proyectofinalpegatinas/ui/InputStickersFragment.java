package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Sticker;

public class InputStickersFragment extends Fragment {

    private EditText etInput;
    private TextView tvResult;

    public InputStickersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_input_stickers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etInput = view.findViewById(R.id.et_sticker_input);
        tvResult = view.findViewById(R.id.tv_input_result);
        Button btnAdd = view.findViewById(R.id.btn_add_stickers);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processInput();
            }
        });
    }

    private void processInput() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty())
            return;

        tvResult.setText("Procesando...");

        final String finalInput = input;

        new Thread(new Runnable() {
            @Override
            public void run() {
                es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                        requireContext());

                String userIdStr = requireContext()
                        .getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                        .getString("user_id", "-1");
                int userId = Integer.parseInt(userIdStr);

                if (userId == -1) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast
                                .makeText(getContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                // Separar en dos columnas
                String[] tokens = finalInput.split("[^0-9]+");
                int successCount = 0;
                int failCount = 0;

                for (String token : tokens) {
                    if (token.trim().isEmpty())
                        continue;

                    try {
                        int id = Integer.parseInt(token.trim());
                        Sticker s = dataSource.getStickerById(id);
                        if (s != null) {
                            dataSource.setUserStickerQuantity(userId, id, 1);
                            successCount++;
                        } else {
                            failCount++;
                        }
                    } catch (NumberFormatException e) {
                        failCount++;
                    }
                }

                final int s = successCount;
                final int f = failCount;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvResult.setText("Resultado:\n✅ Añadidos: " + s + "\n❌ No encontrados: " + f);
                            if (s > 0) {
                                etInput.setText("");
                                Toast.makeText(getContext(), "¡" + s + " cromos marcados como 'Lo tengo'!",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
