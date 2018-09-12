package com.pkhh.projectcndd.ui;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.pkhh.projectcndd.models.FirebaseModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.pkhh.projectcndd.models.FirebaseModel.parseDocumentSnapshot;

public abstract class MyFirebaseLoadMoreAdapter<T extends FirebaseModel> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static final int TYPE_LOAD_MORE = 1;
    static final int TYPE_FIREBASE_MODEL_ITEM = 2;
    private static final Object LOAD_MORE_ITEM = new Object();
    private final int pageSize;
    private final Query query;
    private final Class<T> tClass;

    private @Nullable
    DocumentSnapshot lastVisible;
    private @NonNull
    List<Object> list = new ArrayList<>();

    private boolean isLoading = false;
    private boolean isLastItemReached = false;
    private boolean hasError = false;
    private double visibleThreshold;


    MyFirebaseLoadMoreAdapter(@NonNull Query query, int pageSize, @NonNull RecyclerView recyclerView, @NonNull Class<T> tClass) {

        this.pageSize = pageSize;
        this.query = query;
        this.tClass = tClass;
        this.visibleThreshold = pageSize;

        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalStateException("Not implementation");
        }
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                int itemCount = linearLayoutManager.getItemCount();
                if (lastVisible != null && !isLoading
                        && lastVisibleItemPosition + visibleThreshold >= itemCount
                        && !hasError && !isLastItemReached) {
                    loadMore();
                    isLoading = true;
                }

                if (lastVisibleItemPosition == itemCount && isLastItemReached) {
                    onLastItemReached();
                }
            }
        });
        loadMore();
    }


    public void refresh() {
        lastVisible = null;
        isLoading = false;
        isLastItemReached = false;
        hasError = false;
        loadMore();
    }

    private void loadMore() {
        if (lastVisible != null) {
            list.add(LOAD_MORE_ITEM);
            notifyItemInserted(list.size() - 1);
        }

        (lastVisible != null ? query.startAfter(lastVisible) : query)
                .limit(pageSize)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<T> firebaseModels = new ArrayList<>();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        firebaseModels.add(parseDocumentSnapshot(snapshot, tClass));
                    }

                    if (!list.isEmpty()) {
                        list.remove(list.size() - 1);
                        notifyItemRemoved(list.size());
                    }
                    if (lastVisible == null) {
                        list.clear();
                        notifyDataSetChanged();
                    }
                    int oldSize = list.size();
                    list.addAll(firebaseModels);
                    notifyItemRangeInserted(oldSize, firebaseModels.size());

                    if (lastVisible == null) {
                        onFirstLoaded();
                    }
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    if (queryDocumentSnapshots.size() < pageSize) {
                        isLastItemReached = true;
                    }
                    isLoading = false;
                })
                .addOnFailureListener(e -> hasError = true);
    }

    protected void onLastItemReached() {
    }

    protected void onFirstLoaded() {
    }

    @NonNull
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        Object item = getItem(position);
        if (item == LOAD_MORE_ITEM) {
            return TYPE_LOAD_MORE;
        }
        if (item.getClass().equals(tClass)) {
            return TYPE_FIREBASE_MODEL_ITEM;
        }
        throw new IllegalStateException();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
