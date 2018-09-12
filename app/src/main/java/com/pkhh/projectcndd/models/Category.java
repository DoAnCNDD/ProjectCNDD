package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Category extends FirebaseModel {
    public String name;

    @PropertyName("created_at")
    public Date createdAt;

    @PropertyName("updated_at")
    public Date updatedAt;

    public Category() {
    }
}
