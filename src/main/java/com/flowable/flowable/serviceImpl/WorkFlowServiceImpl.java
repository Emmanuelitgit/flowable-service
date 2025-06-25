package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.dto.WorkFlowDTO;
import com.flowable.flowable.exception.AlreadyExistException;
import com.flowable.flowable.exception.BadRequestException;
import com.flowable.flowable.exception.NotFoundException;
import com.flowable.flowable.models.ApplicationType;
import com.flowable.flowable.models.CompleteStatus;
import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.repo.ApplicationTypeRepo;
import com.flowable.flowable.repo.CompleteStatusRepo;
import com.flowable.flowable.repo.WorkFlowRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class WorkFlowServiceImpl {

    private final WorkFlowRepo workFlowRepo;

    private final ApplicationTypeRepo applicationTypeRepo;

    private final CompleteStatusRepo completeStatusRepo;

    @Autowired
    public WorkFlowServiceImpl(WorkFlowRepo workFlowRepo, ApplicationTypeRepo applicationTypeRepo, CompleteStatusRepo completeStatusRepo) {
        this.workFlowRepo = workFlowRepo;
        this.applicationTypeRepo = applicationTypeRepo;
        this.completeStatusRepo = completeStatusRepo;
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
    public ResponseEntity<Object> saveSetup(WorkFlowDTO workFlows){

        log.info("In save workflow method:->>");

        List<WorkFlow> flows = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();

        workFlows.getFlow().forEach((flow)->{

            log.info("flow:->>{}", flow);

            // building payload
            WorkFlow workFlow = WorkFlow
                    .builder()
                    .name(flow.getName())
                    .applicationId(workFlows.getApplicationId())
                    .priority(flow.getPriority())
                    .build();

            // check if workflow name already exist
            Optional<WorkFlow> isFlowExist = workFlowRepo.findByName(workFlow.getName());

            if (isFlowExist.isPresent()){
                throw new AlreadyExistException("work flow name already exist:"+workFlow.getName());
            }

            // check application availability
            applicationTypeRepo.findById(workFlows.getApplicationId())
                    .orElseThrow(()-> new NotFoundException("application given id cannot be found:"+workFlows.getApplicationId()));

             WorkFlow res = workFlowRepo.save(workFlow);

             flows.add(res);
        });


        // saving complete status record
        CompleteStatus completeStatus = CompleteStatus
                .builder()
                .onRejection(workFlows.getCompleteStatus().getOnRejection())
                .onSuccess(workFlows.getCompleteStatus().getOnSuccess())
                .applicationId(workFlows.getApplicationId())
                .build();

        CompleteStatus isStatusExist = completeStatusRepo.findByApplicationId(workFlows.getApplicationId());

        Object completeStatusRes = null;

        if (isStatusExist ==null){
             completeStatusRes = completeStatusRepo.save(completeStatus);
        }else {
            completeStatusRes = isStatusExist;
        }

        responseMap.put("flows", flows);
        responseMap.put("complete status", completeStatusRes);

        return new ResponseEntity<>(responseMap, HttpStatusCode.valueOf(201));
    }


    /**
     * @description this method is used to update a workflow record given the id and the payload.
     * @Auther Emmanuel Yidana
     * @param workFlows
     * @return returns ResponseEntity containing the workflow response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> updateSetup(WorkFlowDTO workFlows){

        log.info("In update workflow method:->>");

        List<WorkFlow> flows = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();

        AtomicReference<String> applicationType = new AtomicReference<>("");

        workFlows.getFlow().forEach((flow)->{

            log.info("About to update workflow record->>>>");

            // check if work flow exist
            WorkFlow existingFlow = workFlowRepo.findById(flow.getId())
                    .orElseThrow(()-> new NotFoundException("work flow record cannot be found"));

            // check application availability
            ApplicationType application = applicationTypeRepo.findById(workFlows.getApplicationId())
                    .orElseThrow(()-> new NotFoundException("application given id cannot be found:"+workFlows.getApplicationId()));

            // getting the application type
            applicationType.set(application.getName());

            // saving updated flow records
            existingFlow.setName(flow.getName());
            existingFlow.setPriority(flow.getPriority());
            existingFlow.setApplicationId(workFlows.getApplicationId());

            WorkFlow flowResponse = workFlowRepo.save(existingFlow);


            flows.add(flowResponse);
        });


        // check if complete status exist by application id
        CompleteStatus existingStatus = completeStatusRepo.findByApplicationId(workFlows.getApplicationId());

        if (existingStatus == null){
            throw new BadRequestException("cannot find existing complete status for:"+applicationType);
        }

        // saving updated complete status
        existingStatus.setOnRejection(workFlows.getCompleteStatus().getOnRejection());
        existingStatus.setOnSuccess(workFlows.getCompleteStatus().getOnSuccess());
        CompleteStatus completeStatusRes = completeStatusRepo.save(existingStatus);

        responseMap.put("flows", flows);
        responseMap.put("complete status",completeStatusRes);

        return new ResponseEntity<>(responseMap, HttpStatusCode.valueOf(200));
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
