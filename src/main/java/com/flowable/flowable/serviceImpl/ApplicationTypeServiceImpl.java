package com.flowable.flowable.serviceImpl;

import com.flowable.flowable.dto.ResponseDTO;
import com.flowable.flowable.exception.AlreadyExistException;
import com.flowable.flowable.exception.NotFoundException;
import com.flowable.flowable.models.ApplicationType;
import com.flowable.flowable.repo.ApplicationTypeRepo;
import com.flowable.flowable.util.AppUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationTypeServiceImpl {

    private final ApplicationTypeRepo applicationTypeRepo;

    @Autowired
    public ApplicationTypeServiceImpl(ApplicationTypeRepo applicationTypeRepo) {
        this.applicationTypeRepo = applicationTypeRepo;
    }


    /**
     * @description this method is used to fetch all application types fom the db.
     * @Auther Emmanuel Yidana
     * @param
     * @return returns ResponseEntity containing the tasks response.
     * @Date 23/06/2025
     */
    public ResponseEntity<ResponseDTO> findAll(){

        List<ApplicationType> applicationTypes =  applicationTypeRepo.findAll();
        if (applicationTypes.isEmpty()){
            throw new NotFoundException("no application type record found");
        }

        ResponseDTO responseDTO = AppUtils.getResponseDto("application type created", HttpStatus.OK,applicationTypes);

        return new ResponseEntity<>(responseDTO, HttpStatusCode.valueOf(200));
    }


    /**
     * @description this method is used to save an application type to the db.
     * @Auther Emmanuel Yidana
     * @param applicationType
     * @return returns ResponseEntity containing the tasks response.
     * @Date 23/06/2025
     */
    public ResponseEntity<ResponseDTO> saveApplicationType(ApplicationType applicationType){

        ApplicationType existingData = applicationTypeRepo.findByName(applicationType.getName());

        if (existingData != null){
            throw new AlreadyExistException("application type already exist");
        }

        //convert name to uppercase
        applicationType.setName(applicationType.getName().toUpperCase());

        ResponseDTO responseDTO = AppUtils.getResponseDto("application type created", HttpStatus.CREATED, applicationTypeRepo.save(applicationType));

        return new ResponseEntity<>(responseDTO, HttpStatusCode.valueOf(201));
    }


    /**
     * @description this method is used to update an application type given the application id.
     * @Auther Emmanuel Yidana
     * @param applicationType
     * @return returns ResponseEntity containing the tasks response.
     * @Date 22/06/2025
     */
    public ResponseEntity<ResponseDTO> updateApplicationType(ApplicationType applicationType){

        ApplicationType existingData = applicationTypeRepo.findById(applicationType.getId())
                .orElseThrow(()-> new NotFoundException("application type record cannot be found"));

        existingData.setName(applicationType.getName());

        ResponseDTO responseDTO = AppUtils.getResponseDto("application type updated", HttpStatus.OK, applicationTypeRepo.save(existingData));

        return new ResponseEntity<>(responseDTO, HttpStatusCode.valueOf(200));
    }

    /**
     * @description this method is used to remove an application type record from the db given the id.
     * @Auther Emmanuel Yidana
     * @param id
     * @return returns ResponseEntity containing the tasks response.
     * @Date 23/06/2025
     */
    public ResponseEntity<ResponseDTO> removeApplicationType(UUID id){

        ApplicationType existingData = applicationTypeRepo.findById(id)
                .orElseThrow(()-> new NotFoundException("application type record cannot be found"));

        applicationTypeRepo.deleteById(id);

        ResponseDTO responseDTO = AppUtils.getResponseDto("application type deleted", HttpStatus.OK);

        return new ResponseEntity<>(responseDTO, HttpStatusCode.valueOf(200));
    }
}
