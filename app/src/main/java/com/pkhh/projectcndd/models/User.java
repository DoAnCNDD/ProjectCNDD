package com.pkhh.projectcndd.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class User extends FirebaseModel {
    public String email;

    public String phone;

    @PropertyName("full_name")
    public String fullName;

    public String address;

    public String avatar;

    @PropertyName("is_active")
    public boolean isActive;

    @PropertyName("created_at")
    public Date createdAt;

    @PropertyName("updated_at")
    public Date updatedAt;

    public User() {
    }
}
