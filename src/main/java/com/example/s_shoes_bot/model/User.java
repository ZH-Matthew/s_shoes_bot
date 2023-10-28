package com.example.s_shoes_bot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = "usersData")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    private Long chat_id;
}
