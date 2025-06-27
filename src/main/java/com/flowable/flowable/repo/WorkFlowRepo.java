package com.flowable.flowable.repo;


import com.flowable.flowable.models.WorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkFlowRepo extends JpaRepository<WorkFlow, UUID> {

    List<WorkFlow> findByApplicationIdOrderByPriorityAsc(UUID applicationId);

    Optional<WorkFlow> findByName(String name);


    @Query(value = "SELECT * FROM work_flow_tb fl WHERE fl.application_id=:applicationId AND fl.request_type=:requestType AND fl.name=:name", nativeQuery = true)
    WorkFlow findByApplicationIdAndRequestTypeAndName(UUID applicationId, String requestType, String name);

    List<WorkFlow> findByApplicationId(UUID applicationId);

    List<WorkFlow> findByApplicationIdAndRequestTypeOrderByPriority(UUID applicationId, String requestType);

    List<WorkFlow> findByApplicationIdAndRequestTypeOrderByPriorityAsc(UUID applicationId, String requestType);
}
