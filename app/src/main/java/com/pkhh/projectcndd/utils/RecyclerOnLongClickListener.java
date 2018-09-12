package com.pkhh.projectcndd.utils;

import android.view.View;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface RecyclerOnLongClickListener {
    void onLongClick(@NonNull View view, int position);
}
