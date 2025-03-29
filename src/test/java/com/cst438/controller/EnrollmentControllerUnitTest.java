package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpResponse;
import java.util.Date;

import static com.cst438.test.utils.TestUtils.asJsonString;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest

public class EnrollmentControllerUnitTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void addEnrollment() throws Exception {

        MockHttpServletResponse response;

        int sectionNo = 9;
        int studentId = 3;

        // create DTO with data for new enrollment.
        // the primary key, enrollmentId, is set to 0. it will be
        // set by the database when the assignment is inserted.
        EnrollmentDTO enrollment = new EnrollmentDTO(
                0,
                null,
                studentId,
                null,
                null,
              null,
                 null,
             1,
            sectionNo,
              null,
                null,
                null,
               0,
                0,
             null
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+sectionNo+"?studentId="+studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        // return data converted from String to DTO
        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.enrollmentId());
        // check other fields of the DTO for expected values
        assertEquals("cst363", result.courseId());
        assertNull(result.grade());
        assertEquals(sectionNo, result.sectionNo());
        assertEquals(2, result.sectionId());
        assertEquals(2025, result.year());
        assertEquals(studentId, result.studentId());

        // check the database
        Enrollment e = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNotNull(e);
        assertEquals("cst363", e.getSection().getCourse().getCourseId());
        assertNull(e.getGrade());
        assertEquals(sectionNo, e.getSection().getSectionNo());
        assertEquals(2, e.getSection().getSecId());
        assertEquals(2025, e.getSection().getTerm().getYear());
        assertEquals(studentId, e.getStudent().getId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/enrollments/" + result.enrollmentId()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        e = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNull(e);  // section should not be found after delete
    }

    @Test
    public void addDuplicateEnrollment() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new enrollment.
        // the primary key, enrollmentId, is set to 0. it will be
        // set by the database when the assignment is inserted.

        int sectionNo = 8;
        int studentId = 3;
        EnrollmentDTO enrollment = new EnrollmentDTO(
                0,
                null,
                studentId,
                null,
                null,
                null,
                null,
                1,
                sectionNo,
                null,
                null,
                null,
                0,
                0,
                null
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content


        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+sectionNo+"?studentId="+studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for 400 meaning bad request
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("student " + studentId +
                " already enrolled in section " + sectionNo, response.getErrorMessage());
    }

    @Test
    public void addEnrollmentBadSection() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new enrollment.
        // the primary key, enrollmentId, is set to 0. it will be
        // set by the database when the assignment is inserted.

        int sectionNo = 0;
        int studentId = 3;
        EnrollmentDTO enrollment = new EnrollmentDTO(
                0,
                null,
                studentId,
                null,
                null,
                null,
                null,
                1,
                sectionNo,
                null,
                null,
                null,
                0,
                0,
                null
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content


        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+sectionNo+"?studentId="+studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for not found
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("section not found " + sectionNo, response.getErrorMessage());
    }

    @Test
    public void addEnrollmentPastDeadline() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new enrollment.
        // the primary key, enrollmentId, is set to 0. it will be
        // set by the database when the assignment is inserted.

        int sectionNo = 5;
        int studentId = 3;
        EnrollmentDTO enrollment = new EnrollmentDTO(
                0,
                null,
                studentId,
                null,
                null,
                null,
                null,
                1,
                sectionNo,
                null,
                null,
                null,
                0,
                0,
                null
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content


        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/"+sectionNo+"?studentId="+studentId)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for bad request
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        Date now = new Date();
        assertEquals("today is before the add date or after the deadline " + now, response.getErrorMessage());
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
