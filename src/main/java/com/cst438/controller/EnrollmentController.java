package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Instructor gets list of enrollments for a section.
     * The list is returned in order by student name.
     * The logged-in user must be the instructor for the section.
     */
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(@PathVariable("sectionNo") int sectionNo) {
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
        
        if (enrollments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No enrollments found for section " + sectionNo);
        }
        
        return enrollments.stream()
                .map(enrollment -> {
                    var section = enrollment.getSection();
                    var course = section.getCourse();
                    var term = section.getTerm();

                    return new EnrollmentDTO(
                        enrollment.getEnrollmentId(),
                        enrollment.getGrade(),
                        enrollment.getStudent().getId(),
                        enrollment.getStudent().getName(),
                        enrollment.getStudent().getEmail(),
                        course.getCourseId(),
                        course.getTitle(),
                        section.getSecId(),
                        section.getSectionNo(),
                        section.getBuilding(),
                        section.getRoom(),
                        section.getTimes(),
                        course.getCredits(),
                        term.getYear(),
                        term.getSemester()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Instructor updates enrollment grades.
     * Only the grade attribute of enrollment can be changed.
     * The logged-in user must be the instructor for the section.
     */ 
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {
        for (EnrollmentDTO dto : dlist) {
            Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Enrollment ID " + dto.enrollmentId() + " not found"));

            enrollment.setGrade(dto.grade());
            enrollmentRepository.save(enrollment);
        }
    }
}
