package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class GradeController {
    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    /**
     * Instructor lists the grades for an assignment for all enrolled students.
     * Returns the list of grades (ordered by student name) for the assignment.
     * If there is no Grade entity for an enrolled student, a Grade entity with a null score is created.
     * Logged-in user must be the instructor for the section.
     */
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {
        Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
        if (a == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "assignment not found " + assignmentId);
        } else {
            List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(a.getSection().getSectionNo());
            List<GradeDTO> dto_list1 = new ArrayList<>();
            for (Enrollment e : enrollments) {
                Grade g = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), assignmentId);
                if (g == null) {
                    g = new Grade();
                    g.setAssignment(a);
                    g.setEnrollment(e);
                }
                dto_list1.add(new GradeDTO(
                        g.getGradeId(),
                        g.getEnrollment().getStudent().getName(),
                        g.getEnrollment().getStudent().getEmail(),
                        g.getEnrollment().getSection().getCourse().getTitle(),
                        g.getEnrollment().getSection().getCourse().getCourseId(),
                        g.getEnrollment().getSection().getSecId(),
                        g.getScore()));
            }
            return dto_list1;
        }
    }

    /**
     * Instructor updates one or more assignment grades.
     * Only the score attribute of the Grade entity can be changed.
     * Logged-in user must be the instructor for the section.
     */
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {
        for (GradeDTO gDTO : dlist) {
            Grade g = gradeRepository.findById(gDTO.gradeId()).orElse(null);
            if (g == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found " + gDTO.gradeId());
            } else {
                g.setScore(gDTO.score());
                gradeRepository.save(g);
            }
        }
    }
}
