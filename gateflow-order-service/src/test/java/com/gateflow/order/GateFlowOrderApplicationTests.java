package com.gateflow.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateflow.order.dto.OrderDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GateFlowOrderApplicationTests {

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
                .andExpect(jsonPath("$.service").value("gateflow-order-service"))
                .andExpect(jsonPath("$.port").value(8083));
    }

    @Test
    void orderCrudLifecycle() throws Exception {
        
        OrderDto newOrder = new OrderDto();
        newOrder.setCustomerId(101L);
        newOrder.setOrderStatus("PENDING");
        newOrder.setTotalAmount(new BigDecimal("349.50"));
        newOrder.setShippingAddress("789 Silicon Way, San Jose, CA");

        String responseJson = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.orderStatus").value("PENDING"))
                .andReturn().getResponse().getContentAsString();

        OrderDto created = objectMapper.readValue(responseJson, OrderDto.class);
        Long id = created.getId();

        
        mockMvc.perform(get("/orders/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(101));

        
        created.setOrderStatus("CONFIRMED");
        mockMvc.perform(put("/orders/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"));

        
        mockMvc.perform(delete("/orders/" + id))
                .andExpect(status().isNoContent());

        
        mockMvc.perform(get("/orders/" + id))
                .andExpect(status().isNotFound());
    }
}
