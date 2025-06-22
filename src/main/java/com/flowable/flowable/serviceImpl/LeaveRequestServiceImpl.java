package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.dto.TaskDTO;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @description this method is used to start or initiate a process.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public void startLeaveProcess() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("approverList", List.of("manager", "hr"));
        vars.put("rejected", false);
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
     * @param taskId
     * @param variables
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }
}
