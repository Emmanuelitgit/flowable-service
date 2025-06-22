package com.flowable.flowable.repo;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TMSWorkFlowRepo extends JpaRepository<com.flowable.flowable.models.TMSWorkFlow, UUID> {
}
