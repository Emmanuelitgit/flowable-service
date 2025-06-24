package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.exception.AlreadyExistException;
import com.flowable.flowable.exception.NotFoundException;
import com.flowable.flowable.models.ApplicationType;
import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.repo.ApplicationTypeRepo;
import com.flowable.flowable.repo.WorkFlowRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class WorkFlowServiceImpl {

    private final WorkFlowRepo workFlowRepo;

    private final ApplicationTypeRepo applicationTypeRepo;

    @Autowired
    public WorkFlowServiceImpl(WorkFlowRepo workFlowRepo, ApplicationTypeRepo applicationTypeRepo) {
        this.workFlowRepo = workFlowRepo;
        this.applicationTypeRepo = applicationTypeRepo;
    }


    /**
     * @description this method is used to fetch all workflows form the db.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the workflow response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> findAll(){

        log.info("In fetch all workflows method:->>");

        return new ResponseEntity<>(workFlowRepo.findAll(), HttpStatusCode.valueOf(200));
    }

    /**
     * @description this method is used to save a new workflow record.
     * @Auther Emmanuel Yidana
     * @param workFlows
     * @return returns ResponseEntity containing the workflow response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> saveSetup(List<WorkFlow> workFlows){

        log.info("In save workflow method:->>");

        List<WorkFlow> flows = new ArrayList<>();

        workFlows.forEach((flow)->{

            log.info("flow:->>{}", flow);

            // check if workflow name already exist
            Optional<WorkFlow> isFlowExist = workFlowRepo.findByName(flow.getName());

            if (isFlowExist.isPresent()){
                throw new AlreadyExistException("work flow name already exist:"+flow.getName());
            }

            // check application availability
            applicationTypeRepo.findById(flow.getApplicationId())
                    .orElseThrow(()-> new NotFoundException("application given id cannot be found:"+flow.getApplicationId()));

             WorkFlow res = workFlowRepo.save(flow);
             flows.add(res);
        });


        return new ResponseEntity<>(flows, HttpStatusCode.valueOf(201));
    }


    /**
     * @description this method is used to update a workflow record given the id and the payload.
     * @Auther Emmanuel Yidana
     * @param workFlow
     * @return returns ResponseEntity containing the workflow response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> updateSetup(WorkFlow workFlow){

        log.info("In update workflow method:->>");

        WorkFlow existingData = workFlowRepo.findById(workFlow.getId())
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        existingData.setName(workFlow.getName());
        existingData.setPriority(workFlow.getPriority());

        WorkFlow res = workFlowRepo.save(existingData);

        return new ResponseEntity<>(res, HttpStatusCode.valueOf(200));
    }


    /**
     * @description this method is used to remove a workflow record from the db.
     * @Auther Emmanuel Yidana
     * @param id
     * @return returns ResponseEntity containing the workflow response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> removeSetup(UUID id){

        log.info("In remove workflow method:->>");

        WorkFlow existingData = workFlowRepo.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));

        workFlowRepo.deleteById(existingData.getId());

        return new ResponseEntity<>("setup deleted", HttpStatusCode.valueOf(200));
    }
}
