package com.project.DuAnTotNghiep.controller.admin;

import com.project.DuAnTotNghiep.entity.Image;
import com.project.DuAnTotNghiep.service.ImageService;
import javassist.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/admin/product/{id}/images")
    List<Image> getAllImagesByProductId(@PathVariable Long id) throws NotFoundException {
        return imageService.getAllImagesByProductId(id);
    }
}
