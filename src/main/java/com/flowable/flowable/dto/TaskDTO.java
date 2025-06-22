package com.flowable.flowable.dto;

import lombok.Data;
import org.flowable.task.api.Task;

@Data
public class TaskDTO {
    private String id;
    private String name;
    private String assignee;
    private String processInstanceId;

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.assignee = task.getAssignee();
        this.processInstanceId = task.getProcessInstanceId();
    }

}