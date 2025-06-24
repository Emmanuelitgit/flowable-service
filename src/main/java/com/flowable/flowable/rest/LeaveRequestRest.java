package com.flowable.flowable.rest;

import com.flowable.flowable.config.kafka.dto.TMSUpdatePayload;
import com.flowable.flowable.serviceImpl.ProcessHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LeaveRequestRest {

    @Autowired
    private ProcessHandlerService leaveRequestService;

    @PostMapping("/start")
    public ResponseEntity<?> startProcess(@RequestBody TMSUpdatePayload tmsUpdatePayload) {
        leaveRequestService.startLeaveProcess(tmsUpdatePayload);
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
    public ResponseEntity<?> completeTask(@RequestBody TMSUpdatePayload tmsUpdatePayload) {
        leaveRequestService.completeTask(tmsUpdatePayload);
        return ResponseEntity.ok("Task completed");
    }
}
