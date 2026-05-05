package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.dto.CustomerDto.CustomerDto;
import com.project.DuAnTotNghiep.entity.Customer;
import com.project.DuAnTotNghiep.entity.Product;
import com.project.DuAnTotNghiep.service.CustomerService;
import com.project.DuAnTotNghiep.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BuyAtTheCounterController {

    @Autowired
    ProductService productService;

    @Autowired
    CustomerService customerService;

    @GetMapping("/admin/pos")
    public String getIndex(Model model) {
        Pageable pageable1 = PageRequest.of(0, 2);
        Page<Product> productPage = productService.getAllProduct(pageable1);
        model.addAttribute("products", productPage);
        return "admin/BuyAtTheCounter";
    }

}
