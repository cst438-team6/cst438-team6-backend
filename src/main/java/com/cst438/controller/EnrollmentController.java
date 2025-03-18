package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository; // Inject the Enrollment repository

    /**
     * Instructor gets a list of enrollments for a section
     * The list of enrollments returned is in order by student name
     * Logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(@PathVariable("sectionNo") int sectionNo) {
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            EnrollmentDTO dto = new EnrollmentDTO(enrollment); // Assuming you have a constructor in EnrollmentDTO
            enrollmentDTOs.add(dto);
        }

        return enrollmentDTOs;
    }

    /**
     * Instructor updates enrollment grades
     * Only the grade attribute of enrollment can be changed
     * Logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/enrollments")
    public void updateEnrollmentGrades(@RequestBody List<EnrollmentDTO> dlist) {
        for (EnrollmentDTO enrollmentDTO : dlist) {
            Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(enrollmentDTO.getEnrollmentId());
            if (optionalEnrollment.isPresent()) {
                Enrollment enrollment = optionalEnrollment.get();
                enrollment.setGrade(enrollmentDTO.getGrade()); // Assume EnrollmentDTO has a getGrade method
                enrollmentRepository.save(enrollment); // Save updated enrollment back to the database
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found for ID: " + enrollmentDTO.getEnrollmentId());
            }
        }
    }
}