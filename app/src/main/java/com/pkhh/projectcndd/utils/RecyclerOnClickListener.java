package com.pkhh.projectcndd.utils;


import android.view.View;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface RecyclerOnClickListener {
    void onClick(@NonNull View view, int position);
}


