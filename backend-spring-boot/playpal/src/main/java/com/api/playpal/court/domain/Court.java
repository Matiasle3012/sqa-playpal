package com.api.playpal.court.domain;

import com.api.playpal.branch.domain.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Document(collection = "courts")
public class Court {

    @Id
    private String id;

    private List<String> sports = new ArrayList<>();

    private String type;

    private int number;

    private Long price;

    private String description;

    private String hours;

    private String thumbnail_url;

    @DBRef
    @JsonIgnore
    private Branch branch;

    private String branchName;
    private String branchCity;

    public Court() {
    }

    public Court(String type, String sports, Integer number, Long price, String description, String hours) {
        this.type = type;
        this.number = number;
        this.price = price;
        this.description = description;
        this.hours = hours;
        this.thumbnail_url = null;
        this.sports = Arrays.stream(sports.split(",")).toList();
    }
    // GETTERS AND SETTERS

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
        if (branch != null) {
            this.branchName = branch.getName();
            this.branchCity = branch.getCity();
        }
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public List<String> getSports() {
        return sports;
    }

    public void setSports(String sports) {
        this.sports = Arrays.stream(sports.split(",")).toList();
    }

    public String getThumbnail_url() {
        return thumbnail_url;
    }

    public void setThumbnail_url(String thumbnail_url) {
        this.thumbnail_url = thumbnail_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCity() {
        return branchCity;
    }

    public void setBranchCity(String branchCity) {
        this.branchCity = branchCity;
    }

    public void setSports(List<String> sports) {
        this.sports = sports;
    }
}
