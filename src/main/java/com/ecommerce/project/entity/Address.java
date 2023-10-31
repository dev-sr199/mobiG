package com.ecommerce.project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String streetAddress;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public String getFullAddress() {
        return streetAddress + ", " + city + ", " + state + ", " + postalCode + ", " + country;
    }

    public Address(User user) {
        this.user = user;
    }

    public Address() {

    }
}

