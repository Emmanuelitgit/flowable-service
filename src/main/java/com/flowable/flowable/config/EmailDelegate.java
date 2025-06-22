package com.flowable.flowable.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("emailDelegate")
public class EmailDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("About to send email:->>>>");
        Boolean rejected = (Boolean) execution.getVariable("rejected");

        if (Boolean.TRUE.equals(rejected)) {
            // Send rejection email
        } else {
            // Send approval email
        }
    }
}