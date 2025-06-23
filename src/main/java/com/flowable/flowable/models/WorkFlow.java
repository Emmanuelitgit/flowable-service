package com.flowable.flowable.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "work_flow_tb")
public class WorkFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private UUID applicationId;
    private String name;
    private Integer priority;
}
