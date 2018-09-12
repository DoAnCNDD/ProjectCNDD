package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

import androidx.annotation.NonNull;

public abstract class FirebaseModel {
    public String id;

    @NonNull
    public static <T extends FirebaseModel> T parseDocumentSnapshot(@NonNull DocumentSnapshot snapshot, Class<T> tClass) {
        T t = Objects.requireNonNull(snapshot.toObject(tClass));
        t.id = snapshot.getId();
        return t;
    }
}
