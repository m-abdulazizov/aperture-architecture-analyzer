package com.example;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class User {

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Department department;
}
