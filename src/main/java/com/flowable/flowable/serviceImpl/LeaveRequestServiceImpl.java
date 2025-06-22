package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.dto.TaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LeaveRequestServiceImpl {

    private final RuntimeService runtimeService;

    private final TaskService taskService;

    @Autowired
    public LeaveRequestServiceImpl(RuntimeService runtimeService, TaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    /**
     * @description this method is used to start or initiate a process instance.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public void startLeaveProcess(Map<String, Object> vars) {
        vars.put("approverList", List.of("manager", "gm", "hr"));
        vars.put("rejected", false);
        vars.put("approveleaverequest", null);
        runtimeService.startProcessInstanceByKey("leaveProcess", vars);
    }

    /**
     * @description this method is used to fetch all process tasks from the flowable db.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public ResponseEntity<Object> findAll(){
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
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(username).list();

        List<TaskDTO> dtos = tasks.stream()
                .map(TaskDTO::new)
                .toList();

        return new ResponseEntity<>(dtos, HttpStatusCode.valueOf(200));
    }

    /**
     * @description this method is used to complete a task by a user given the task id and some metadata thus the variable.
     * @Auther Emmanuel Yidana
     * @param variables
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public void completeTask(Map<String, Object> variables) {

        /** retrieve a task given the leaveId **/

        Task task = taskService.createTaskQuery()
                .processVariableValueEquals("leaveId", variables.get("leaveId"))
                .singleResult();

      if (task != null){
          if (task.getName().equalsIgnoreCase("Approval Task")){
              log.info("In task approval:->>>>>");
              if (variables.isEmpty()){
                  throw new ResponseStatusException(HttpStatusCode.valueOf(400), "variables cannot be null for task approval");
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
}
