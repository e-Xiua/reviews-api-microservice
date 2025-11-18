package com.iwellness.reviews.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwellness.reviews.client.CorsConfig;
import com.iwellness.reviews.client.CorsConfigurationProperties;
import com.iwellness.reviews.dto.ReviewRequestDTO;
import com.iwellness.reviews.dto.ReviewResponseDTO;
import com.iwellness.reviews.model.EntityType;
import com.iwellness.reviews.service.ReviewService;

@WebMvcTest(ReviewController.class)
@Import({CorsConfig.class, CorsConfigurationProperties.class})
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewRequestDTO validRequest;
    private ReviewResponseDTO validResponse;

    @BeforeEach
    void setUp() {
        validRequest = ReviewRequestDTO.builder()
                .entityType(EntityType.SERVICE)
                .entityId(1L)
                .rating(5)
                .comment("Excellent service!")
                .build();

        validResponse = ReviewResponseDTO.builder()
                .id(1L)
                .entityId(1L)
                .entityType("SERVICE")
                .userId(100L)
                .nombre("John Doe")
                .rating(5)
                .comment("Excellent service!")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/reviews - Success")
    void createReview_Success() throws Exception {
        when(reviewService.createReview(any(ReviewRequestDTO.class), eq(100L)))
                .thenReturn(validResponse);

        mockMvc.perform(post("/api/reviews")
                        .header("X-User-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.nombre").value("John Doe"));

        verify(reviewService, times(1)).createReview(any(), eq(100L));
    }

    @Test
    @DisplayName("POST /api/reviews - Missing user header")
    void createReview_MissingUserHeader_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/reviews/{id} - Success")
    void updateReview_Success() throws Exception {
        when(reviewService.updateReview(eq(1L), any(), eq(100L)))
                .thenReturn(validResponse);

        mockMvc.perform(put("/api/reviews/1")
                        .header("X-User-Id", "100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @DisplayName("DELETE /api/reviews/{id} - Success")
    void deleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(1L, 100L);

        mockMvc.perform(delete("/api/reviews/1")
                        .header("X-User-Id", "100"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/reviews/entity/{type}/{id} - Success")
    void getReviewsByEntity_Success() throws Exception {
        Page<ReviewResponseDTO> page = new PageImpl<>(
                List.of(validResponse), 
                PageRequest.of(0, 10), 
                1L);
        
        when(reviewService.getReviewsByEntity(anyLong(), anyString(), anyInt(), anyInt(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/reviews/entity/SERVICE/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}