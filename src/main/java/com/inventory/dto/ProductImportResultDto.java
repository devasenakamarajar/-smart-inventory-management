package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductImportResultDto {

    private int importedCount;
    private int failedCount;
    private List<String> errors;
}