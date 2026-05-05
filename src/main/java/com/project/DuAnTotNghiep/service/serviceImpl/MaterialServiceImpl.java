package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.Material.MaterialDto;
import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.entity.Material;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.MaterialRepository;
import com.project.DuAnTotNghiep.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialServiceImpl implements MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Override
    public Page<Material> getAllMaterial(Pageable pageable) {
        return materialRepository.findAll(pageable);
    }

    @Override
    public Material save(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public Material createMaterial(Material material) {
        if(materialRepository                                                                                                                                                                           .existsByCode(material.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã chất liệu " + material.getCode() + " đã tồn tại");
        }
        material.setDeleteFlag(false);
        return materialRepository.save(material);
    }

    @Override
    public Material updateMaterial(Material material) {
        Material existingMaterial = materialRepository.findById(material.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy chất liệu"));
        if(!existingMaterial.getCode().equals(material.getCode())) {
            if(materialRepository.existsByCode(material.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã chất liệu " + material.getCode() + " đã tồn tại");
            }
        }
        material.setDeleteFlag(false);
        return materialRepository.save(material);
    }

    @Override
    public void delete(Long id) {
        Material material = materialRepository.findById(id).orElseThrow(null);
        material.setDeleteFlag(true);
        materialRepository.save(material);
    }

    @Override
    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    @Override
    public List<Material> getAll() {
        return materialRepository.findAll();
    }

    @Override
    public MaterialDto createMaterialApi(MaterialDto materialDto) {
        if(materialRepository.existsByCode(materialDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã chất liệu đã tồn tại");
        }
        Material material = new Material(null, materialDto.getCode(), materialDto.getName(), 1, false);
        Material materialNew = materialRepository.save(material);
        return new MaterialDto(materialNew.getId(), material.getCode(), material.getName());
    }
}
