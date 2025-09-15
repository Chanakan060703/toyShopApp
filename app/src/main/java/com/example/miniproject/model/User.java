package com.example.miniproject.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;

    // Constructor
    public User(String firstname, String lastname, String email, String password) {
        this.firstname = firstname != null ? firstname : "";
        this.lastname = lastname != null ? lastname : "";
        this.email = email != null ? email : "";
        this.password = password != null ? password : "";
    }

    // Parcelable Constructor
    protected User(Parcel in) {
        id = in.readString();
        firstname = in.readString();
        lastname = in.readString();
        email = in.readString();
        password = in.readString();
    }

    // Parcelable Creator
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname != null ? firstname : ""; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname != null ? lastname : ""; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email : ""; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password != null ? password : ""; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeString(firstname);
        parcel.writeString(lastname);
        parcel.writeString(email);
        parcel.writeString(password);
    }
}