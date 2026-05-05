package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Category.CategoryDto;
import com.project.DuAnTotNghiep.entity.Category;
import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.CategoryRepository;
import com.project.DuAnTotNghiep.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<Category> getAllCategory(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category createCategory(Category category) {
        if(categoryRepository.existsByCode(category.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại " + category.getCode() + " đã tồn tại");
        }
        category.setDeleteFlag(false);
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        Category existingCategory = categoryRepository.findById(category.getId()).orElseThrow(null);
        if(!existingCategory.getCode().equals(category.getCode())) {
            if(categoryRepository.existsByCode(category.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại " + category.getCode() + " đã tồn tại");
            }
        }
        category.setDeleteFlag(false);
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(null);
        category.setDeleteFlag(true);
        categoryRepository.save(category);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryRepository.existsById(id);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Override
    public CategoryDto createCategoryApi(CategoryDto categoryDto) {
        if(categoryRepository.existsByCode(categoryDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã loại đã tồn tại");
        }
        Category category = new Category(null, categoryDto.getCode(), categoryDto.getName(), 1, false);
        Category categoryNew = categoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getCode(), category.getName());
    }
}
