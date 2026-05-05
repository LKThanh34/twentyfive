package com.project.DuAnTotNghiep.service;

import com.project.DuAnTotNghiep.entity.Image;
import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ImageService {
    List<Image> getAllImagesByProductId(Long productId);
    void removeImageByIds(List<Long> ids);
}
