package com.flowable.flowable.rest;

import com.flowable.flowable.models.TMSWorkFlow;
import com.flowable.flowable.serviceImpl.TMSWorkFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/work-flow/tms")
public class TMSWorkFlowRest {

    private final TMSWorkFlowServiceImpl tmsWorkFlowService;

    @Autowired
    public TMSWorkFlowRest(TMSWorkFlowServiceImpl tmsWorkFlowService) {
        this.tmsWorkFlowService = tmsWorkFlowService;
    }

    @GetMapping
    private ResponseEntity<Object> findAll(){
        return tmsWorkFlowService.findAll();
    }

    @PostMapping
    private ResponseEntity<Object> saveSetup(@RequestBody TMSWorkFlow tmsWorkFlow){
        return tmsWorkFlowService.saveSetup(tmsWorkFlow);
    }

    @PutMapping
    private ResponseEntity<Object> updateSetup(@RequestBody TMSWorkFlow tmsWorkFlow){
        return tmsWorkFlowService.updateSetup(tmsWorkFlow);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Object> removeSetup(@PathVariable UUID id){
        return tmsWorkFlowService.removeSetup(id);
    }
}
