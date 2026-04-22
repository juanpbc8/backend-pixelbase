package com.pixelbase.backend.catalog.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    private String description;

    private List<Long> categoryIds;

}
