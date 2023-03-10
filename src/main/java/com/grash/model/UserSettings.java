package com.grash.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private boolean emailNotified;

    private boolean emailUpdatesForWorkOrders;

    private boolean emailUpdatesForRequests;

    private boolean emailUpdatesForPurchaseOrders;

}
