package com.flowable.flowable.repo;


import com.flowable.flowable.models.TMSWorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TMSWorkFlowRepo extends JpaRepository<com.flowable.flowable.models.TMSWorkFlow, UUID> {
    List<TMSWorkFlow> findByApplicationTypeOrderByPriorityAsc(String applicationType);
}
