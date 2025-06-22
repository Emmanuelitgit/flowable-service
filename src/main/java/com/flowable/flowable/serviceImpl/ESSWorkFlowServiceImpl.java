package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.models.ESSWorkFlow;
import com.flowable.flowable.models.TMSWorkFlow;
import com.flowable.flowable.repo.ESSWorkFlowRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ESSWorkFlowServiceImpl {

    private final ESSWorkFlowRepo essWorkFlowRepo;

    @Autowired
    public ESSWorkFlowServiceImpl(ESSWorkFlowRepo essWorkFlowRepo) {
        this.essWorkFlowRepo = essWorkFlowRepo;
    }


    public ResponseEntity<Object> findAll(){
        return new ResponseEntity<>(essWorkFlowRepo.findAll(), HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Object> saveSetup(ESSWorkFlow tmsWorkFlow){
        ESSWorkFlow ess = essWorkFlowRepo.save(tmsWorkFlow);
        return new ResponseEntity<>(ess, HttpStatusCode.valueOf(201));
    }

    public ResponseEntity<Object> updateSetup(ESSWorkFlow tmsWorkFlow){

        ESSWorkFlow existingData = essWorkFlowRepo.findById(tmsWorkFlow.getId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        existingData.setName(tmsWorkFlow.getName());
        existingData.setPriority(tmsWorkFlow.getPriority());

        ESSWorkFlow res = essWorkFlowRepo.save(existingData);

        return new ResponseEntity<>(res, HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Object> removeSetup(UUID id){
        ESSWorkFlow existingData =essWorkFlowRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        essWorkFlowRepo.deleteById(existingData.getId());

        return new ResponseEntity<>("setup deleted", HttpStatusCode.valueOf(200));
    }
}
