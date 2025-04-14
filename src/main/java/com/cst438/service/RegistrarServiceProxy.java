package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TermRepository termRepository;
    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message) {
        try {
            String[] parts = message.split(" ", 2);
            String action = parts[0];

            // Handle each action directly without using switch statements
            if (action.equals("addCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = new Course();
                c.setCourseId(dto.courseId());
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
            } else if (action.equals("deleteCourse")) {
                courseRepository.deleteById(parts[1]);
            } else if (action.equals("updateCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                if (c != null) { // Check if course exists
                    c.setTitle(dto.title());
                    c.setCredits(dto.credits());
                    courseRepository.save(c);
                }
            } else if (action.equals("addUser")) {
                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User user = new User();
                user.setId(dto.id());
                user.setName(dto.name());
                user.setEmail(dto.email());
                user.setType(dto.type());
                userRepository.save(user);
            } else if (action.equals("deleteUser")) {
                userRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (action.equals("updateUser")) {
                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User user = userRepository.findById(dto.id()).orElse(null);
                if (user != null) { // Check if user exists
                    user.setName(dto.name());
                    user.setEmail(dto.email());
                    user.setType(dto.type());
                    userRepository.save(user);
                }
            } else if (action.equals("addSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                Section section = new Section();
                section.setCourse(c);
                section.setSectionNo(dto.secNo());
                section.setBuilding(dto.building());
                section.setRoom(dto.room());
                section.setTerm(term);
                section.setInstructor_email(dto.instructorEmail());
                section.setTimes(dto.times());
                section.setSecId(dto.secId());
                sectionRepository.save(section);
            } else if (action.equals("deleteSection")) {
                sectionRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (action.equals("updateSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section section = sectionRepository.findById(dto.secNo()).orElse(null);
                if (section != null) { // Check if section exists
                    Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                    Course c = courseRepository.findById(dto.courseId()).orElse(null);
                    section.setCourse(c);
                    section.setSectionNo(dto.secNo());
                    section.setBuilding(dto.building());
                    section.setRoom(dto.room());
                    section.setTerm(term);
                    section.setInstructor_email(dto.instructorEmail());
                    section.setTimes(dto.times());
                    section.setSecId(dto.secId());
                    sectionRepository.save(section);
                }
            } else if (action.equals("addEnrollment")) {
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(dto.enrollmentId());
                User student = userRepository.findById(dto.studentId()).orElse(null);
                enrollment.setStudent(student);
                Section section = sectionRepository.findById(dto.sectionId()).orElse(null);
                enrollment.setSection(section);
                enrollmentRepository.save(enrollment);
            } else if (action.equals("deleteEnrollment")) {
                enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (action.equals("updateEnrollment")) {
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment enrollment = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
                if (enrollment != null) { // Check if enrollment exists
                    enrollment.setGrade(dto.grade()); // Update the final grade
                    enrollmentRepository.save(enrollment);
                    // Send a message to the registrar service about the updated enrollment
                    sendMessage(asJsonString(dto));
                }
            } else {
                System.out.println("Invalid action: " + action);
            }
        } catch (Exception e) {
            System.out.println("Exception in receiveFromRegistrar: " + e.getMessage());
        }
    }

    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}