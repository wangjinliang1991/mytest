package com.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
@Data
@AllArgsConstructor
public class UserInfo implements Serializable {
    private Integer id;
    private String name;
    private Integer age;
    private String gender;
    private String address;
}
