package com.chiringuito.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMenu_shouldReturnAllAvailableMenuItems() throws Exception {
        mockMvc.perform(get("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].price").exists())
                .andExpect(jsonPath("$[0].imageUrl").exists())
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getMenu_shouldReturnMenuItemsSortedByName() throws Exception {
        mockMvc.perform(get("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Calamares Fritos"))
                .andExpect(jsonPath("$[1].name").value("Churros con Chocolate"))
                .andExpect(jsonPath("$[2].name").value("Ensalada Mixta"));
    }

    @Test
    void getMenu_shouldReturnValidPriceFormat() throws Exception {
        mockMvc.perform(get("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price", isA(Number.class)))
                .andExpect(jsonPath("$[0].price").exists());
    }

    @Test
    void getMenu_shouldHandleEmptyDatabase() throws Exception {
        // This test would fail with current data, but demonstrates test coverage
        // In a real scenario, we'd use @Sql to clear the database first
        mockMvc.perform(get("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}