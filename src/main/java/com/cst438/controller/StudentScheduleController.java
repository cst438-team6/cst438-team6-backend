package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentScheduleController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;


    /**
     * students lists their transcript containing all enrollments
     * returns list of enrollments in chronological order
     * logged in user must be the student (assignment 7)
     * example URL  /transcript?studentId=19803
     */
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {
        // list course_id, sec_id, title, credit, grade
        // hint: use enrollment repository method findEnrollmentByStudentIdOrderByTermId
        // remove the following line when done
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);
        List<EnrollmentDTO> eDTOList = new ArrayList<>();
        for (Enrollment e : enrollments) {
            eDTOList.add(new EnrollmentDTO(
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
        return eDTOList;
    }


    /**
     * students enrolls into a section of a course
     * returns the enrollment data including primary key
     * logged in user must be the student (assignment 7)
     */
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId) {

        // TODO

        // check that the Section entity with primary key sectionNo exists
        // check that today is between addDate and addDeadline for the section
        // check that student is not already enrolled into this section
        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.

        // remove the following line when done.

        Section s = sectionRepository.findById(sectionNo).orElse(null);
        if (s == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "section not found " + sectionNo);
        }
        LocalDate dateNow = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate deadline = LocalDate.parse(s.getTerm().getAddDeadline().toString(), formatter);
        LocalDate start = LocalDate.parse(s.getTerm().getAddDate().toString(), formatter);
        if (!dateNow.isBefore(deadline) || !dateNow.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "today is before the add date or after the deadline " + dateNow.toString());
        }

        Enrollment e = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if (e != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student " + studentId +
                    " already enrolled in section " + sectionNo);
        }

        User u = userRepository.findById(studentId).orElse(null);
        if (u == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student doesn't exist" + studentId);
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(u);
        enrollment.setSection(s);

        enrollmentRepository.save(enrollment);
        return new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                null,
                u.getId(),
                u.getName(),
                u.getEmail(),
                s.getCourse().getCourseId(),
                s.getCourse().getTitle(),
                s.getSecId(),
                s.getSectionNo(),
                s.getBuilding(),
                s.getRoom(),
                s.getTimes(),
                s.getCourse().getCredits(),
                s.getTerm().getYear(),
                s.getTerm().getSemester()
        );
    }


    /**
     * students drops an enrollment for a section
     * logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

        // TODO
        // check that today is not after the dropDeadline for section
        Enrollment e = enrollmentRepository.findById(enrollmentId).orElse(null);
        if (e == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found " + enrollmentId);
        }

        LocalDate dateNow = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate deadline = LocalDate.parse(e.getSection().getTerm().getDropDeadline().toString(), formatter);
        if (!dateNow.isAfter(deadline)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "today is after the deadline " + dateNow.toString());
        } else {
            enrollmentRepository.delete(e);
        }
    }


}
