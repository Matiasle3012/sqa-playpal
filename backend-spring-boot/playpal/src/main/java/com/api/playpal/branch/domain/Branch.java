package com.api.playpal.branch.domain;

import com.api.playpal.court.domain.Court;
import com.api.playpal.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "branches")
public class Branch {

    @Id
    private String id;

    private String name;

    private String city;

    private String street;

    @DBRef
    private List<Court> courts;

    private String thumbnail_url;

    @DBRef
    @JsonIgnore
    private User provider;

    public Branch(String name, String city, String street, User provider) {
        this.name = name;
        this.city = city;
        this.street = street;
        this.provider = provider;
        this.thumbnail_url = null;
        this.courts = new ArrayList<>();
    }


    //GETTER Y SETTERS


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public List<Court> getCourts() {
        return courts;
    }

    public void setCourts(List<Court> courts) {
        this.courts = courts;
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }
}
