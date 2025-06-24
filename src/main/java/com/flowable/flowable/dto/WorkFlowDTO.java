package com.flowable.flowable.dto;

import com.flowable.flowable.models.CompleteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowDTO {
    private UUID id;
    private Data flow;
    private CompleteStatus completeStatus;

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data{
        private UUID applicationId;
        private String name;
        private Integer priority;
    }
}
