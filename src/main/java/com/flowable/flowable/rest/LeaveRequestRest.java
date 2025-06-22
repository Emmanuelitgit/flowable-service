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
    public ResponseEntity<?> startProcess(@RequestBody Map<String, Object> vars) {
        leaveRequestService.startLeaveProcess(vars);
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

    @PostMapping("/tasks/complete")
    public ResponseEntity<?> completeTask(@RequestBody Map<String, Object> variables) {
        leaveRequestService.completeTask(variables);
        return ResponseEntity.ok("Task completed");
    }
}
