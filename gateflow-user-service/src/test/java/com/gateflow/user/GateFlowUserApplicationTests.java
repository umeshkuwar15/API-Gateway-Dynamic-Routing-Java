package com.gateflow.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateflow.user.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GateFlowUserApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void healthCheckReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("gateflow-user-service"))
                .andExpect(jsonPath("$.port").value(8081));
    }

    @Test
    void userCrudLifecycle() throws Exception {
        
        UserDto newUser = new UserDto();
        newUser.setUsername("alex_test");
        newUser.setEmail("alex@example.com");
        newUser.setFirstName("Alex");
        newUser.setLastName("Morgan");
        newUser.setDepartment("Security");
        newUser.setActive(true);

        String responseJson = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("alex_test"))
                .andReturn().getResponse().getContentAsString();

        UserDto created = objectMapper.readValue(responseJson, UserDto.class);
        Long id = created.getId();

        
        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alex@example.com"));

        
        created.setDepartment("Cloud Infrastructure");
        mockMvc.perform(put("/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Cloud Infrastructure"));

        
        mockMvc.perform(delete("/users/" + id))
                .andExpect(status().isNoContent());

        
        mockMvc.perform(get("/users/" + id))
                .andExpect(status().isNotFound());
    }
}
