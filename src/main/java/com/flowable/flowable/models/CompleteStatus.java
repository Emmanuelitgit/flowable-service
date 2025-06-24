package com.flowable.flowable.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "complete_status_tb")
public class CompleteStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String onSuccess;
    private String onRejection;
    private UUID applicationId;
}
