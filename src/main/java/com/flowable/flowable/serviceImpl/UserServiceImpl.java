package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class UserServiceImpl {

    private final IdentityService identityService;

    @Autowired
    public UserServiceImpl(IdentityService identityService) {
        this.identityService = identityService;
    }

    public ResponseEntity<String> createUser(UserDTO request) {
        if (identityService.createUserQuery().userId(request.getUserId()).singleResult() == null) {
            User user = identityService.newUser(request.getUserId());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            identityService.saveUser(user);

            return ResponseEntity.ok("User created");
        } else {
            return ResponseEntity.badRequest().body("User already exists");
        }
    }


    public ResponseEntity<Object> findAll(){
        log.info("About fetch all users from flowable db:->>>>>");
        List<User> users = identityService
                .createUserQuery()
                .list();
        return new ResponseEntity<>(users, HttpStatusCode.valueOf(200));
    }
}
