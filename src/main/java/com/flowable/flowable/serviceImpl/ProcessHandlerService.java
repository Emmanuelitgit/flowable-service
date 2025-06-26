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

        List<String> sortedWorkflows = workFlows.stream()
                .sorted(Comparator.comparing(WorkFlow::getPriority))
                .map(WorkFlow::getName)
                .collect(Collectors.toList());

       /* if (!sortedWorkflows.isEmpty()) {
            sortedWorkflows.remove(sortedWorkflows.size() - 1);
        }*/

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


        if (task.getName().equalsIgnoreCase("Approval Task")){
            log.info("In task approval:->>>>>");
            if (variables.isEmpty()){
                throw new BadRequestException("variables cannot be null for task approval");
            }


            if (updatePayload.getApproveleaverequest().equalsIgnoreCase("Yes")){

                ApplicationType appType = applicationTypeRepo.findByName(applicationType.toUpperCase());
                List<WorkFlow> workFlows = workFlowRepo.findByApplicationIdOrderByPriorityAsc(appType.getId());


                String updatedStatus = null;

                // the loop is used to get the nest status
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
        }

    }

/**
 While this may look good in the face security, it is also a big trade of for performance.
 depending on the load of the isUserAuthorize method, there might be possible delay in response.
 my advocate has always been to make the entry point to your system or resouces once but very strong.
 With the wide array of security tools and frameworks available today, a system can be both highly secure and resilient if these are properly enforced.
 That’s just my take on the meme, though
 */
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

        return variables;
    }

    public CompleteStatus getCompeteStatus(UUID id){
        return completeStatusRepo.findByApplicationId(id);
    }
}
