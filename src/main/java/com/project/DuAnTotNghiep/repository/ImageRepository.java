package com.project.DuAnTotNghiep.repository;

import com.project.DuAnTotNghiep.entity.Image;
import com.project.DuAnTotNghiep.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findAllByProduct(Product product);
    Image findImageById(Long id);
}