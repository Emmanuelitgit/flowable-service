package com.flowable.flowable.repo;

import com.flowable.flowable.models.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApplicationTypeRepo extends JpaRepository<com.flowable.flowable.models.ApplicationType, UUID> {
    ApplicationType findByName(String name);
}
