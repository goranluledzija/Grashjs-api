package com.grash.model;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class SingleWorkOrderFieldConfiguration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private FieldConfiguration fieldConfiguration;
    private boolean forCreation;

    @ManyToOne
    @NotNull
    WorkOrderConfiguration workOrderConfiguration;


}
