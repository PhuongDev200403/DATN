package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.ProductDTO;
import com.buixuantruong.shopapp.model.Product;

public interface VariantService {

    void synchronizeVariants(Product product, ProductDTO productDTO);
}
