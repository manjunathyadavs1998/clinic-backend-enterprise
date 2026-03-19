package com.clinic.app.controller;

import com.clinic.app.dto.labtest.CreateLabTestRequest;
import com.clinic.app.dto.labtest.LabTestResponse;
import com.clinic.app.dto.labtest.UpdateLabTestRequest;
import com.clinic.app.config.SecurityConfig;
import com.clinic.app.security.JwtService;
import com.clinic.app.security.TokenBlacklistService;
import com.clinic.app.security.CustomUserDetailsService;
import com.clinic.app.service.LabTestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabTestController.class)
@Import(SecurityConfig.class)
class LabTestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean LabTestService labTestService;
    @MockBean JwtService jwtService;
    @MockBean TokenBlacklistService tokenBlacklistService;
    @MockBean CustomUserDetailsService customUserDetailsService;

    private LabTestResponse sampleResponse() {
        return LabTestResponse.builder()
                .id(1L).name("Blood Test").cost(new BigDecimal("100.00")).active(true).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_shouldReturn200WithList() throws Exception {
        when(labTestService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/lab-tests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Blood Test"))
                .andExpect(jsonPath("$[0].cost").value(100.00));
    }

    @Test
    @WithMockUser(roles = "RECEPTIONIST")
    void getAll_shouldReturn200ForReceptionist() throws Exception {
        when(labTestService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/v1/lab-tests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_shouldReturn200() throws Exception {
        when(labTestService.getById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/lab-tests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Blood Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn200WithCreatedResponse() throws Exception {
        CreateLabTestRequest request = new CreateLabTestRequest();
        request.setName("Blood Test");
        request.setCost(new BigDecimal("100.00"));

        when(labTestService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/lab-tests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Blood Test"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void create_shouldReturn403ForDoctor() throws Exception {
        CreateLabTestRequest request = new CreateLabTestRequest();
        request.setName("Blood Test");
        request.setCost(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/lab-tests")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn200() throws Exception {
        UpdateLabTestRequest request = new UpdateLabTestRequest();
        request.setName("Updated Test");

        LabTestResponse updated = LabTestResponse.builder()
                .id(1L).name("Updated Test").cost(new BigDecimal("100.00")).active(true).build();

        when(labTestService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(patch("/api/v1/lab-tests/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn200() throws Exception {
        doNothing().when(labTestService).delete(1L);

        mockMvc.perform(delete("/api/v1/lab-tests/1").with(csrf()))
                .andExpect(status().isOk());

        verify(labTestService).delete(1L);
    }

    @Test
    void getAll_shouldReturn4xxWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/lab-tests"))
                .andExpect(status().is4xxClientError());
    }
}
