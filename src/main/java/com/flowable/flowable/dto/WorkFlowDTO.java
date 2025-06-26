package com.flowable.flowable.dto;

import com.flowable.flowable.models.CompleteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlowDTO {
    private List<Data> flow;
    private CompleteStatus completeStatus;
    private UUID applicationId;
    private String requestType;

    @lombok.Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Data{
        private UUID id;
        private String name;
        private Integer priority;
    }
}
