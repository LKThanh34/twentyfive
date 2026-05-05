package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.Brand;
import com.project.DuAnTotNghiep.entity.Size;
import com.project.DuAnTotNghiep.service.BrandService;
import com.project.DuAnTotNghiep.service.SizeService;
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
public class SizeController {

    @Autowired
    private SizeService sizeService;

    @GetMapping("/size-all")
    public String getAllBrand(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "sort", defaultValue = "name,asc") String sortField) {
        int pageSize = 5; // Number of items per page
        String[] sortParams = sortField.split(",");
        String sortFieldName = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.ASC;

        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
            sortDirection = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(sortDirection, sortFieldName);

        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Size> sizePage = sizeService.getAllSize(pageable);

        model.addAttribute("sortField", sortFieldName);
        model.addAttribute("sortDirection", sortDirection);

        model.addAttribute("items", sizePage);

        return "admin/size";
    }

    @GetMapping("/size-create")
    public String viewAddBrand(Model model){
        Size size = new Size();
        model.addAttribute("action", "/admin/size-save");
        model.addAttribute("Size", size);
        return "admin/size-create";
    }


    @PostMapping("/size-save")
    public String addSize(Model model, @Validated @ModelAttribute("Size") Size size, RedirectAttributes redirectAttributes) {
        try {
            Size sizeNew = sizeService.createSize(size);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm kích cỡ mới thành công");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/size-create";
        }
        return "redirect:/admin/size-all";
    }

    @PostMapping("/size-update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Validated @ModelAttribute("Brand") Size size, RedirectAttributes redirectAttributes) {
        Optional<Size> optional = sizeService.findById(id);
        if (optional.isPresent()) {
            try {
                sizeService.updateSize(size);
                redirectAttributes.addFlashAttribute("successMessage", "Kích cỡ được cập nhật thành công");

            }catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/admin/size-detail/" + id;
            }
            return "redirect:/admin/size-all";
        } else {
            return "404";
        }
    }

    @GetMapping("/size-detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Optional<Size> optional = sizeService.findById(id);
        if (optional.isPresent()) {
            Size size = optional.get();
            model.addAttribute("Size", size);
            model.addAttribute("action", "/admin/size-update/" + size.getId());
            return "admin/size-create";
        } else {
            return null;
        }
    }

    @GetMapping("/size-delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model){
        sizeService.delete(id);
        model.addAttribute("successMessage", "Xóa kích cỡ thành công");
        return "redirect:/admin/size-all";
    }
}
