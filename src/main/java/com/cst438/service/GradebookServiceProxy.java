package com.cst438.service;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message)  {
        try {
            System.out.println("Received From Gradebook Service " + message);
            String[] parts = message.split(",", 2);
            if (parts[0].equals("updateEnrollment")) {
                EnrollmentDTO edto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = enrollmentRepository.findById(edto.enrollmentId()).orElse(null);
                if (e == null) {
                    System.out.println("receiveFromGradebook Error: Enrollment " + edto.enrollmentId() + " not found");
                } else {
                    e.setGrade(edto.grade());
                    enrollmentRepository.save(e);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in receiveFromGradebook " + e.getMessage());
        }
    }

    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
    }
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}