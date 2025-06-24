package com.flowable.flowable.repo;


import com.flowable.flowable.models.WorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkFlowRepo extends JpaRepository<WorkFlow, UUID> {

    List<WorkFlow> findByApplicationIdOrderByPriorityAsc(UUID applicationId);

    Optional<WorkFlow> findByName(String name);
}
