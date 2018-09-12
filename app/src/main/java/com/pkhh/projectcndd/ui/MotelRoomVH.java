package com.pkhh.projectcndd.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class MotelRoomVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    static final DecimalFormat decimalFormat = new DecimalFormat("###,###");

    private final ImageView imagePreview;
    private final TextView textPrice;
    private final TextView textAddress;
    private final TextView textPostBy;
    private final ImageView imageShare;
    private final ImageView imageSave;
    private final RecyclerOnClickListener recyclerClickListener;

    MotelRoomVH(@NonNull View itemView, @NonNull RecyclerOnClickListener recyclerClickListener) {
        super(itemView);
        textPrice = itemView.findViewById(R.id.text_price);
        textAddress = itemView.findViewById(R.id.text_address);
        textPostBy = itemView.findViewById(R.id.text_post_by);
        imageShare = itemView.findViewById(R.id.image_share);
        imageSave = itemView.findViewById(R.id.image_save);
        imagePreview = itemView.findViewById(R.id.image_preview);
        this.recyclerClickListener = recyclerClickListener;

        imageSave.setOnClickListener(this);
        imageShare.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    void bind(MotelRoom motelRoom) {
        textPrice.setText("$ " + decimalFormat.format(motelRoom.price) + " đ");
        textAddress.setText(motelRoom.address);
        motelRoom.user.get()
                .addOnSuccessListener(documentSnapshot -> textPostBy.setText("đăng bởi " + documentSnapshot.get("full_name")))
                .addOnFailureListener(e -> textPostBy.setText("đăng bởi ..."));

        List<String> imageUrls = motelRoom.images;
        if (imageUrls != null && !imageUrls.isEmpty()) {
            Picasso.get()
                    .load(imageUrls.get(0))
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.ic_home_black_24dp)
                    .error(R.drawable.ic_home_black_24dp)
                    .into(imagePreview);
        }
    }

    @Override
    public void onClick(View v) {
        final int adapterPosition = getAdapterPosition();
        if (adapterPosition != NO_POSITION) {
            recyclerClickListener.onClick(v, adapterPosition);
        }
    }
}