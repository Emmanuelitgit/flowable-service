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
@Table(name = "work_flow_tb")
public class WorkFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String requestType;
    private UUID applicationId;
    private String name;
    private Integer priority;
}
