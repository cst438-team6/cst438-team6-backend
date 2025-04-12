package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.SectionDTO;
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

    @Autowired
    UserRepository userRepository;

    /**
     * Instructor lists assignments for a section.
     * Assignment data is returned ordered by due date.
     * Logged in user must be the instructor for the section (assignment 7).
     */

    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(@PathVariable("secNo") int secNo) {
        Section section = sectionRepository.findById(secNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

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
        Section section = sectionRepository.findById(dto.secNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        Term term = section.getTerm();
        Date dueDate = Date.valueOf(dto.dueDate());
        if (dueDate.before(term.getStartDate()) || dueDate.after(term.getEndDate())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Due date is outside course date range");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(dto.title());
        assignment.setDueDate(dueDate);

        assignment.setSection(section);

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

        assignment.setTitle(dto.title());
        assignment.setDueDate(Date.valueOf(dto.dueDate()));

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

    // get Sections for an instructor
    // example URL  /sections?instructorEmail=dwisneski@csumb.edu&year=2024&semester=Spring
    @GetMapping("/sections")
    public List<SectionDTO> getSectionsForInstructor(
            @RequestParam("email") String instructorEmail,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {


        List<Section> sections = sectionRepository.findByInstructorEmailAndYearAndSemester(instructorEmail, year, semester);

        List<SectionDTO> dto_list = new ArrayList<>();
        for (Section s : sections) {
            User instructor = null;
            if (s.getInstructorEmail() != null) {
                instructor = userRepository.findByEmail(s.getInstructorEmail());
            }
            dto_list.add(new SectionDTO(
                    s.getSectionNo(),
                    s.getTerm().getYear(),
                    s.getTerm().getSemester(),
                    s.getCourse().getCourseId(),
                    s.getCourse().getTitle(),
                    s.getSecId(),
                    s.getBuilding(),
                    s.getRoom(),
                    s.getTimes(),
                    (instructor != null) ? instructor.getName() : "",
                    (instructor != null) ? instructor.getEmail() : ""
            ));
        }
        return dto_list;
    }
}