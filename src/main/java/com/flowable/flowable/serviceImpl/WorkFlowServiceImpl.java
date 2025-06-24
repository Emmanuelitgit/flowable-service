package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.dto.WorkFlowDTO;
import com.flowable.flowable.exception.AlreadyExistException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public ResponseEntity<Object> saveSetup(List<WorkFlowDTO> workFlows){

        log.info("In save workflow method:->>");

        List<WorkFlow> flows = new ArrayList<>();

        workFlows.forEach((flow)->{

            log.info("flow:->>{}", flow);

            // check if workflow name already exist


            WorkFlow workFlow = WorkFlow
                    .builder()
                    .name(flow.getFlow().getName())
                    .applicationId(flow.getFlow().getApplicationId())
                    .priority(flow.getFlow().getPriority())
                    .build();

            Optional<WorkFlow> isFlowExist = workFlowRepo.findByName(workFlow.getName());

            if (isFlowExist.isPresent()){
                throw new AlreadyExistException("work flow name already exist:"+workFlow.getName());
            }

            // check application availability
            applicationTypeRepo.findById(workFlow.getApplicationId())
                    .orElseThrow(()-> new NotFoundException("application given id cannot be found:"+workFlow.getApplicationId()));

             WorkFlow res = workFlowRepo.save(workFlow);

             // saving complete status record
            CompleteStatus completeStatus = CompleteStatus
                    .builder()
                    .onRejection(flow.getCompleteStatus().getOnRejection())
                    .onSuccess(flow.getCompleteStatus().getOnSuccess())
                    .applicationId(res.getApplicationId())
                    .build();

            CompleteStatus isStatusExist = completeStatusRepo.findByApplicationId(flow.getFlow().getApplicationId());
             if (isStatusExist ==null){
                 completeStatusRepo.save(completeStatus);
             }

             flows.add(res);
        });


        return new ResponseEntity<>(flows, HttpStatusCode.valueOf(201));
    }


    /**
     * @description this method is used to update a workflow record given the id and the payload.
     * @Auther Emmanuel Yidana
     * @param workFlows
     * @return returns ResponseEntity containing the workflow response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> updateSetup(List<WorkFlowDTO> workFlows){

        log.info("In update workflow method:->>");


        List<WorkFlow> flows = new ArrayList<>();

        workFlows.forEach((flow)->{

            // check if workflow exist
            WorkFlow existingData = workFlowRepo.findById(flow.getId())
                    .orElseThrow(()-> new ResponseStatusException(HttpStatusCode.valueOf(404),"setup record cannot found"));


            // check application availability
            applicationTypeRepo.findById(flow.getFlow().getApplicationId())
                    .orElseThrow(()-> new NotFoundException("application given id cannot be found:"+flow.getFlow().getApplicationId()));

            existingData.setName(flow.getFlow().getName());
            existingData.setPriority(flow.getFlow().getPriority());

            WorkFlow response = workFlowRepo.save(existingData);

            // saving complete status record
            CompleteStatus completeStatus = CompleteStatus
                    .builder()
                    .onRejection(flow.getCompleteStatus().getOnRejection())
                    .onSuccess(flow.getCompleteStatus().getOnSuccess())
                    .applicationId(response.getApplicationId())
                    .build();

            CompleteStatus isStatusExist = completeStatusRepo.findByApplicationId(flow.getFlow().getApplicationId());
            if (isStatusExist ==null){
                completeStatusRepo.save(completeStatus);
            }

            flows.add(response);
        });


        return new ResponseEntity<>(flows, HttpStatusCode.valueOf(200));
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
