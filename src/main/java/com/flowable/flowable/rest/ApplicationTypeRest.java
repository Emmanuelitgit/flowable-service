package com.flowable.flowable.rest;

import com.flowable.flowable.dto.ResponseDTO;
import com.flowable.flowable.models.ApplicationType;
import com.flowable.flowable.serviceImpl.ApplicationTypeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationTypeRest {

    private final ApplicationTypeServiceImpl applicationTypeService;

    @Autowired
    public ApplicationTypeRest(ApplicationTypeServiceImpl applicationTypeService) {
        this.applicationTypeService = applicationTypeService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO> findAll(){
        return applicationTypeService.findAll();
    }

    @PostMapping
    public ResponseEntity<ResponseDTO> saveApplicationType(@RequestBody ApplicationType applicationType){
        return applicationTypeService.saveApplicationType(applicationType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO> updateApplicationType(@RequestBody ApplicationType applicationType, @PathVariable UUID id){
        applicationType.setId(id);
        return applicationTypeService.updateApplicationType(applicationType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO> removeApplicationType(@PathVariable UUID id){
        return applicationTypeService.removeApplicationType(id);
    }
}
