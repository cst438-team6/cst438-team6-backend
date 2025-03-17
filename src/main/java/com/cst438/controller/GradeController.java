package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class GradeController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    /**
     * Instructor lists the grades for an assignment for all enrolled students.
     * Returns the list of grades (ordered by student name) for the assignment.
     * If there is no Grade entity for an enrolled student, a Grade entity with a null score is created.
     * Logged-in user must be the instructor for the section.
     */
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        Section section = assignment.getSection();
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(section.getSectionNo());

        return enrollments.stream().map(enrollment -> {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignmentId)
                    .orElseGet(() -> {
                        Grade newGrade = new Grade();
                        newGrade.setEnrollment(enrollment);
                        newGrade.setAssignment(assignment);
                        newGrade.setScore(null);
                        return gradeRepository.save(newGrade);
                    });

            return new GradeDTO(
                    grade.getGradeId(),
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    assignment.getTitle(),
                    assignment.getSection().getCourse().getCourseId(),
                    assignment.getSection().getSectionNo(),
                    grade.getScore()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Instructor updates one or more assignment grades.
     * Only the score attribute of the Grade entity can be changed.
     * Logged-in user must be the instructor for the section.
     */
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {
        for (GradeDTO dto : dlist) {
            Grade grade = gradeRepository.findById(dto.gradeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));

            grade.setScore(dto.score());
            gradeRepository.save(grade);
        }
    }
}
