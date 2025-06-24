package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.config.kafka.dto.TMSUpdatePayload;
import com.flowable.flowable.dto.TaskDTO;
import com.flowable.flowable.exception.BadRequestException;
import com.flowable.flowable.exception.NotFoundException;
import com.flowable.flowable.models.ApplicationType;
import com.flowable.flowable.models.CompleteStatus;
import com.flowable.flowable.models.WorkFlow;
import com.flowable.flowable.repo.ApplicationTypeRepo;
import com.flowable.flowable.repo.CompleteStatusRepo;
import com.flowable.flowable.repo.WorkFlowRepo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProcessHandlerService {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final WorkFlowRepo workFlowRepo;

    private final ApplicationTypeRepo applicationTypeRepo;

    private final KafkaTemplate<String, TMSUpdatePayload> kafkaTemplate;

    private final CompleteStatusRepo completeStatusRepo;

    @Autowired
    public ProcessHandlerService(RuntimeService runtimeService, TaskService taskService, WorkFlowRepo workFlowRepo, ApplicationTypeRepo applicationTypeRepo, KafkaTemplate<String, TMSUpdatePayload> kafkaTemplate, CompleteStatusRepo completeStatusRepo) {
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
    public void startLeaveProcess(TMSUpdatePayload tmsUpdatePayload) {

        Map<String, Object> variables = processVariables(tmsUpdatePayload);

        /** check if task is active given the leave id */

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals(tmsUpdatePayload.getLeaveId())
                .singleResult();

        if (task != null){
            throw new BadRequestException("An active task already in use with the given id:"+tmsUpdatePayload.getLeaveId());
        }

        /** check application type */

        log.info("About to start approval workflow for {}:->>>>", tmsUpdatePayload.getApplicationType());

        // load the approval flow from the db sorted in ascending order by the priority
        ApplicationType applicationType = applicationTypeRepo.findByName(tmsUpdatePayload.getApplicationType());
        List<WorkFlow> workFlows = workFlowRepo.findByApplicationIdOrderByPriorityAsc(applicationType.getId());

        List<String> sortedWorkflows = workFlows.stream()
                .sorted(Comparator.comparing(WorkFlow::getPriority))
                .map(WorkFlow::getName)
                .collect(Collectors.toList());

//        if (!sortedWorkflows.isEmpty()) {
//            sortedWorkflows.remove(sortedWorkflows.size() - 1);
//        }

        variables.put("approverList", sortedWorkflows);

        runtimeService.startProcessInstanceByKey("leaveProcess", variables);

        kafkaTemplate.send( "complete-task-update", tmsUpdatePayload);
    }

    /**
     * @description this method is used to fetch all process tasks from the flowable db.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> findAll(){

        log.info("About to fetch all tasks:->>>>");

        List<Task> tasks = taskService.createTaskQuery().list();

        List<TaskDTO> dtos = tasks.stream()
                .map(TaskDTO::new)
                .toList();

        return new ResponseEntity<>(dtos, HttpStatusCode.valueOf(200));
    }


    /**
     * @description this method is used to get user specific task given the userID thus the username. eg manager, employee.
     * @Auther Emmanuel Yidana
     * @param username
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> getTasksForUser(String username) {

        log.info("About to fetch tasks for {}:->>>>", username);

        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(username).list();

        List<TaskDTO> dtos = tasks.stream()
                .map(TaskDTO::new)
                .toList();

        return new ResponseEntity<>(dtos, HttpStatusCode.valueOf(200));
    }

    /**
     * @description this method is used to complete a task by a user given the task id and some metadata thus the variables.
     * @Auther Emmanuel Yidana
     * @param tmsUpdatePayload
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    @KafkaListener(topics = "complete-task-update", containerFactory="KafkaListenerContainerFactory", groupId = "tms-group")
    public void completeTask(TMSUpdatePayload tmsUpdatePayload) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveId", tmsUpdatePayload.getLeaveId());
        variables.put("approveleaverequest", tmsUpdatePayload.getApproveleaverequest());

        log.info("About to complete task for {}", variables.get("leaveId"));

        /** retrieve a task given the leaveId **/

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals(tmsUpdatePayload.getLeaveId())
                .singleResult();

        if (task == null){
            throw new NotFoundException("no active task found for:->>"+tmsUpdatePayload.getLeaveId());
        }
        // Fetch process instance ID from the task
        String processInstanceId = task.getProcessInstanceId();

        // Get the applicationType variable from the process instance
        String applicationType = (String) runtimeService
                .getVariable(processInstanceId, "applicationType")
                .toString()
                .toLowerCase();


        log.info("Application Type from process variables: {}", applicationType);


        if (task.getName().equalsIgnoreCase("Approval Task")){
            log.info("In task approval:->>>>>");
            if (variables.isEmpty()){
                throw new BadRequestException("variables cannot be null for task approval");
            }


            if (tmsUpdatePayload.getApproveleaverequest().equalsIgnoreCase("Yes")){

                ApplicationType appType = applicationTypeRepo.findByName(applicationType.toUpperCase());
                List<WorkFlow> workFlows = workFlowRepo.findByApplicationIdOrderByPriorityAsc(appType.getId());


                String updatedStatus = null;

                // the loop is used to ge the nest status
                for (int i = 0; i < workFlows.size(); i++) {
                    if (workFlows.get(i).getName().equalsIgnoreCase(task.getAssignee())) {
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


                log.info("Updated status:->>>>{}", updatedStatus);

                TMSUpdatePayload tmsUpdatePayloadBuilder = TMSUpdatePayload
                        .builder()
                        .status(updatedStatus.toUpperCase())
                        .leaveId(tmsUpdatePayload.getLeaveId())
                        .build();


                kafkaTemplate.send(applicationType+"-flowable-update", tmsUpdatePayloadBuilder);

            } else if (tmsUpdatePayload.getApproveleaverequest().equalsIgnoreCase("No")) {

                UUID applicationId = applicationTypeRepo.findByName(applicationType.toUpperCase()).getId();

                TMSUpdatePayload tmsUpdatePayloadBuilder = TMSUpdatePayload
                        .builder()
                        .status(getCompeteStatus(applicationId).getOnRejection())
                        .leaveId(tmsUpdatePayload.getLeaveId())
                        .build();

                kafkaTemplate.send(applicationType+"-flowable-update", tmsUpdatePayloadBuilder);

            }

            taskService.complete(task.getId(), variables);

        }else {
            log.info("In submit leave request:->>>>>");
            taskService.complete(task.getId());
        }

    }


    /**
     * @description this method is used to build the process variables objects
     * @Auther Emmanuel Yidana
     * @param tmsUpdatePayload
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    private Map<String, Object> processVariables(TMSUpdatePayload tmsUpdatePayload){
        Map<String, Object> variables = new HashMap<>();
        variables.put("rejected", false);
        variables.put("leaveId", tmsUpdatePayload.getLeaveId());
        variables.put("requestedBy", tmsUpdatePayload.getRequestedBy());
        variables.put("status", tmsUpdatePayload.getStatus());
        variables.put("approveleaverequest", null);
        variables.put("applicationType", tmsUpdatePayload.getApplicationType());
        variables.put("requestType", tmsUpdatePayload.getRequestType());

        return variables;
    }

    public CompleteStatus getCompeteStatus(UUID id){
        return completeStatusRepo.findByApplicationId(id);
    }
}
