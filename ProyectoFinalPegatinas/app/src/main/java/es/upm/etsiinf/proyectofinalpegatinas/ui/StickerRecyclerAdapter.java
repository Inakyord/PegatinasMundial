package es.upm.etsiinf.proyectofinalpegatinas.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.io.File;
import android.net.Uri;

import es.upm.etsiinf.proyectofinalpegatinas.R;
import es.upm.etsiinf.proyectofinalpegatinas.data.local.Sticker;

public class StickerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;

    private Context context;
    private List<Object> items; // Can be String (Header) or Sticker
    private OnStickerClickListener listener;

    public interface OnStickerClickListener {
        void onStickerClick(Sticker sticker);

        void onStickerToggle(Sticker sticker, boolean isChecked);
    }

    public StickerRecyclerAdapter(Context context, List<Object> items, OnStickerClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false);
            return new StickerViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else {
            ((StickerViewHolder) holder).bind((Sticker) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ITEM;
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_header_title);
        }

        void bind(String title) {
            android.content.SharedPreferences prefs = context.getSharedPreferences("user_session",
                    Context.MODE_PRIVATE);
            String fav = prefs.getString("favorite_country_name", "");

            if (title.equalsIgnoreCase(fav)) {
                tvTitle.setText(title + " ⭐");
            } else {
                tvTitle.setText(title);
            }
        }
    }

    class StickerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvId;
        ImageView ivImage;
        CheckBox cbOwned;
        CardView cardView;

        StickerViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_sticker_name);
            tvId = itemView.findViewById(R.id.tv_sticker_id);
            ivImage = itemView.findViewById(R.id.iv_sticker_image);
            cbOwned = itemView.findViewById(R.id.cb_sticker_have);
            cardView = (CardView) itemView;
        }

        void bind(final Sticker sticker) {
            tvName.setText(sticker.getName());
            if (tvId != null) {
                tvId.setText("#" + sticker.getId());
            }

            // Image Logic
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
                    File file = new File(context.getFilesDir(), fileName);
                    if (file.exists()) {
                        ivImage.setImageURI(Uri.fromFile(file));
                        imageSet = true;
                    }
                }

                if (!imageSet) {
                    ivImage.setImageResource(resId);
                }
            }

            // Logic
            boolean isOwned = sticker.getQuantity() > 0;
            cbOwned.setOnCheckedChangeListener(null);
            cbOwned.setChecked(isOwned);

            if (isOwned) {
                cardView.setCardBackgroundColor(Color.parseColor("#A5D6A7")); // Green 200
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onStickerClick(sticker);
                }
            });

            cbOwned.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = cbOwned.isChecked();
                    if (listener != null)
                        listener.onStickerToggle(sticker, checked);
                    if (checked) {
                        cardView.setCardBackgroundColor(Color.parseColor("#A5D6A7"));
                    } else {
                        cardView.setCardBackgroundColor(Color.WHITE);
                    }
                }
            });
        }
    }
}
