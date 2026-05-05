package com.project.DuAnTotNghiep.service.serviceImpl;

import com.project.DuAnTotNghiep.dto.DiscountCode.DiscountCodeDto;
import com.project.DuAnTotNghiep.dto.DiscountCode.SearchDiscountCodeDto;
import com.project.DuAnTotNghiep.dto.Product.SearchProductDto;
import com.project.DuAnTotNghiep.entity.Color;
import com.project.DuAnTotNghiep.entity.DiscountCode;
import com.project.DuAnTotNghiep.exception.NotFoundException;
import com.project.DuAnTotNghiep.exception.ShopApiException;
import com.project.DuAnTotNghiep.repository.DiscountCodeRepository;
import com.project.DuAnTotNghiep.repository.Specification.DiscountCodeSpec;
import com.project.DuAnTotNghiep.service.DiscountCodeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DiscountCodeImpl implements DiscountCodeService {
    private final DiscountCodeRepository discountCodeRepository;

    public DiscountCodeImpl(DiscountCodeRepository discountCodeRepository) {
        this.discountCodeRepository = discountCodeRepository;
    }

    @Override
    public Page<DiscountCodeDto> getAllDiscountCode(SearchDiscountCodeDto searchDiscountCodeDto, Pageable pageable) {
        Specification<DiscountCode> spec = new DiscountCodeSpec(searchDiscountCodeDto);
        Page<DiscountCode> discountCodes = discountCodeRepository.findAll(spec, pageable);
        return discountCodes.map(this::convertToDto);
    }

    @Override
    public DiscountCodeDto saveDiscountCode(DiscountCodeDto discountCodeDto) {
       if (discountCodeRepository.existsByCode(discountCodeDto.getCode())) {
            throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã giảm giá "+discountCodeDto.getCode()+" đã tồn tại");
        }
        DiscountCode discountCode = convertToEntity(discountCodeDto);
        discountCode.setStatus(1);
        discountCode.setDeleteFlag(false);
        DiscountCode discountCodeSave = discountCodeRepository.save(discountCode);
        return convertToDto(discountCodeSave);
    }

    @Override
    public DiscountCodeDto updateDiscountCode(DiscountCodeDto discountCodeDto) {
        DiscountCode existingColor = discountCodeRepository.findById(discountCodeDto.getId()).orElseThrow(() -> new NotFoundException("Không tìm thấy mã giảm giá " + discountCodeDto.getCode()) );
        if(!existingColor.getCode().equals(discountCodeDto.getCode())) {
            if(discountCodeRepository.existsByCode(discountCodeDto.getCode())) {
                throw new ShopApiException(HttpStatus.BAD_REQUEST, "Mã giảm giá " + discountCodeDto.getCode() + " đã tồn tại");
            }
        }

        DiscountCode discountCode = convertToEntity(discountCodeDto);
        discountCode.setStatus(1);
        discountCode.setDeleteFlag(false);
        DiscountCode discountCodeSave = discountCodeRepository.save(discountCode);
        return convertToDto(discountCodeSave);
    }

    public boolean isExistsDiscountCode(String code) {
        return discountCodeRepository.existsByCode(code);
    }

    @Override
    public DiscountCodeDto getDiscountCodeById(Long id) {
        DiscountCode discountCode = discountCodeRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy mã giảm giá"));
        return convertToDto(discountCode);
    }

    @Override
    public DiscountCodeDto getDiscountCodeByCode(Long code) {
        return null;
    }

    @Override
    public DiscountCodeDto updateStatus(Long discountCodeId, int status) {
        DiscountCode discountCode = discountCodeRepository.findById(discountCodeId).orElseThrow(() -> new NotFoundException("Không tìm thấy mã giảm giá"));
        discountCode.setStatus(status);
        return convertToDto(discountCodeRepository.save(discountCode));
    }

    @Override
    public Page<DiscountCodeDto> getAllAvailableDiscountCode(Pageable pageable) {
        Page<DiscountCode> validCodes = discountCodeRepository.findAllAvailableValid(pageable);
        return validCodes.map(this::convertToDto);
    }

    private DiscountCodeDto convertToDto(DiscountCode discountCode) {
        DiscountCodeDto dto = new DiscountCodeDto();
        dto.setId(discountCode.getId());
        dto.setCode(discountCode.getCode().trim());
        dto.setDiscountAmount(discountCode.getDiscountAmount());
        dto.setMaximumAmount(discountCode.getMaximumAmount());
        dto.setDetail(discountCode.getDetail());
        dto.setPercentage(discountCode.getPercentage());
        dto.setStartDate(discountCode.getStartDate());
        dto.setEndDate(discountCode.getEndDate());
        dto.setType(discountCode.getType());
        dto.setMinimumAmountInCart(discountCode.getMinimumAmountInCart());
        dto.setMaximumUsage(discountCode.getMaximumUsage());
        dto.setStatus(discountCode.getStatus());
        return dto;
    }

    private DiscountCode convertToEntity(DiscountCodeDto discountCodeDto) {
        DiscountCode discountCode = new DiscountCode();
        discountCode.setId(discountCodeDto.getId());
        discountCode.setCode(discountCodeDto.getCode().trim());
        discountCode.setMaximumAmount(discountCodeDto.getMaximumAmount());
        discountCode.setDiscountAmount(discountCodeDto.getDiscountAmount());
        discountCode.setDetail(discountCodeDto.getDetail());
        discountCode.setPercentage(discountCodeDto.getPercentage());
        discountCode.setStartDate(discountCodeDto.getStartDate());
        discountCode.setEndDate(discountCodeDto.getEndDate());
        discountCode.setType(discountCodeDto.getType());
        discountCode.setMinimumAmountInCart(discountCodeDto.getMinimumAmountInCart());
        discountCode.setMaximumUsage(discountCodeDto.getMaximumUsage());
        discountCode.setStatus(discountCodeDto.getStatus());
        return discountCode;
    }
}
