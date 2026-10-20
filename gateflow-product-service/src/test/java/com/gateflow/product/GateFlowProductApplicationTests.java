package com.gateflow.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateflow.product.dto.ProductDto;
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
class GateFlowProductApplicationTests {

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
                .andExpect(jsonPath("$.service").value("gateflow-product-service"))
                .andExpect(jsonPath("$.port").value(8082));
    }

    @Test
    void productCrudLifecycle() throws Exception {
        
        ProductDto newProduct = new ProductDto();
        newProduct.setName("Wireless Gaming Headset");
        newProduct.setDescription("Low-latency wireless audio with surround sound");
        newProduct.setPrice(new BigDecimal("129.99"));
        newProduct.setStockQuantity(40);
        newProduct.setCategory("Gaming");

        String responseJson = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Wireless Gaming Headset"))
                .andReturn().getResponse().getContentAsString();

        ProductDto created = objectMapper.readValue(responseJson, ProductDto.class);
        Long id = created.getId();

        
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Gaming"));

        
        created.setStockQuantity(35);
        mockMvc.perform(put("/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(35));

        
        mockMvc.perform(delete("/products/" + id))
                .andExpect(status().isNoContent());

        
        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isNotFound());
    }
}
