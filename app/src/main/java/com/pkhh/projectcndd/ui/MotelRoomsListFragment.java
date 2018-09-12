package com.pkhh.projectcndd.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.R;
import com.pkhh.projectcndd.models.FirebaseModel;
import com.pkhh.projectcndd.models.MotelRoom;
import com.pkhh.projectcndd.utils.RecyclerOnClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_ID;
import static com.pkhh.projectcndd.utils.Constants.MOTEL_ROOM_NAME_COLLECION;

public class MotelRoomsListFragment extends Fragment {
    private static final int PAGE_SIZE = 30;
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private ViewGroup rootLayout;
    private MyFirebaseLoadMoreAdapter<MotelRoom> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_motel_rooms_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViewAndAdapter(view);

        rootLayout = view.findViewById(R.id.root_motel_rooms_list_fragment);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(adapter::refresh);
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));

        FirebaseFirestore.setLoggingEnabled(true);
    }

    private void onItemClick(int viewId, @NonNull String modelId) {
        if (viewId == R.id.image_share) {
            Toast.makeText(requireContext(), "Share clicked", Toast.LENGTH_SHORT).show();
            return;
        }

        if (viewId == R.id.image_save) {
            Toast.makeText(requireContext(), "Save clicked", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), MotelRoomDetailActivity.class);
        intent.putExtra(MOTEL_ROOM_ID, modelId);
        startActivity(intent);
    }


    private void setupRecyclerViewAndAdapter(@NonNull View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Query query = firestore.collection(MOTEL_ROOM_NAME_COLLECION)
                .whereEqualTo("district", firestore.document("districts/Fd1zR38ewKGfoYaDcNbr"));

        adapter = new MyFirebaseLoadMoreAdapter<MotelRoom>(query, PAGE_SIZE, recyclerView, MotelRoom.class) {
            @Override
            protected void onLastItemReached() {
                Snackbar.make(rootLayout, "Get all!!!", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            protected void onFirstLoaded() {
                swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == TYPE_LOAD_MORE) {
                    final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_item_layout, parent, false);
                    return new LoadMoreVH(itemView);
                }
                if (viewType == TYPE_FIREBASE_MODEL_ITEM) {
                    final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.motel_room_item_layout, parent, false);
                    final RecyclerOnClickListener recyclerOnClickListener = (view, position) -> MotelRoomsListFragment.this.onItemClick(view.getId(), ((FirebaseModel) getItem(position)).id);
                    return new MotelRoomVH(
                            itemView,
                            recyclerOnClickListener
                    );
                }
                throw new IllegalStateException("Unknown view type " + viewType);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                Object item = getItem(position);

                if (holder instanceof LoadMoreVH) {
                    ((LoadMoreVH) holder).bind();
                } else if (holder instanceof MotelRoomVH && item instanceof MotelRoom) {
                    ((MotelRoomVH) holder).bind((MotelRoom) item);
                } else {
                    throw new IllegalStateException("Unknown view holder " + holder);
                }
            }

            class LoadMoreVH extends RecyclerView.ViewHolder {
                private final ProgressBar viewById;

                LoadMoreVH(@NonNull View itemView) {
                    super(itemView);
                    viewById = itemView.findViewById(R.id.progressBar2);
                }

                void bind() {
                    viewById.setIndeterminate(true);
                }
            }
        };

        recyclerView.setAdapter(adapter);
    }
}
