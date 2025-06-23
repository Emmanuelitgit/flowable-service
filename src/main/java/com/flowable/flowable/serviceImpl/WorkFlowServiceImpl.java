package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.repo.WorkFlowRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class WorkFlowServiceImpl {

    private final WorkFlowRepo workFlowRepo;

    @Autowired
    public WorkFlowServiceImpl(WorkFlowRepo workFlowRepo) {
        this.workFlowRepo = workFlowRepo;
    }

    public ResponseEntity<Object> findAll(){
        return new ResponseEntity<>(workFlowRepo.findAll(), HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Object> saveSetup(WorkFlow workFlow){
        WorkFlow flow = workFlowRepo.save(workFlow);
        return new ResponseEntity<>(flow, HttpStatusCode.valueOf(201));
    }

    public ResponseEntity<Object> updateSetup(WorkFlow workFlow){

        WorkFlow existingData = workFlowRepo.findById(workFlow.getId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        existingData.setName(workFlow.getName());
        existingData.setPriority(workFlow.getPriority());

        WorkFlow res = workFlowRepo.save(existingData);

        return new ResponseEntity<>(res, HttpStatusCode.valueOf(200));
    }

    public ResponseEntity<Object> removeSetup(UUID id){
        WorkFlow existingData = workFlowRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        workFlowRepo.deleteById(existingData.getId());

        return new ResponseEntity<>("setup deleted", HttpStatusCode.valueOf(200));
    }
}
