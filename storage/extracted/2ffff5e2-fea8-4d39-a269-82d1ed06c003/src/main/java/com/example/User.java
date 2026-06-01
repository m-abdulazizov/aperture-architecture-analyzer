package com.example;
import jakarta.persistence.*;
@Entity
public class User {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Department department;
}
