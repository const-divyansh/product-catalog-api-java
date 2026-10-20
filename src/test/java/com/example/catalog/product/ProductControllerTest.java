package com.example.catalog.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void crudAndFilterFlow() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
            "name", "Running Shoe", "category", "shoes", "price", "89.99", "currency", "USD"
        ));
        String response = mvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Running Shoe"))
            .andReturn().getResponse().getContentAsString();

        long id = mapper.readTree(response).get("id").asLong();
        mvc.perform(get("/api/v1/products/" + id)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/products").param("category", "shoes")
                .param("minPrice", "50").param("maxPrice", "100"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(patch("/api/v1/products/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"price\":\"79.99\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(79.99));
        mvc.perform(delete("/api/v1/products/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/products/" + id)).andExpect(status().isNotFound());
    }
}
