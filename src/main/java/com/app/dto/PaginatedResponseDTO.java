package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedResponseDTO<T> {
    private List<UserResponseDTO> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean last;
}
