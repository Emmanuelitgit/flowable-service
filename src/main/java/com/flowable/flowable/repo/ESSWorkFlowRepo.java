package com.flowable.flowable.repo;

import com.flowable.flowable.models.ESSWorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ESSWorkFlowRepo extends JpaRepository<ESSWorkFlow, UUID> {
}
