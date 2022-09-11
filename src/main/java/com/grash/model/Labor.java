package com.grash.model;

import com.grash.model.abstracts.Time;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class Labor extends Time {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User worker;

    @OneToOne
    private LaborCost laborCost;

    @ManyToOne
    @NotNull
    private WorkOrder workOrder;
}
