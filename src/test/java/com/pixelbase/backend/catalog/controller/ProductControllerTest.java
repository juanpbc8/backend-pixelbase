package com.pixelbase.backend.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixelbase.backend.catalog.dto.ProductRequest;
import com.pixelbase.backend.catalog.entity.ProductEntity;
import com.pixelbase.backend.catalog.service.ProductService;
import com.pixelbase.backend.modules.security.jwt.JwtAuthenticationFilter;
import com.pixelbase.backend.modules.security.jwt.JwtService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    // Necesario porque SecurityConfig los inyecta aunque los filtros estén desactivados
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    private ProductEntity sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = ProductEntity.builder()
                .id(1L)
                .name("Laptop Lenovo")
                .description("Ultrabook")
                .categories(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("GET /api/products devuelve página")
    void searchReturnsPage() throws Exception {
        Page<ProductEntity> page = new PageImpl<>(List.of(sampleProduct));
        when(productService.search(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop Lenovo"));
    }

    @Test
    @DisplayName("GET /api/products/{id} devuelve producto")
    void getReturnsProduct() throws Exception {
        when(productService.findById(1L)).thenReturn(sampleProduct);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop Lenovo"));
    }

    @Test
    @DisplayName("GET /api/products/{id} devuelve 404 cuando no existe")
    void getReturns404WhenNotFound() throws Exception {
        when(productService.findById(99L)).thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products crea producto válido")
    void createValidProduct() throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName("Laptop");
        req.setDescription("Nueva");

        when(productService.create(any())).thenReturn(sampleProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/products devuelve 400 si el body es inválido")
    void createFailsWithValidationErrors() throws Exception {
        // name vacío viola @NotBlank
        ProductRequest req = new ProductRequest();

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} devuelve 204")
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
