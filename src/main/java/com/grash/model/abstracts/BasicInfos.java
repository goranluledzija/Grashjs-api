package com.grash.model.abstracts;

import lombok.Data;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public abstract class BasicInfos extends CompanyAudit {
    private String name;
    private String address;
    private String phone;
    private String website;
    private String email;
}
