package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Brand.BrandDto;
import com.project.DuAnTotNghiep.entity.Brand;
import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.BrandRepository;
import com.project.DuAnTotNghiep.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;


    @Override
    public Page<Brand> getAllBrand(Pageable pageable) {
        return brandRepository.findAll(pageable);
    }

    @Override
    public Brand createBrand(Brand brand) {

        if(brand.getCode() == null) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Vui lòng nhập mã nhãn hàng");
        }
        if(brandRepository.existsByCode(brand.getCode().trim())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã nhãn hàng đã tồn tại");
        }
        brand.setStatus(1);
        brand.setDeleteFlag(false);
        return save(brand);
    }

    @Override
    public Brand updateBrand(Long id, Brand brand) {
        if(brand.getCode() == null) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Vui lòng nhập mã nhãn hàng");
        }
        Brand existingBrand = brandRepository.findById(brand.getId()).orElseThrow(null);
        if(!existingBrand.getCode().equals(brand.getCode())) {
            if(brandRepository.existsByCode(brand.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã nhãn hàng " + brand.getCode() + " đã tồn tại");
            }
        }
        brand.setDeleteFlag(false);
        return save(brand);
    }

    @Override
    public Brand save(Brand brand) {

        return brandRepository.save(brand);
    }

    @Override
    public void delete(Long id) {
        Brand brand = brandRepository.findById(id).orElseThrow(null);
        brand.setDeleteFlag(true);
        brandRepository.save(brand);
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return brandRepository.findById(id);
    }

    @Override
    public List<Brand> getAll() {
        return brandRepository.findAll();
    }

    @Override
    public BrandDto createBrandApi(BrandDto brandDto) {
        if(brandRepository.existsByCode(brandDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã nhãn hàng đã tồn tại");
        }
        Brand brand = convertToEntity(brandDto);
        brand.setStatus(1);
        brand.setDeleteFlag(false);
        Brand brandNew = brandRepository.save(brand);
        return convertToDto(brandNew);
    }

    @Override
    public boolean existsById(Long id) {
        return brandRepository.existsById(id);
    }

    private Brand convertToEntity(BrandDto brandDto) {
        Brand brand = new Brand();
        brand.setCode(brandDto.getCode());
        brand.setName(brandDto.getName());
        brand.setId(brandDto.getId());
        return brand;
    }

    private BrandDto convertToDto(Brand brand) {
        BrandDto brandDto = new BrandDto();
        brandDto.setId(brand.getId());
        brandDto.setCode(brand.getCode());
        brandDto.setName(brand.getName());
        return brandDto;
    }
}
