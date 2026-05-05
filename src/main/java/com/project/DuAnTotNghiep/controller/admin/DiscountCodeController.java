package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.dto.DiscountCode.DiscountCodeDto;
import com.project.DuAnTotNghiep.dto.DiscountCode.SearchDiscountCodeDto;
import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.entity.DiscountCode;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.service.DiscountCodeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

@Controller
public class DiscountCodeController {
    private final DiscountCodeService discountCodeService;

    public DiscountCodeController(DiscountCodeService discountCodeService) {
        this.discountCodeService = discountCodeService;
    }

    @GetMapping("/admin-only/discount-code")
    public String viewDiscountCodePage(Model model, SearchDiscountCodeDto searchDiscountCodeDto, @PageableDefault Pageable pageable) {
        Page<DiscountCodeDto> discountCodes = discountCodeService.getAllDiscountCode(searchDiscountCodeDto, pageable);
        model.addAttribute("discountCodes", discountCodes);
        model.addAttribute("dataSearch", searchDiscountCodeDto);
        model.addAttribute("totalPage", discountCodes.getTotalPages());
        model.addAttribute("currentPage", pageable.getPageNumber());
        return "/admin/discount-code";
    }

    @GetMapping("/admin-only/discount-code-create")
    public String viewDiscountCodeCreatePage(Model model) {
        DiscountCodeDto discountCodeDto = new DiscountCodeDto();
        discountCodeDto.setType(1);
        discountCodeDto.setStartDate(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        Date endDate = calendar.getTime();
        discountCodeDto.setEndDate(endDate);

        model.addAttribute("DiscountCode", discountCodeDto);
        model.addAttribute("action", "/admin/discount-code-save");
        return "/admin/discount-code-create";
    }

    @GetMapping("/admin-only/discount-code/edit/{id}")
    public String viewDiscountCodeEditPage(Model model, @PathVariable Long id) {
        try {
            DiscountCodeDto discountCodeDto = discountCodeService.getDiscountCodeById(id);
            model.addAttribute("DiscountCode", discountCodeDto);
            model.addAttribute("action", "/admin/discount-code-update");
            return "/admin/discount-code-create";
        } catch (Exception ex) {
            return "error/404";
        }
    }

    @PostMapping("/admin/discount-code-save")
    public String saveDiscountCode(Model model, RedirectAttributes redirectAttributes, @ModelAttribute("DiscountCode") DiscountCodeDto discountCodeDto) {
        try {
            DiscountCodeDto dto = discountCodeService.saveDiscountCode(discountCodeDto);
            redirectAttributes.addFlashAttribute("message", "Mã giảm giá " + dto.getCode() + " đã thêm thành công");
        } catch (ShopApiException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/admin-only/discount-code-create";
        }
        return "redirect:/admin-only/discount-code";
    }

    @PostMapping("/admin/discount-code-update")
    public String updateDiscountCode(Model model, RedirectAttributes redirectAttributes, @ModelAttribute("DiscountCode") DiscountCodeDto discountCodeDto) {
        try {
            DiscountCodeDto dto = discountCodeService.updateDiscountCode(discountCodeDto);
            redirectAttributes.addFlashAttribute("message", "Mã giảm giá " + dto.getCode() + " đã cập nhật thành công");
        } catch (ShopApiException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
            return "redirect:/admin-only/discount-code/edit/" + discountCodeDto.getId();
        }
        return "redirect:/admin-only/discount-code";
    }

    @PostMapping("/admin/update-discount-status/{status}")
    public String updateDiscountCodeStatus(Model model, RedirectAttributes redirectAttributes, @ModelAttribute("id") Long id, @PathVariable int status) {
        try {
            discountCodeService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "Mã giảm giá đã được cập nhật");
        } catch (NotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return "redirect:/admin-only/discount-code";
    }

    @ResponseBody
    @GetMapping("/api/private/discount-code")
    public Page<DiscountCodeDto> getAllDiscountCodes(SearchDiscountCodeDto searchDiscountCodeDto, Pageable pageable) {
        return discountCodeService.getAllDiscountCode(searchDiscountCodeDto, pageable);
    }

    @ResponseBody
    @GetMapping("/api/private/discount-code-valid")
    public Page<DiscountCodeDto> getAllValidDiscountCodes(Pageable pageable) {
        return discountCodeService.getAllAvailableDiscountCode(pageable);
    }

}
