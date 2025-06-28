package com.flowable.flowable.rest;

import com.flowable.flowable.dto.UserDTO;
import com.flowable.flowable.serviceImpl.UserServiceImpl;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserRest {

    private final UserServiceImpl userService;

    @Autowired
    public UserRest(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody UserDTO request) {
        return userService.createUser(request);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(){
        return userService.findAll();
    }
}