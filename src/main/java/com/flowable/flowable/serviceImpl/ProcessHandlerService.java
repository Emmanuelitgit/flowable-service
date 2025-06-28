package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.config.kafka.dto.UpdatePayload;
import com.flowable.flowable.dto.ResponseDTO;
import com.flowable.flowable.dto.TaskDTO;
import com.flowable.flowable.exception.BadRequestException;
import com.flowable.flowable.exception.NotFoundException;
import com.flowable.flowable.models.ApplicationType;
import com.flowable.flowable.models.CompleteStatus;
import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.repo.ApplicationTypeRepo;
import com.flowable.flowable.repo.CompleteStatusRepo;
import com.flowable.flowable.repo.WorkFlowRepo;
import com.flowable.flowable.util.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessHandlerService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final WorkFlowRepo workFlowRepo;

    private final ApplicationTypeRepo applicationTypeRepo;

    private final KafkaTemplate<String, UpdatePayload> kafkaTemplate;

    private final CompleteStatusRepo completeStatusRepo;

    @Autowired
    public ProcessHandlerService(RuntimeService runtimeService, TaskService taskService, WorkFlowRepo workFlowRepo, ApplicationTypeRepo applicationTypeRepo, KafkaTemplate<String, UpdatePayload> kafkaTemplate, CompleteStatusRepo completeStatusRepo) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.workFlowRepo = workFlowRepo;
        this.applicationTypeRepo = applicationTypeRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.completeStatusRepo = completeStatusRepo;
    }

    /**
     * @description this method is used to start or initiate a process instance.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    @KafkaListener(topics = "start-process-update", containerFactory = "KafkaListenerContainerFactory", groupId = "tms-group")
    public void startLeaveProcess(UpdatePayload updatePayload) {

        Map<String, Object> variables = processVariables(updatePayload);

        /** check if task is active given the leave id */

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals(updatePayload.getLeaveId())
                .singleResult();

        if (task != null){
            throw new BadRequestException("An active task already in use with the given id:"+ updatePayload.getLeaveId());
        }

        /** check application type */

        log.info("About to start approval workflow for {}:->>>>", updatePayload.getApplicationType());

       // get application type details
        ApplicationType applicationType = applicationTypeRepo
                .findByName(updatePayload.getApplicationType());

        // load the approval flow from the db sorted in ascending order by the priority
        List<WorkFlow> workFlows = workFlowRepo
                .findByApplicationIdAndRequestTypeOrderByPriorityAsc(applicationType.getId(), updatePayload.getRequestType().toUpperCase());

        // filter to get initiator role priority
        int initiatorPriority = workFlows.stream()
                .filter(wf -> wf.getName().equalsIgnoreCase(updatePayload.getInitiatorRole()))
                .map(WorkFlow::getPriority)
                .findFirst()
                .orElse(0);

        log.info("initiator role:->>>{}", updatePayload.getInitiatorRole());

        // sorting flows in ascending order base on priority.
        // this get only flows which priorities are greater than the initiator priority
        List<String> sortedWorkflows = workFlows.stream()
                .filter(wf -> wf.getPriority() > initiatorPriority)
                .sorted(Comparator.comparing(WorkFlow::getPriority))
                .map(WorkFlow::getName)
                .collect(Collectors.toList());


        log.info("work flows:->>>>{}", sortedWorkflows);
        log.info("initiatiro priortity:->>{}", initiatorPriority);
        log.info("priorities:->>>{}", workFlows.size()==initiatorPriority);

        // if the requester is part of the approver and he/she is the last person on the approvers list
        if (workFlows.size()==initiatorPriority){
            updatePayload.setLastApprover(true);
            sortedWorkflows.add(updatePayload.getInitiatorRole());
        }

        log.info("updated work flow:->>>{}", sortedWorkflows);
        variables.put("approverList", sortedWorkflows);

        runtimeService.startProcessInstanceByKey("leaveProcess", variables);

        kafkaTemplate.send( "complete-task-update", updatePayload);
    }

    /**
     * @description this method is used to fetch all process tasks from the flowable db.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public ResponseEntity<ResponseDTO> findAll(){

        log.info("About to fetch all tasks:->>>>");

        List<Task> tasks = taskService.createTaskQuery().list();

        List<TaskDTO> dtos = tasks.stream()
                .map(TaskDTO::new)
                .toList();

        ResponseDTO responseDTO = AppUtils.getResponseDto("tasks details", HttpStatus.OK, dtos);

        return new ResponseEntity<>(responseDTO, HttpStatusCode.valueOf(200));
    }


    /**
     * @description this method is used to get user specific task given the userID thus the username. eg manager, employee.
     * @Auther Emmanuel Yidana
     * @param username
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public ResponseEntity<ResponseDTO> getTasksForUser(String username) {

        log.info("About to fetch tasks for {}:->>>>", username);

        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(username).list();

        List<TaskDTO> dtos = tasks.stream()
                .map(TaskDTO::new)
                .toList();

        ResponseDTO responseDTO = AppUtils.getResponseDto("User task details", HttpStatus.OK, dtos);

        return new ResponseEntity<>(responseDTO, HttpStatusCode.valueOf(200));
    }

    /**
     * @description this method is used to complete a task by a user given the task id and some metadata thus the variables.
     * @Auther Emmanuel Yidana
     * @param updatePayload
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    @KafkaListener(topics = "complete-task-update", containerFactory="KafkaListenerContainerFactory", groupId = "tms-group")
    public void completeTask(UpdatePayload updatePayload) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveId", updatePayload.getLeaveId());
        variables.put("approveleaverequest", updatePayload.getApproveleaverequest());

        log.info("About to complete task for {}", variables.get("leaveId"));

        /** retrieve a task given the leaveId **/

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals(updatePayload.getLeaveId())
                .singleResult();

        if (task == null){
            throw new NotFoundException("no active task found for:->>"+ updatePayload.getLeaveId());
        }
        // Fetch process instance ID from the task
        String processInstanceId = task.getProcessInstanceId();

        // Get the applicationType variable from the process instance
        String applicationType = (String) runtimeService
                .getVariable(processInstanceId, "applicationType")
                .toString()
                .toLowerCase();

        log.info("Application Type from process variables: {}", applicationType);

        log.info("task:->>{}", task);

        if (task.getName().equalsIgnoreCase("Approval Task")){
            log.info("In task approval:->>>>>");
            if (variables.isEmpty()){
                throw new BadRequestException("variables cannot be null for task approval");
            }


            if (updatePayload.getApproveleaverequest().equalsIgnoreCase("Yes")){

                ApplicationType appType = applicationTypeRepo.findByName(applicationType.toUpperCase());

                String updatedStatus = getNextApprover(task.getAssignee(), appType.getId());

                log.info("Updated status:->>>>{}", updatedStatus);

                UpdatePayload updatePayloadBuilder = UpdatePayload
                        .builder()
                        .status(updatedStatus.toUpperCase())
                        .leaveId(updatePayload.getLeaveId())
                        .build();


                kafkaTemplate.send(applicationType+"-flowable-update", updatePayloadBuilder);

            } else if (updatePayload.getApproveleaverequest().equalsIgnoreCase("No")) {

                UUID applicationId = applicationTypeRepo.findByName(applicationType.toUpperCase()).getId();

                UpdatePayload updatePayloadBuilder = UpdatePayload
                        .builder()
                        .status(getCompeteStatus(applicationId).getOnRejection())
                        .leaveId(updatePayload.getLeaveId())
                        .build();

                kafkaTemplate.send(applicationType+"-flowable-update", updatePayloadBuilder);

            }

            taskService.complete(task.getId(), variables);

        }else {
            log.info("In submit leave request:->>>>>");
            taskService.complete(task.getId());

            // if the requester is last approver, then re-publish an update to update the status of the request to a complete status.
            if (updatePayload.isLastApprover()){
                updatePayload.setApproveleaverequest("Yes");
                kafkaTemplate.send("complete-task-update", updatePayload);
            }

        }

    }


    /**
     * @description a helper method use to handle role removal from an ongoing or completed process.
     * @Auther Emmanuel Yidana
     * @param removedRole
     * @return returns ResponseEntity containing the tasks response.
     * @Date 28/06/2025
     */
    public void handleRoleRemoval(String removedRole, String applicationType) {

        log.info("About to remove approver from approval flow:->>>{}", removedRole);

        List<Task> tasks = taskService.createTaskQuery().processVariableValueEquals(applicationType).list();


       // loop to complete tasks that are assigned to the removed role
        for (Task task : tasks) {

            String processInstanceId = task.getProcessInstanceId();

            List<String> approverList = (List<String>) runtimeService.getVariable(processInstanceId, "approverList");

            // Remove the removed role from the list
            if (!approverList.isEmpty()){
                approverList = approverList.stream()
                        .filter(approver -> !approver.equalsIgnoreCase(removedRole))
                        .collect(Collectors.toList());

                // update the approval flow list
                runtimeService.setVariable(processInstanceId, "approverList", approverList);
            }

            // Now check if the current task is assigned to the removed role
            String currentApprover = task.getAssignee();
            if (currentApprover != null && currentApprover.equalsIgnoreCase(removedRole)) {

                // Complete this task programmatically to move the flow forward
                Map<String, Object> vars = new HashMap<>();
                vars.put("approveleaverequest", "Yes");
                log.info("task:->>>{}", task);
                taskService.complete(task.getId(), vars);
                

                ApplicationType appType = applicationTypeRepo.findByName(applicationType.toUpperCase());

                // get next approver or updated status if completed
                String updatedStatus = getNextApprover(task.getAssignee(), appType.getId());

                // publish an update to update the request status of the respective application
                UpdatePayload updatePayload = UpdatePayload
                        .builder()
                        .ApplicationType(applicationType)
                        .leaveId((Long) runtimeService.getVariable(task.getProcessDefinitionId(), "leaveId"))
                        .status(updatedStatus)
                        .build();

                kafkaTemplate.send(applicationType+"-flowable-update", updatePayload);

                log.info("Auto-completed task for removed role {} in process {}", removedRole, task.getProcessInstanceId());
            }
        }
    }


    /**
     * @description a helper method use to get next approver.
     * @Auther Emmanuel Yidana
     * @param currentAssignee
     * @param applicationId
     * @return returns the next approver as string.
     * @Date 28/06/2025
     */
    public String getNextApprover(String currentAssignee, UUID applicationId){

        List<WorkFlow> workFlows = workFlowRepo.findByApplicationIdOrderByPriorityAsc(applicationId);

        String updatedStatus = null;

        // the loop is used to get the next status
        for (int i = 0; i < workFlows.size(); i++) {
            if (workFlows.get(i).getName().equalsIgnoreCase(currentAssignee)) {
                if (i + 1 < workFlows.size()) {
                    updatedStatus = workFlows.get(i + 1).getName();
                } else {
                    updatedStatus = workFlows.get(i).getApplicationId() != null
                            ? getCompeteStatus(workFlows.get(i).getApplicationId()).getOnSuccess()
                            : "COMPLETED";
                }
                break;
            }
        }

        return updatedStatus;
    }


    /**
     * @description this method is used to build the process variables objects
     * @Auther Emmanuel Yidana
     * @param updatePayload
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    private Map<String, Object> processVariables(UpdatePayload updatePayload){
        Map<String, Object> variables = new HashMap<>();
        variables.put("rejected", false);
        variables.put("leaveId", updatePayload.getLeaveId());
        variables.put("requestedBy", updatePayload.getRequestedBy());
        variables.put("status", updatePayload.getStatus());
        variables.put("approveleaverequest", null);
        variables.put("applicationType", updatePayload.getApplicationType());
        variables.put("requestType", updatePayload.getRequestType());
        variables.put("initiatorRole", updatePayload.getInitiatorRole());

        return variables;
    }

    // a helper method to get the complete status for a flow
    public CompleteStatus getCompeteStatus(UUID id){
        return completeStatusRepo.findByApplicationId(id);
    }
}
