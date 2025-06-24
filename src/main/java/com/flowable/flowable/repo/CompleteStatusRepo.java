package com.flowable.flowable.repo;

import com.flowable.flowable.models.CompleteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompleteStatusRepo extends JpaRepository<CompleteStatus, UUID> {
    CompleteStatus findByApplicationId(UUID applicationId);
}
