package com.pkhh.projectcndd.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.models.User;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_DIV;
import static com.pkhh.projectcndd.models.FirebaseModel.parseDocumentSnapshot;
import static com.pkhh.projectcndd.ui.MotelRoomVH.decimalFormat;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_NAME_COLLECION;
import static java.util.Objects.requireNonNull;

public class MotelRoomDetailActivity extends AppCompatActivity implements View.OnClickListener {
    @SuppressLint("SimpleDateFormat")
    public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private SliderLayout sliderLayout;
    private TextView textPrice;
    private TextView textAddress;
    private TextView textTimePost;
    private TextView textArea;
    private ImageView imageMap;
    private TextView textName;
    private TextView textPhone;
    private ImageView imageAvatar;

    @Nullable
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motel_room_detail);

        requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        sliderLayout = findViewById(R.id.sliderLayout);
        textPrice = findViewById(R.id.text_price);
        textAddress = findViewById(R.id.text_address);
        textTimePost = findViewById(R.id.text_post_time);
        textArea = findViewById(R.id.text_area);
        imageMap = findViewById(R.id.image_map);
        textName = findViewById(R.id.text_name);
        textPhone = findViewById(R.id.text_phone);
        imageAvatar = findViewById(R.id.image_avatar);

        findViewById(R.id.button_sms).setOnClickListener(this);
        findViewById(R.id.button_call).setOnClickListener(this);

        String id = getIntent().getStringExtra(MOTEL_ROOM_ID);
        firestore.document(MOTEL_ROOM_NAME_COLLECION + "/" + id)
                .addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        updateUi(parseDocumentSnapshot(queryDocumentSnapshots, MotelRoom.class));
                    }
                });
    }

    private void updateUi(@NonNull MotelRoom motelRoom) {
        updateImageSlider(motelRoom);

        textPrice.setText("$ " + decimalFormat.format(motelRoom.price) + " đ");
        textAddress.setText(motelRoom.address);
        textTimePost.setText("Ngày đăng: " + dateFormat.format(motelRoom.createdAt));
        textArea.setText(HtmlCompat.fromHtml(motelRoom.size + " m<sup><small>2</small></sup>",
                FROM_HTML_SEPARATOR_LINE_BREAK_DIV));
        imageMap.setImageResource(R.drawable.ic_home_black_24dp); // TODO

        motelRoom.user.get()
                .addOnSuccessListener(documentSnapshot -> {
                    user = parseDocumentSnapshot(documentSnapshot, User.class);
                    updateUserInformation(user);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserInformation(@NonNull User user) {
        Picasso.get()
                .load(user.avatar)
                .fit()
                .centerCrop()
                .noFade()
                .into(imageAvatar);
        textName.setText(user.fullName);
        textPhone.setText(user.phone);
    }

    private void updateImageSlider(@NonNull MotelRoom motelRoom) {
        sliderLayout.removeAllSliders();

        int index = 0;
        for (String e : motelRoom.images) {
            BaseSliderView sliderView = new TextSliderView(this)
                    .description("Ảnh " + ++index)
                    .image(e)
                    .setOnSliderClickListener(slider -> setOnSliderClickListener(slider.getUrl()))
                    .setScaleType(BaseSliderView.ScaleType.Fit);
            sliderLayout.addSlider(sliderView);
        }
    }

    private void setOnSliderClickListener(String url) {
        // TODO
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sliderLayout.startAutoCycle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sliderLayout.stopAutoCycle();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_call) {
            if (user != null && isTelephonyEnabled()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + user.phone));
                startActivity(intent);
            }
            return;
        }
        if (v.getId() == R.id.button_sms) {
            if (user != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setType("vnd.android-dir/mms-sms");
                intent.putExtra("address", user.phone);
                intent.putExtra("sms_body", "Nice");
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isTelephonyEnabled() {
        TelephonyManager manager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return manager != null && manager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }
}
