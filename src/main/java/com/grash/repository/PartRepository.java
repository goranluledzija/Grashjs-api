package com.grash.repository;

import com.grash.model.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface PartRepository extends JpaRepository<Part, Long> {
    Collection<Part> findByCompany_Id(@Param("x") Long id);
}
