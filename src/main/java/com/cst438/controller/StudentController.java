package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

   @Autowired
   EnrollmentRepository enrollmentRepository;

   @Autowired
   UserRepository userRepository;

   @Autowired
   SectionRepository sectionRepository;



    // student gets class schedule for a given term
    // user must be student
    // remove studentId request param after login security implemented
   @GetMapping("/enrollments")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           @RequestParam("studentId") int studentId) {

        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);
        List<EnrollmentDTO> dlist = new ArrayList<>();
        for (Enrollment e : enrollments) {
         dlist.add( new EnrollmentDTO(
                 e.getEnrollmentId(),
                 e.getGrade(),
                 e.getStudent().getId(),
                 e.getStudent().getName(),
                 e.getStudent().getEmail(),
                 e.getSection().getCourse().getCourseId(),
                 e.getSection().getCourse().getTitle(),
                 e.getSection().getSecId(),
                 e.getSection().getSectionNo(),
                 e.getSection().getBuilding(),
                 e.getSection().getRoom(),
                 e.getSection().getTimes(),
                 e.getSection().getCourse().getCredits(),
                 e.getSection().getTerm().getYear(),
                 e.getSection().getTerm().getSemester()));
        }
        return dlist;
   }
}