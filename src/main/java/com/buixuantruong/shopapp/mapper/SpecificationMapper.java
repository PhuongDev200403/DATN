package com.buixuantruong.shopapp.mapper;

import com.buixuantruong.shopapp.dto.SpecificationDTO;
import com.buixuantruong.shopapp.dto.response.SpecificationResponse;
import com.buixuantruong.shopapp.model.Specification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SpecificationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variant", ignore = true)
    Specification toSpecification(SpecificationDTO specificationDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variant", ignore = true)
    void updateSpecificationFromDto(SpecificationDTO specificationDTO, @MappingTarget Specification specification);

    SpecificationResponse toResponse(Specification specification);
}
