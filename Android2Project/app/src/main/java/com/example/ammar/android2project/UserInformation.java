package com.example.ammar.android2project;

import java.util.Date;

/**
 * Created by YAS on 11/28/2017.
 */

public class UserInformation {

    private String name;
    private String email;
    private String longitude;
    private String latitude;
    private String registrationDate;
    private boolean status;


    public UserInformation() {

    }

    public UserInformation(String email, String name, String longitude, String latitude, String registrationDate,boolean status) {
        this.email = email;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getRegistrationDate() {return registrationDate;}

    public void setRegistrationDate(String registrationDate) {this.registrationDate = registrationDate;}

    public boolean isStatus() {return status;}

    public void setStatus(boolean status) {this.status = status;}


}
