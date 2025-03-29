package com.cst438.controller;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.dto.AssignmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.cst438.test.utils.TestUtils.asJsonString;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest

public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Test
    public void addAssignment() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new assignment.
        // the primary key, id, is set to 0. it will be
        // set by the database when the assignment is inserted.
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Test assignment",
                "2025-03-30",
                "cst363",
                1,
                8
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.id());
        // check other fields of the DTO for expected values
        assertEquals("cst363", result.courseId());

        // check the database
        Assignment a = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(a);
        assertEquals("cst363", a.getSection().getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignments/" + result.id()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a);  // section should not be found after delete
    }

    @Test
    public void addAssignmentWithBadDueDate() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new assignment.
        // the primary key, id, is set to 0. it will be
        // set by the database when the assignment is inserted.
        // set due date outside start and end dates for the sections term
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Test assignment",
                "2035-03-30",
                "cst363",
                1,
                8
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // check the response code for 409 meaning conflict
        assertEquals(409, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Due date is outside course date range", message);
    }

    @Test
    public void addAssignmentWithInvalidSectionNumber() throws Exception {

        MockHttpServletResponse response;

        // create DTO with data for new Assignment.
        // the primary key, secNo, is set to 0. it will be
        // set by the database when the Assignment is inserted.
        //section number can't be 0
        AssignmentDTO assignment = new AssignmentDTO(
                0,
                "Test assignment",
                "2035-03-30",
                "cst363",
                1,
                0
        );
        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert Assignment to String data and set as request content

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignment)))
                .andReturn()
                .getResponse();

        // check the response code for 404 meaning not found
        assertEquals(404, response.getStatus());

        // check the expected error message
        String message = response.getErrorMessage();
        assertEquals("Section not found", message);
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
