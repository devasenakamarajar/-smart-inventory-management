package com.inventory.service;

import com.inventory.dto.ProductDto;
import com.inventory.dto.ProductImportResultDto;
import com.inventory.entity.Category;
import com.inventory.entity.Product;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAllActiveProducts().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDto(product);
    }

    public ProductDto createProduct(ProductDto dto) {
        validateProductData(dto);

        if (productRepository.findBySku(dto.getSku().trim()).isPresent()) {
            throw new DuplicateResourceException("Product with SKU already exists: " + dto.getSku());
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Product product = new Product();
        product.setProductName(dto.getProductName().trim());
        product.setSku(dto.getSku().trim());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setBrand(dto.getBrand());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setCurrentStock(dto.getCurrentStock());
        product.setMinimumStock(dto.getMinimumStock());
        product.setMaximumStock(dto.getMaximumStock());
        product.setExpiryDate(dto.getExpiryDate());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : true);

        return convertToDto(productRepository.save(product));
    }

    public ProductImportResultDto importProducts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please choose a CSV file to import");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only CSV files are supported");
        }

        int importedCount = 0;
        List<String> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String csv = reader.lines().collect(Collectors.joining("\n"));
            if (csv.startsWith("\uFEFF")) {
                csv = csv.substring(1);
            }
            char delimiter = csv.lines().findFirst()
                    .map(line -> line.contains(";") && !line.contains(",") ? ';' : ',')
                    .orElse(',');

            Set<String> requiredHeaders = new HashSet<>(Arrays.asList(
                    "productName", "sku", "categoryId", "purchasePrice", "sellingPrice",
                    "currentStock", "minimumStock", "maximumStock"));

                try (CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setDelimiter(delimiter)
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build()
                     .parse(new StringReader(csv))) {
                parser.getHeaderMap().keySet().stream()
                    .filter(header -> header != null && header.trim().isEmpty())
                    .findAny()
                    .ifPresent(header -> { throw new IllegalArgumentException("CSV contains an empty column header"); });
                Set<String> missingHeaders = new HashSet<>(requiredHeaders);
                Set<String> headers = parser.getHeaderMap().keySet().stream()
                    .map(header -> header.trim())
                    .collect(Collectors.toSet());
                missingHeaders.removeAll(headers);
                if (!missingHeaders.isEmpty()) {
                    throw new IllegalArgumentException("Missing required columns: " + String.join(", ", missingHeaders));
                }
            for (CSVRecord record : parser) {
                try {
                    createProduct(toProductDto(record));
                    importedCount++;
                } catch (Exception exception) {
                    errors.add("Row " + record.getRecordNumber() + ": " + exception.getMessage());
                }
            }
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Could not read CSV file: " + exception.getMessage());
        }
        return new ProductImportResultDto(importedCount, errors.size(), errors);
    }

    private ProductDto toProductDto(CSVRecord record) {
        ProductDto dto = new ProductDto();
        dto.setProductName(requiredValue(record, "productName"));
        dto.setSku(requiredValue(record, "sku"));
        dto.setDescription(valueOrNull(record, "description"));
        dto.setCategoryId(resolveCategoryId(requiredValue(record, "categoryId")));
        dto.setBrand(valueOrNull(record, "brand"));
        dto.setPurchasePrice(new BigDecimal(requiredValue(record, "purchasePrice")));
        dto.setSellingPrice(new BigDecimal(requiredValue(record, "sellingPrice")));
        dto.setCurrentStock(Integer.valueOf(requiredValue(record, "currentStock")));
        dto.setMinimumStock(Integer.valueOf(requiredValue(record, "minimumStock")));
        dto.setMaximumStock(Integer.valueOf(requiredValue(record, "maximumStock")));
        dto.setExpiryDate(optionalDate(record, "expiryDate"));
        dto.setStatus(true);
        return dto;
    }

    private String requiredValue(CSVRecord record, String column) {
        String value = valueOrNull(record, column);
        if (value == null) {
            throw new IllegalArgumentException(column + " is required");
        }
        return value;
    }

    private String valueOrNull(CSVRecord record, String column) {
        String value = record.get(column);
        return value == null || value.isBlank() ? null : value;
    }

    private Long optionalReferenceId(CSVRecord record, String column) {
        String value = valueOrNull(record, column);
        return value == null ? null : parseReferenceId(value, column);
    }

    private Long resolveCategoryId(String value) {
        String normalized = value.trim();
        if (normalized.matches("CAT\\d+")) {
            return categoryRepository.findByName(normalized)
                    .orElseGet(() -> {
                        Category category = new Category();
                        category.setName(normalized);
                        category.setStatus(true);
                        return categoryRepository.save(category);
                    })
                    .getId();
        }
        return parseReferenceId(normalized, "categoryId");
    }

    private Long parseReferenceId(String value, String column) {
        String normalized = value.trim();
        if (normalized.matches("[A-Za-z]+\\d+")) {
            normalized = normalized.replaceAll("^[A-Za-z]+", "");
        }
        try {
            return Long.valueOf(normalized);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(column + " must be a numeric ID or a code such as CAT004");
        }
    }

    private LocalDate optionalDate(CSVRecord record, String column) {
        String value = valueOrNull(record, column);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("expiryDate must use YYYY-MM-DD format");
        }
    }

    public ProductDto updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        validateProductData(dto);

        productRepository.findBySku(dto.getSku().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Product with SKU already exists: " + dto.getSku());
                });

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        product.setProductName(dto.getProductName().trim());
        product.setSku(dto.getSku().trim());
        product.setDescription(dto.getDescription());
        product.setCategory(category);
        product.setBrand(dto.getBrand());
        product.setPurchasePrice(dto.getPurchasePrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setCurrentStock(dto.getCurrentStock());
        product.setMinimumStock(dto.getMinimumStock());
        product.setMaximumStock(dto.getMaximumStock());
        product.setExpiryDate(dto.getExpiryDate());
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : product.isStatus());

        return convertToDto(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setStatus(false);
        productRepository.save(product);
    }

    public List<ProductDto> searchProducts(String keyword, Long categoryId) {
        return productRepository.searchProducts(keyword, categoryId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateProductData(ProductDto dto) {
        if (dto.getCurrentStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (dto.getMinimumStock() < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative");
        }
        if (dto.getMaximumStock() < 0) {
            throw new IllegalArgumentException("Maximum stock cannot be negative");
        }
        if (dto.getPurchasePrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Purchase price must be greater than 0");
        }
        if (dto.getSellingPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Selling price must be greater than 0");
        }
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setSku(product.getSku());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        dto.setBrand(product.getBrand());
        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setCurrentStock(product.getCurrentStock());
        dto.setMinimumStock(product.getMinimumStock());
        dto.setMaximumStock(product.getMaximumStock());
        dto.setExpiryDate(product.getExpiryDate());
        dto.setStatus(product.isStatus());
        return dto;
    }
}
