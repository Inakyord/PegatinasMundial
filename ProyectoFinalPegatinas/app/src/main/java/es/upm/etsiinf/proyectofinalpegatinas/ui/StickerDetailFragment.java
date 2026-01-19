package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Sticker;
import java.io.File;
import android.net.Uri;

/**
 * Fragmento para ver el detalle de un cromo.
 */
public class StickerDetailFragment extends Fragment {

    private static final String ARG_STICKER = "arg_sticker";
    private Sticker sticker;

    public StickerDetailFragment() {
    }

    public static StickerDetailFragment newInstance(Sticker sticker) {
        StickerDetailFragment fragment = new StickerDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STICKER, sticker);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sticker = (Sticker) getArguments().getSerializable(ARG_STICKER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sticker_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvName = view.findViewById(R.id.tv_detail_name);
        TextView tvTeam = view.findViewById(R.id.tv_detail_team);
        android.widget.ImageView ivImage = view.findViewById(R.id.iv_detail_image);
        Button btnShare = view.findViewById(R.id.btn_share);

        if (sticker != null) {
            tvName.setText("#" + sticker.getId() + " " + sticker.getName());
            tvTeam.setText(sticker.getTeam());

            // Cargar imagen
            String customUri = sticker.getCustomImageUri();
            if (customUri != null && !customUri.isEmpty()) {
                ivImage.setImageURI(android.net.Uri.parse(customUri));
            } else {
                // Default image logic
                int resId = R.drawable.ic_app_icon; // Fallback
                String pos = sticker.getPosicion();

                boolean imageSet = false;
                if (pos != null) {
                    String fileName = "default_" + pos.toLowerCase() + ".png";
                    File file = new File(getContext().getFilesDir(), fileName);
                    if (file.exists()) {
                        ivImage.setImageURI(Uri.fromFile(file));
                        imageSet = true;
                    }
                }

                if (!imageSet) {
                    ivImage.setImageResource(resId);
                }
            }

            // Cambio de imagen
            ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickImageFromGallery();
                }
            });

            TextView tvAge = view.findViewById(R.id.tv_detail_age);
            TextView tvClub = view.findViewById(R.id.tv_detail_club);
            TextView tvHeight = view.findViewById(R.id.tv_detail_height);
            TextView tvPosDesc = view.findViewById(R.id.tv_detail_position_desc);

            if (tvAge != null) {
                tvAge.setText(getString(R.string.detail_age, sticker.getEdad()));
                tvClub.setText(getString(R.string.detail_club, sticker.getClub()));
                tvHeight.setText(getString(R.string.detail_height, sticker.getEstatura()));
                tvPosDesc.setText(getString(R.string.detail_position_desc, sticker.getPosicionDescripcion()));
            }
        }

        // Control de cantidad
        final TextView tvQty = view.findViewById(R.id.tv_detail_quantity);
        Button btnMinus = view.findViewById(R.id.btn_qty_minus);
        Button btnPlus = view.findViewById(R.id.btn_qty_plus);
        final TextView tvStatus = view.findViewById(R.id.tv_detail_status);

        if (sticker != null) {
            updateQuantityUI(tvQty, tvStatus, sticker.getQuantity());

            btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeQuantity(tvQty, tvStatus, -1);
                }
            });

            btnPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeQuantity(tvQty, tvStatus, 1);
                }
            });
        }

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvQty.setVisibility(View.INVISIBLE);
                btnMinus.setVisibility(View.INVISIBLE);
                btnPlus.setVisibility(View.INVISIBLE);
                tvStatus.setVisibility(View.INVISIBLE);
                btnShare.setVisibility(View.INVISIBLE);

                // Compartir
                es.upm.etsiinf.proyectofinalpegatinas.utils.ShareUtils.shareStickerScreenshot(
                        getContext(), view, sticker.getName());
                tvQty.setVisibility(View.VISIBLE);
                btnMinus.setVisibility(View.VISIBLE);
                btnPlus.setVisibility(View.VISIBLE);
                tvStatus.setVisibility(View.VISIBLE);
                btnShare.setVisibility(View.VISIBLE);
            }
        });
    }

    private void pickImageFromGallery() {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 1001); // REQUEST_CODE_PICK_IMAGE
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            final android.net.Uri uri = data.getData();
            if (uri != null) {
                final int takeFlags = data.getFlags()
                        & (android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                | android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }

                android.widget.ImageView ivImage = getView().findViewById(R.id.iv_detail_image);
                ivImage.setImageURI(uri);
                sticker.setCustomImageUri(uri.toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (getContext() == null)
                            return;

                        String userIdStr = getContext()
                                .getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                                .getString("user_id", "-1");
                        int userId = Integer.parseInt(userIdStr);

                        es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                                getContext());
                        dataSource.updateStickerImage(userId, Integer.parseInt(sticker.getId()), uri.toString());
                    }
                }).start();
            }
        }
    }

    private void updateQuantityUI(TextView tvQty, TextView tvStatus, int quantity) {
        tvQty.setText(String.valueOf(quantity));
        if (quantity > 0) {
            tvStatus.setText("Estado: LO TENGO");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStatus.setText("Estado: FALTA");
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void changeQuantity(final TextView tvQty, final TextView tvStatus, final int delta) {
        final int current = sticker.getQuantity();
        final int newQuantity = Math.max(0, current + delta);

        if (current == newQuantity)
            return;

        sticker.setQuantity(newQuantity);
        updateQuantityUI(tvQty, tvStatus, newQuantity);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getContext() == null)
                    return;
                String userIdStr = getContext()
                        .getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                        .getString("user_id", "-1");
                int userId = Integer.parseInt(userIdStr);

                es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource dataSource = new es.upm.etsiinf.proyectofinalpegatinas.data.local.AlbumLocalDataSource(
                        getContext());

                dataSource.setUserStickerQuantity(userId, Integer.parseInt(sticker.getId()), newQuantity);

                if (current == 0 && newQuantity > 0) {
                    if (dataSource.isAlbumComplete(userId)) {
                        es.upm.etsiinf.proyectofinalpegatinas.utils.NotificationHelper.showMilestoneNotification(
                                getContext(), "¡Álbum Completado! 🏆", "¡Felicidades! Has completado todo el álbum.");
                    }
                }
            }
        }).start();
    }
}
