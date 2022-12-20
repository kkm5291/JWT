package com.example.jwt.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private String password;
    private String roles; // USER, ADMIN

    // 만약 만드는 서비스가 role이 하나라면 이렇게 만들 필요가 없음
    // 걍 이메서드 자체가 필요가 없어!
    public List<String> getRoleList() {
        if (this.roles.length() > 0) {
            return Arrays.asList(this.roles.split(","));
        }
        return new ArrayList<>(); // null이 안뜨도록!
    }
}
