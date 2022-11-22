package com.grash.repository;

import com.grash.model.PartQuantity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface PartQuantityRepository extends JpaRepository<PartQuantity, Long> {
    Collection<PartQuantity> findByCompany_Id(Long id);
}
