package com.project.DuAnTotNghiep.entity;

import com.project.DuAnTotNghiep.entity.enumClass.ClothingType;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;

    @Nationalized
    private String name;

    private LocalDateTime createDate;
    private LocalDateTime updatedDate;
    private int status;

    @Column(nullable = false)
    private boolean deleteFlag;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int gender;

    @Nationalized
    private String describe;

    @ManyToOne
    @JoinColumn(name = "categoryId")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brandId")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "materialId")
    private Material material;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> image;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductDetail> productDetails;

}