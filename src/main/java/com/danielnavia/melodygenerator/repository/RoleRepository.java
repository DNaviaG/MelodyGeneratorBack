package com.danielnavia.melodygenerator.repository;

import com.danielnavia.melodygenerator.entity.RoleEntity;
import com.danielnavia.melodygenerator.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    Optional<RoleEntity> findByName(Role name);
}