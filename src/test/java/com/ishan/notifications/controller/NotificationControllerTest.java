package com.ishan.notifications.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void supportsTheNotificationLifecycle() throws Exception {
        String id = createNotification("user-" + UUID.randomUUID(), "PAYROLL_COMPLETED", "Payroll completed");

        mockMvc.perform(get("/notifications/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("UNREAD"))
                .andExpect(jsonPath("$.readAt").doesNotExist());

        mockMvc.perform(patch("/notifications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READ"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READ"))
                .andExpect(jsonPath("$.readAt").isNotEmpty());

        mockMvc.perform(patch("/notifications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"UNREAD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNREAD"))
                .andExpect(jsonPath("$.readAt").doesNotExist());

        mockMvc.perform(delete("/notifications/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/notifications/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void filtersByRecipientAndStatus() throws Exception {
        String recipientId = "user-" + UUID.randomUUID();
        String unreadId = createNotification(recipientId, "WELCOME", "Welcome");
        String readId = createNotification(recipientId, "PAYROLL_COMPLETED", "Payroll completed");
        createNotification("another-" + UUID.randomUUID(), "PAYROLL_COMPLETED", "Another recipient");

        mockMvc.perform(patch("/notifications/{id}", readId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"READ"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/notifications").param("recipientId", recipientId).param("status", "UNREAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(unreadId));
    }

    @Test
    void rejectsInvalidCreateRequests() throws Exception {
        mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientId":" ","type":"","message":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void rejectsInvalidStatusAndIdentifier() throws Exception {
        String id = createNotification("user-" + UUID.randomUUID(), "WELCOME", "Welcome");

        mockMvc.perform(patch("/notifications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ARCHIVED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request body"));

        mockMvc.perform(get("/notifications/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request parameter"));
    }

    @Test
    void returnsNotFoundForAnUnknownNotification() throws Exception {
        mockMvc.perform(get("/notifications/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    private String createNotification(String recipientId, String type, String message) throws Exception {
        String requestBody = """
                {
                  "recipientId": "%s",
                  "type": "%s",
                  "message": "%s"
                }
                """.formatted(recipientId, type, message);

        MvcResult result = mockMvc.perform(post("/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/notifications/[0-9a-f-]+")))
                .andExpect(jsonPath("$.recipientId").value(recipientId))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.status").value("UNREAD"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.readAt").doesNotExist())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }
}
