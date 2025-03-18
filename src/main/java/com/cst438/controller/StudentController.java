package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    /**
     * Students list their enrollments given year and semester value.
     * Returns a list of enrollments, may be empty.
     * Logged in user must be the student (assignment 7).
     */
    @GetMapping("/enrollments")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            @RequestParam("studentId") int studentId) {

        // Fetch the list of enrollments for the student for the given year and semester
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);

        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();

        // Map each Enrollment to an EnrollmentDTO
        for (Enrollment enrollment : enrollments) {
            Section section = enrollment.getSection();
            EnrollmentDTO enrollmentDTO = new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    enrollment.getStudent().getId(),
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    section.getCourse().getCourseId(),
                    section.getCourse().getTitle(),
                    section.getSectionNo(),
                    section.getSectionNo(),
                    section.getBuilding(),
                    section.getRoom(),
                    section.getTimes(),
                    section.getCourse().getCredits(),
                    section.getTerm().getYear(),
                    section.getTerm().getSemester()
            );
            enrollmentDTOs.add(enrollmentDTO);
        }

        return enrollmentDTOs;
    }

    /**
     * Students list their assignments given year and semester value.
     * Returns a list of assignments, may be empty.
     * Logged in user must be the student (assignment 7).
     */
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        // Fetch the list of enrollments for the student based on year and semester
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);

        List<AssignmentStudentDTO> assignmentDTOs = new ArrayList<>();

        // For each enrollment, find the assignments related to the section
        for (Enrollment enrollment : enrollments) {
            Section section = enrollment.getSection();
            List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);

            // For each assignment, check if there is a grade associated with the student
            for (Assignment assignment : assignments) {
                Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignment.getAssignmentId());

                // If grade is not found, it means the student doesn't have a grade yet
                Integer gradeScore = (grade != null) ? grade.getScore() : null;

                // Create the AssignmentStudentDTO object using the constructor matching the record fields
                AssignmentStudentDTO assignmentDTO = new AssignmentStudentDTO(
                        assignment.getAssignmentId(),
                        assignment.getTitle(),
                        assignment.getDueDate(),
                        section.getCourse().getCourseId(),
                        section.getSectionNo(),
                        gradeScore
                );

                assignmentDTOs.add(assignmentDTO);
            }
        }

        return assignmentDTOs;
    }
}
