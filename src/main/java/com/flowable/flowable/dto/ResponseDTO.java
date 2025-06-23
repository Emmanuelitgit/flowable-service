package com.flowable.flowable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private int statusCode;
    private String message;
    private Object data;
    private ZonedDateTime date;
}