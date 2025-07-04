package com.flowable.flowable.rest;

import com.flowable.flowable.dto.WorkFlowDTO;
import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.serviceImpl.WorkFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-flow")
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
    private ResponseEntity<Object> saveSetup(@RequestBody WorkFlowDTO workFlows){
        return tmsWorkFlowService.saveSetup(workFlows);
    }

    @PutMapping
    private ResponseEntity<Object> updateSetup(@RequestBody WorkFlowDTO workFlow){
        return tmsWorkFlowService.updateSetup(workFlow);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Object> removeSetup(@PathVariable UUID id){
        return tmsWorkFlowService.removeSetup(id);
    }
}
