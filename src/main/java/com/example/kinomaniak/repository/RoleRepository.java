package com.example.kinomaniak.repository;

import com.example.kinomaniak.model.Role;
import javafx.beans.Observable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Override
    Optional<Role> findById(Integer integer);

    Optional<Role> findByRoleName(String roleName);
}
