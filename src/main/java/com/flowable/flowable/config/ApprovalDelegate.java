package com.flowable.flowable.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("approvalDelegate")
public class ApprovalDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("About to approve or reject request:->>>>>");
        String approver = (String) execution.getVariable("approver");
        String decision = (String) execution.getVariable("approveleaverequest");

        System.out.println(approver + " decided: " + decision);

        if ("No".equalsIgnoreCase(decision)) {
            execution.setVariable("rejected", true);
        }
    }
}