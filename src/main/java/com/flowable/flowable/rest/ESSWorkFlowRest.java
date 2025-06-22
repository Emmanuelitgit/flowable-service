package com.flowable.flowable.rest;

import com.flowable.flowable.models.ESSWorkFlow;
import com.flowable.flowable.models.TMSWorkFlow;
import com.flowable.flowable.serviceImpl.ESSWorkFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/work-flow/ess")
public class ESSWorkFlowRest {

    private final ESSWorkFlowServiceImpl essWorkFlowService;

    @Autowired
    public ESSWorkFlowRest(ESSWorkFlowServiceImpl essWorkFlowService) {
        this.essWorkFlowService = essWorkFlowService;
    }


    @GetMapping
    private ResponseEntity<Object> findAll(){
        return essWorkFlowService.findAll();
    }

    @PostMapping
    private ResponseEntity<Object> saveSetup(@RequestBody ESSWorkFlow tmsWorkFlow){
        return essWorkFlowService.saveSetup(tmsWorkFlow);
    }

    @PutMapping
    private ResponseEntity<Object> updateSetup(@RequestBody ESSWorkFlow tmsWorkFlow){
        return essWorkFlowService.updateSetup(tmsWorkFlow);
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Object> removeSetup(@PathVariable UUID id){
        return essWorkFlowService.removeSetup(id);
    }
}
