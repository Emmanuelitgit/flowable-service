package com.flowable.flowable.util;
import com.flowable.flowable.dto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;


import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;

@Slf4j
@Component
public class AppUtils {

    /**
     * This method is used to handle all responses in the application.
     * @param message
     * @param status
     * @return responseDto object
     * @auther Emmanuel Yidana
     * @createdAt 16h April 2025
     */
    public static ResponseDTO getResponseDto(String message, HttpStatus status){
        ResponseDTO responseDto = new ResponseDTO();
        responseDto.setMessage(message);
        responseDto.setDate(ZonedDateTime.now());
        responseDto.setStatusCode(status.value());
        return responseDto;
    }

    /**
     * This method is used to handle all responses in the application.
     * @param message
     * @param status
     * @param data
     * @return responseDto object
     * @auther Emmanuel Yidana
     * @createdAt 16h April 2025
     */
    public static ResponseDTO getResponseDto(String message, HttpStatus status, Object data){
        if(data==null){
            ResponseDTO responseDto = getResponseDto(message, status);
            return responseDto;
        }
        ResponseDTO responseDto = new ResponseDTO();
        responseDto.setMessage(message);
        responseDto.setDate(ZonedDateTime.now());
        responseDto.setStatusCode(status.value());
        responseDto.setData(data);
        return responseDto;
    }


    /**
     * This method is used to get user full name.
     * @param first
     * @param last
     * @return responseDto object
     * @auther Emmanuel Yidana
     * @createdAt 16h April 2025
     */
    public static String getFullName(String first, String last){
        return first + " " + " " + last;
    }


    public static final ExampleMatcher SEARCH_CONDITION_MATCH_ALL = ExampleMatcher.matchingAll()
            .withMatcher("price", exact())
            .withIgnorePaths("id", "createdBy", "updatedBy", "createdAt", "updatedAt")
            .withMatcher("name", contains().ignoreCase());

    public static final ExampleMatcher SEARCH_CONDITION_MATCH_ANY = ExampleMatcher.matchingAny()
            .withMatcher("price", exact())
            .withIgnorePaths("id", "createdBy", "updatedBy", "createdAt", "updatedAt")
            .withMatcher("name", contains().ignoreCase());
}
