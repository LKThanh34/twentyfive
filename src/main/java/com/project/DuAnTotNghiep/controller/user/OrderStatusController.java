package com.project.DuAnTotNghiep.controller.user;

import com.project.DuAnTotNghiep.dto.Cart.CartDto;
import com.project.DuAnTotNghiep.entity.Bill;
import com.project.DuAnTotNghiep.service.BillService;
import com.project.DuAnTotNghiep.service.CartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class OrderStatusController {
    private final BillService billService;
    private final CartService cartService;

    public OrderStatusController(BillService billService, CartService cartService) {
        this.billService = billService;
        this.cartService = cartService;
    }

    @GetMapping("/cart-status")
    public String viewCartStatus(Model model,
                                 @RequestParam(required = false) String status,
                                 @PageableDefault(size = 5, sort = "createDate", direction = Sort.Direction.DESC)  Pageable pageable) {
        Page<Bill> billPage = null;
        if(status == null || status.trim().isEmpty()) {
            billPage = billService.getBillByAccount(pageable);
        }else {
            billPage = billService.getBillByStatus(status, pageable);
            model.addAttribute("status", status);
        }

        model.addAttribute("bills", billPage);
        return "user/cart-status";
    }

    @PostMapping("/cancel-bill/{id}")
    public String cancelBill(@PathVariable Long id) {
        billService.updateStatus("HUY", id);
        return "redirect:/cart-status";
    }

    @ResponseBody
    @GetMapping("/api/getAllCart")
    public List<CartDto> getAllCart() {
        return cartService.getAllCartByAccountId();
    }

}
