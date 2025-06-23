package com.flowable.flowable.rest;

import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.serviceImpl.WorkFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/work-flow/tms")
public class WorkFlowRest {

    private final WorkFlowServiceImpl tmsWorkFlowService;

    @Autowired
    public WorkFlowRest(WorkFlowServiceImpl tmsWorkFlowService) {
        this.tmsWorkFlowService = tmsWorkFlowService;
    }

    @GetMapping
    private ResponseEntity<Object> findAll(){
        return tmsWorkFlowService.findAll();
    }

    @PostMapping
    private ResponseEntity<Object> saveSetup(@RequestBody WorkFlow workFlow){
        return tmsWorkFlowService.saveSetup(workFlow);
    }

    @PutMapping
    private ResponseEntity<Object> updateSetup(@RequestBody WorkFlow workFlow){
        return tmsWorkFlowService.updateSetup(workFlow);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Object> removeSetup(@PathVariable UUID id){
        return tmsWorkFlowService.removeSetup(id);
    }
}
