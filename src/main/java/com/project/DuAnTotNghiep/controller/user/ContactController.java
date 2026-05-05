package com.project.DuAnTotNghiep.controller.user;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ContactController {

    @GetMapping("getcontact")
    public String getContact(Model model) {

        return "user/contact";    }
}
