package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.config.kafka.dto.TMSUpdatePayload;
import com.flowable.flowable.dto.TaskDTO;
import com.flowable.flowable.models.ESSWorkFlow;
import com.flowable.flowable.models.TMSWorkFlow;
import com.flowable.flowable.repo.ESSWorkFlowRepo;
import com.flowable.flowable.repo.TMSWorkFlowRepo;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LeaveRequestServiceImpl {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    private final TMSWorkFlowRepo tmsWorkFlowRepo;

    private final ESSWorkFlowRepo essWorkFlowRepo;

    private final KafkaTemplate<String, TMSUpdatePayload> kafkaTemplate;

    @Autowired
    public LeaveRequestServiceImpl(RuntimeService runtimeService, TaskService taskService, TMSWorkFlowRepo tmsWorkFlowRepo, ESSWorkFlowRepo essWorkFlowRepo, KafkaTemplate<String, TMSUpdatePayload> kafkaTemplate) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.tmsWorkFlowRepo = tmsWorkFlowRepo;
        this.essWorkFlowRepo = essWorkFlowRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * @description this method is used to start or initiate a process instance.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    @KafkaListener(topics = "tms-start-update", containerFactory = "KafkaListenerContainerFactory", groupId = "tms-group")
    public void startLeaveProcess(TMSUpdatePayload tmsUpdatePayload) {

        Map<String, Object> variables = processVariables(tmsUpdatePayload);

        /** check if task is active given the leave id */

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals(tmsUpdatePayload.getLeaveId())
                .singleResult();
        if (task != null){
            throw new ResponseStatusException(HttpStatusCode.valueOf(400),"An active task alredy in use with the given id:->>>>{}");
        }

        /** check application type */

        if (tmsUpdatePayload.getApplicationType().equalsIgnoreCase("TMS")){

            log.info("About to start approval workflow for TMS:->>>>");

            List<TMSWorkFlow> workFlows = tmsWorkFlowRepo.findAll();

            List<String> sortedWorkflows = workFlows.stream()
                    .sorted(Comparator.comparing(TMSWorkFlow::getPriority))
                    .map(TMSWorkFlow::getName)
                    .collect(Collectors.toList()); // mutable list

            if (!sortedWorkflows.isEmpty()) {
                sortedWorkflows.remove(sortedWorkflows.size() - 1);
            }

            variables.put("approverList", sortedWorkflows);


        } else if (variables.get("applicationType").equals("ESS")) {

            log.info("About to start approval workflow for ESS:->>>>");

            List<ESSWorkFlow> workFlows = essWorkFlowRepo.findAll();

            List<String> sortedWorkflows = workFlows.stream()
                    .sorted(Comparator.comparing(ESSWorkFlow::getPriority))
                    .map(ESSWorkFlow::getName)
                    .toList();

            variables.put("approverList", sortedWorkflows);
        }

        runtimeService.startProcessInstanceByKey("leaveProcess", variables);

        kafkaTemplate.send("tms-update", tmsUpdatePayload);
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
    @KafkaListener(topics = "tms-update", containerFactory="KafkaListenerContainerFactory", groupId = "tms-group")
    public void completeTask(TMSUpdatePayload tmsUpdatePayload) {

        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveId", tmsUpdatePayload.getLeaveId());
        variables.put("approveleaverequest", tmsUpdatePayload.getApproveleaverequest());

        log.info("About to complete task for {}", variables.get("leaveId"));

        /** retrieve a task given the leaveId **/

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals(tmsUpdatePayload.getLeaveId())
                .singleResult();

      if (task != null){
          if (task.getName().equalsIgnoreCase("Approval Task")){
              log.info("In task approval:->>>>>");
              if (variables.isEmpty()){
                  throw new ResponseStatusException(HttpStatusCode.valueOf(400), "variables cannot be null for task approval");
              }


             if (tmsUpdatePayload.getApproveleaverequest().equalsIgnoreCase("Yes")){

                 List<TMSWorkFlow> workFlows = tmsWorkFlowRepo.findAll();

                 List<String> sortedWorkflows = workFlows.stream()
                         .sorted(Comparator.comparing(TMSWorkFlow::getPriority))
                         .map(TMSWorkFlow::getName)
                         .toList();

                 String updatedStatus = "";
                 for (int i=0; i<sortedWorkflows.size(); i++){
                     if (sortedWorkflows.get(i).equalsIgnoreCase(task.getAssignee()) ){
                         updatedStatus = sortedWorkflows.get(i+1);
                     }
                 }

                 log.info("Updated status:->>>>{}", updatedStatus);

                 TMSUpdatePayload tmsUpdatePayloadBuilder = TMSUpdatePayload
                         .builder()
                         .status("PENDING_"+updatedStatus.toUpperCase()+"_APPROVAL")
                         .leaveId(tmsUpdatePayload.getLeaveId())
                         .build();

                 kafkaTemplate.send("flowable-update", tmsUpdatePayloadBuilder);

             } else if (tmsUpdatePayload.getApproveleaverequest().equalsIgnoreCase("No")) {

                 TMSUpdatePayload tmsUpdatePayloadBuilder = TMSUpdatePayload
                         .builder()
                         .status("REJECTED")
                         .leaveId(tmsUpdatePayload.getLeaveId())
                         .build();

                 kafkaTemplate.send("flowable-update", tmsUpdatePayloadBuilder);

             }

              taskService.complete(task.getId(), variables);

          }else {
              log.info("In submit leave request:->>>>>");
              taskService.complete(task.getId());
          }
      }else {
          throw new ResponseStatusException(HttpStatusCode.valueOf(404), "task cannot be found");
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
}
