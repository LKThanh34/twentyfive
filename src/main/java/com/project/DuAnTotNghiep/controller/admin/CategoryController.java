package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.Brand;
import com.project.DuAnTotNghiep.entity.Category;
import com.project.DuAnTotNghiep.service.BrandService;
import com.project.DuAnTotNghiep.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/category-all")
    public String getAllBrand(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "sort", defaultValue = "name,asc") String sortField) {
        int pageSize = 8; // Number of items per page
        String[] sortParams = sortField.split(",");
        String sortFieldName = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(sortDirection, sortFieldName);

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Category> categoryPage = categoryService.getAllCategory(pageable);

        model.addAttribute("sortField", sortFieldName);
        model.addAttribute("sortDirection", sortDirection);

        model.addAttribute("items", categoryPage);

        return "admin/category";
    }

    @GetMapping("/category-create")
    public String viewAddCategory(Model model){
        Category category = new Category();
        model.addAttribute("action", "/admin/category-save");
        model.addAttribute("Category", category);
        return "admin/category-create";
    }


    @PostMapping("/category-save")
    public String addCategory(Model model, @Validated @ModelAttribute("Category") Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm loại sản phẩm mới thành công");

        }catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/category-create";
        }
        return "redirect:/admin/category-all";
    }

    @PostMapping("/category-update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Validated @ModelAttribute("Category") Category category, RedirectAttributes redirectAttributes) {
        if (categoryService.existsById(id)) {
            try {
                Category category1 = categoryService.updateCategory(category);
                redirectAttributes.addFlashAttribute("successMessage", "Loại sản phẩm " + category1.getCode() + " được cập nhật thành công");

            }catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/admin/category-detail/" + id;
            }
            return "redirect:/admin/category-all";
        } else {
            return "404";
        }
    }

    @GetMapping("/category-detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<Category> optional = categoryService.findById(id);
        if (optional.isPresent()) {
            Category category = optional.get();
            model.addAttribute("Category", category);
            model.addAttribute("action", "/admin/category-update/" + category.getId());
            return "admin/category-create";
        } else {
            return "404";
        }
    }

    @GetMapping("/category-delete/{id}")
    public String delete(@PathVariable("id") Long id, ModelMap modelMap){
        categoryService.delete(id);
        return "redirect:/admin/category-all";
    }
}
