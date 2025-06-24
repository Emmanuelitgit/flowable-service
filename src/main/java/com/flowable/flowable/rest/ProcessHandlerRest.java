package com.flowable.flowable.rest;

import com.flowable.flowable.config.kafka.dto.TMSUpdatePayload;
import com.flowable.flowable.dto.ResponseDTO;
import com.flowable.flowable.serviceImpl.ProcessHandlerService;
import com.flowable.flowable.util.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProcessHandlerRest {

    @Autowired
    private ProcessHandlerService leaveRequestService;

    @PostMapping("/start")
    public ResponseEntity<?> startProcess(@RequestBody TMSUpdatePayload tmsUpdatePayload) {
        leaveRequestService.startLeaveProcess(tmsUpdatePayload);
        return ResponseEntity.ok("Leave process started");
    }

    @GetMapping("/tasks")
    public ResponseEntity<ResponseDTO> findAll(){
        return leaveRequestService.findAll();
    }

    @GetMapping("/tasks/{username}")
    public ResponseEntity<ResponseDTO> getUserTasks(@PathVariable String username) {
        return leaveRequestService.getTasksForUser(username);
    }

    @PostMapping("/tasks/complete")
    public ResponseEntity<ResponseDTO> completeTask(@RequestBody TMSUpdatePayload tmsUpdatePayload) {
        leaveRequestService.completeTask(tmsUpdatePayload);

        ResponseDTO responseDTO = AppUtils.getResponseDto("Task mcompleted successfully", HttpStatus.OK);

        return  new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
