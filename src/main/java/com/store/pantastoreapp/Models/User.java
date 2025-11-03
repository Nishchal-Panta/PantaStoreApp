package com.store.pantastoreapp.Models;

import java.util.Objects;

public abstract class User {
    protected String name;
    protected String phone;
    protected String password; // Stored securely (e.g., hashed)

    public User() {
        // Default constructor for frameworks if needed
    }

    public User(String name, String phone, String password) {
        this.name = name;
        this.phone = phone;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}