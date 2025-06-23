package com.flowable.flowable.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "tms_flow_setup_tb")
public class TMSWorkFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String applicationType;
    private String name;
    private Integer priority;
}
