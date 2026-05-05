package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.service.ColorService;
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
public class ColorController {

    @Autowired
    private ColorService colorService;


    @GetMapping("/color-list")
    public String category(Model model, @RequestParam(name = "page", defaultValue = "0") int page,
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
        Page<Color> categoryPage = colorService.findAll(pageable);

        model.addAttribute("sortField", sortFieldName);
        model.addAttribute("sortDirection", sortDirection);

        model.addAttribute("items", categoryPage);

        return "admin/color";
    }

    @GetMapping("/color-create")
    public String viewAddColor(Model model){
        Color color = new Color();
        model.addAttribute("action", "/admin/color-save");
        model.addAttribute("Color", color);
        return "admin/color-create";
    }


    @PostMapping("/color-save")
    public String addColor(RedirectAttributes redirectAttributes, @Validated @ModelAttribute("Color") Color color) {
        try {
            colorService.createColor(color);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm màu mới thành công");
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/color-create";
        }
        return "redirect:/admin/color-list";
    }

    @PostMapping("/color-update/{id}")
    public String update(@PathVariable("id") Long id,
                         @Validated @ModelAttribute("Color") Color color, RedirectAttributes redirectAttributes) {
        if (colorService.existsById(id)) {
            try {
                Color color1 = colorService.updateColor(color);
                redirectAttributes.addFlashAttribute("successMessage", "Mã màu " + color1.getCode() + " cập nhật thành công");

            }catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/admin/edit-color/" + id;
            }
            return "redirect:/admin/color-list";
        } else {
            return "404";
        }
    }

    @GetMapping("/edit-color/{id}")
    public String edit(@PathVariable("id") Long id, Model model) {
        Optional<Color> optionalColor = colorService.findById(id);
        if (optionalColor.isPresent()) {
            Color color = optionalColor.get();
            model.addAttribute("Color", color);
            model.addAttribute("action", "/admin/color-update/" + color.getId());
            return "admin/color-create";
        } else {
            return "404";
        }
    }

    @GetMapping("/color-delete/{id}")
    public String delete(@PathVariable("id") Long id, ModelMap modelMap){
        colorService.delete(id);
        return "redirect:/admin/color-list";
    }
}
