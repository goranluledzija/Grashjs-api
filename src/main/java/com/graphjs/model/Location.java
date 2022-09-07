package com.graphjs.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String address;

    private String gps;

//    private List<Asset> assetList;

//    private List<User> workers;

//    private List<Team> teamList;

//    Parent: Location

//    private Vendor vendor;

//    private Customer customer;

//    private List<Part> partList;

//    private List<FloorPlan> floorPlanList;
}
