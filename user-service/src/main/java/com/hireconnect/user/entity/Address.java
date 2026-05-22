package com.hireconnect.user.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address implements Serializable {

    private static final long serialVersionUID = 1L;

    private String houseNo;
    private String street;
    private String city;
    private String state;
    private Integer pincode;
}
