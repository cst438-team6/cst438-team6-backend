package com.cst438.controller;

import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.domain.Assignment;
import com.cst438.domain.Section;
import com.cst438.domain.Enrollment;
import com.cst438.domain.Grade;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.GradeRepository;
import com.cst438.domain.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    /**
     * Instructor lists assignments for a section.
     * Assignment data is returned ordered by due date.
     * Logged in user must be the instructor for the section (assignment 7).
     */

    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(@PathVariable("secNo") int secNo) {
        // Check if the section exists
        Section section = sectionRepository.findById(secNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        // Retrieve assignments for the section ordered by due date
        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        return assignments.stream()
                .map(a -> new AssignmentDTO(
                        a.getAssignmentId(),
                        a.getTitle(),
                        String.valueOf(a.getDueDate()),
                        section.getCourse().getCourseId(),
                        section.getSecId(),
                        section.getSectionNo()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Instructor creates an assignment for a section.
     * Assignment data with primary key is returned.
     * Logged in user must be the instructor for the section (assignment 7).
     */


    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(@RequestBody AssignmentDTO dto) {
        // Check if the section exists
        Section section = sectionRepository.findById(dto.secNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        // Create a new assignment entity
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.title());
        assignment.setDueDate(Date.valueOf(dto.dueDate()));

        // Save the assignment and return the created AssignmentDTO
        assignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                String.valueOf(assignment.getDueDate()),
                dto.courseId(),
                dto.secId(),
                dto.secNo()
        );
    }

    /**
     * Instructor updates an assignment for a section.
     * Only title and dueDate may be changed.
     * Updated assignment data is returned.
     * Logged in user must be the instructor for the section (assignment 7).
     */


    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {
        Assignment assignment = assignmentRepository.findById(dto.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        // Update the title and dueDate
        assignment.setTitle(dto.title());
        assignment.setDueDate(Date.valueOf(dto.dueDate()));

        // Save the updated assignment and return the updated AssignmentDTO
        assignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                String.valueOf(assignment.getDueDate()),
                dto.courseId(),
                dto.secId(),
                dto.secNo()
        );
    }


    /**
     * Instructor deletes an assignment for a section.
     * Logged in user must be the instructor for the section (assignment 7).
     */
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        // Delete the assignment
        assignmentRepository.delete(assignment);
    }

    // student lists their assignments/grades for an enrollment ordered by due date
    // student must be enrolled in the section
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        // check that this enrollment is for the logged in user student.

        List<AssignmentStudentDTO> dlist = new ArrayList<>();
        List<Assignment> alist = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        for (Assignment a : alist) {

            Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(a.getSection().getSectionNo(), studentId);
            if (e==null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found studentId:"+studentId+" sectionNo:"+a.getSection().getSectionNo());
            }

            // if assignment has been graded, include the score
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId( e.getEnrollmentId(), a.getAssignmentId());

            System.out.println(grade);

            dlist.add(new AssignmentStudentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    (grade!=null)? grade.getScore(): null ));

        }
        return dlist;
    }
}