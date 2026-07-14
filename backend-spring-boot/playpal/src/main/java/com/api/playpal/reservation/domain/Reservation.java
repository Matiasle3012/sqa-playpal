package com.api.playpal.reservation.domain;

import com.api.playpal.court.domain.Court;
import com.api.playpal.user.domain.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "reservations")
public class Reservation {

    @Id
    private String id;

    @DBRef
    private Court court;

    @DBRef
    private User user;

    private String start;
    private String end;
    private LocalDate date; // YYYY-MM-DD
    private boolean active;
    private String createdAt;
    private Long totalPrice;

    public Reservation(String start, String end, Long totalPrice, LocalDate date , Court court, User user) {
        this.start = start;
        this.end = end;
        this.createdAt = LocalDateTime.now().toString();
        this.totalPrice = totalPrice;
        this.date = date;
        this.active = true;
        this.court = court;
        this.user = user;
    }

    //GETTERS Y SETTERS


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Court getCourt() {
        return court;
    }

    public void setCourt(Court court) {
        this.court = court;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Long totalPrice) {
        this.totalPrice = totalPrice;
    }
}
