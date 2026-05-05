package com.project.DuAnTotNghiep.controller.api;

import com.project.DuAnTotNghiep.dto.Category.CategoryDto;
import com.project.DuAnTotNghiep.service.CategoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CategoryRestController {
    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/api/category")
    public CategoryDto createCategoryApi(@RequestBody CategoryDto categoryDto) {
        return categoryService.createCategoryApi(categoryDto);
    }
}
