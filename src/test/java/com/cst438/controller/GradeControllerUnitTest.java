package com.cst438.controller;

import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest
public class GradeControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void updateGradeSuccessfully() throws Exception {
        // Step 1: Perform the GET request to fetch the grade for a specific assignment (using assignmentId 1)
        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/1/grades")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        String gradesJson = response.getContentAsString();
        GradeDTO[] gradeDTOs = new ObjectMapper().readValue(gradesJson, GradeDTO[].class);

        // Step 2: Update the GradeDTO objects with new scores
        List<GradeDTO> updatedGrades = new ArrayList<>();
        for (GradeDTO grade : gradeDTOs) {
            GradeDTO updatedGrade = new GradeDTO(
                    grade.gradeId(),
                    grade.studentName(),
                    grade.studentEmail(),
                    grade.assignmentTitle(),
                    grade.courseId(),
                    grade.sectionId(),
                    95
            );
            updatedGrades.add(updatedGrade);
        }

        // Step 3: Perform the PUT request to update the grades
        MockHttpServletResponse putResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(updatedGrades)))
                .andReturn()
                .getResponse();

        assertEquals(200, putResponse.getStatus());

        // Step 4: Perform a GET request again to fetch the updated grades
        MockHttpServletResponse getUpdatedResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/1/grades")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Assert that the grades were successfully updated and the score is now 95
        assertEquals(200, getUpdatedResponse.getStatus());
        String updatedGradesJson = getUpdatedResponse.getContentAsString();
        GradeDTO[] updatedGradeDTOs = new ObjectMapper().readValue(updatedGradesJson, GradeDTO[].class);

        // Check if the updated grades have the new score
        for (GradeDTO updatedGrade : updatedGradeDTOs) {
            assertEquals(95, (int) updatedGrade.score());
        }
    }


    @Test
    public void updateInvalidGrade() throws Exception {
        MockHttpServletResponse response;

        // Creating a GradeDTO with invalid gradeId
        GradeDTO grade = new GradeDTO(
                9999,
                "Jane Doe",
                "jane.doe@example.com",
                "Nonexistent Assignment",
                "CST363",
                1,
                90
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(grade)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
