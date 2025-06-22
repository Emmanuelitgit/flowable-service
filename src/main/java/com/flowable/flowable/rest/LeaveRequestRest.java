package com.flowable.flowable.rest;

import com.flowable.flowable.serviceImpl.LeaveRequestServiceImpl;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class LeaveRequestRest {

    @Autowired
    private LeaveRequestServiceImpl leaveRequestService;

    @PostMapping("/start")
    public ResponseEntity<?> startProcess() {
        leaveRequestService.startLeaveProcess();
        return ResponseEntity.ok("Leave process started");
    }

    @GetMapping("/tasks")
    public ResponseEntity<Object> findAll(){
        return leaveRequestService.findAll();
    }

    @GetMapping("/tasks/{username}")
    public ResponseEntity<Object> getUserTasks(@PathVariable String username) {
        return leaveRequestService.getTasksForUser(username);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable String taskId,
                                          @RequestBody Map<String, Object> variables) {
        leaveRequestService.completeTask(taskId, variables);
        return ResponseEntity.ok("Task completed");
    }
}
