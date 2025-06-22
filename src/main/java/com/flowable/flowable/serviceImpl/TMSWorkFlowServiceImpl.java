package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.models.TMSWorkFlow;
import com.flowable.flowable.repo.TMSWorkFlowRepo;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class TMSWorkFlowServiceImpl {

    private final TMSWorkFlowRepo tmsWorkFlowRepo;

    @Autowired
    public TMSWorkFlowServiceImpl(TMSWorkFlowRepo tmsWorkFlowRepo) {
        this.tmsWorkFlowRepo = tmsWorkFlowRepo;
    }

    public ResponseEntity<Object> findAll(){
        return new ResponseEntity<>(tmsWorkFlowRepo.findAll(), HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Object> saveSetup(TMSWorkFlow tmsWorkFlow){
        TMSWorkFlow tms = tmsWorkFlowRepo.save(tmsWorkFlow);
        return new ResponseEntity<>(tms, HttpStatusCode.valueOf(201));
    }

    public ResponseEntity<Object> updateSetup(TMSWorkFlow tmsWorkFlow){

        TMSWorkFlow existingData = tmsWorkFlowRepo.findById(tmsWorkFlow.getId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        existingData.setName(tmsWorkFlow.getName());
        existingData.setPriority(tmsWorkFlow.getPriority());

        TMSWorkFlow res = tmsWorkFlowRepo.save(existingData);

        return new ResponseEntity<>(res, HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Object> removeSetup(UUID id){
        TMSWorkFlow existingData = tmsWorkFlowRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        tmsWorkFlowRepo.deleteById(existingData.getId());

        return new ResponseEntity<>("setup deleted", HttpStatusCode.valueOf(200));
    }
}
