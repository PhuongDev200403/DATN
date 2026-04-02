package com.buixuantruong.shopapp.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class GHNService {

    WebClient.Builder webClientBuilder;

    @Value("${ghn.api-url}")
    @NonFinal
    String apiUrl;

    @Value("${ghn.token}")
    @NonFinal
    String token;

    @Value("${ghn.shop-id}")
    @NonFinal
    String shopId;

    /**
     * Lấy danh sách Tỉnh/Thành
     */
    public List<?> getProvinces() {
        return callGHNApiList("master-data/province", null);
    }

    /**
     * Lấy danh sách Quận/Huyện theo ProvinceID
     */
    public List<?> getDistricts(Integer provinceId) {
        return callGHNApiList("master-data/district", Map.of("province_id", provinceId));
    }

    /**
     * Lấy danh sách Phường/Xã theo DistrictID
     */
    public List<?> getWards(Integer districtId) {
        return callGHNApiList("master-data/ward", Map.of("district_id", districtId));
    }

    /**
     * Tính phí vận chuyển
     * @param toDistrictId ID huyện người nhận
     * @param toWardCode   Mã xã người nhận
     * @param weight       Khối lượng (gram)
     */
    public Long calculateFee(Integer toDistrictId, String toWardCode, Integer weight) {
        Map<String, Object> body = Map.of(
                "service_type_id", 2,
                "to_district_id", toDistrictId,
                "to_ward_code", toWardCode,
                "weight", weight,
                "length", 10,
                "width", 10,
                "height", 10
        );

        Map<String, Object> response = callGHNApiMap("v2/shipping-order/fee", body);
        if (response != null && response.get("total") != null) {
            return ((Number) response.get("total")).longValue();
        }
        return 30000L; // Phí ship mặc định nếu lỗi
    }

    // ----------------------------------------------------------------
    // Internal helpers
    // ----------------------------------------------------------------

    /**
     * Dùng cho các endpoint trả về data dạng List (provinces, districts, wards)
     */
    private List<?> callGHNApiList(String endpoint, Object body) {
        try {
            Map<String, Object> fullResponse = doRequest(endpoint, body);
            if (fullResponse != null && "200".equals(String.valueOf(fullResponse.get("code")))) {
                Object data = fullResponse.get("data");
                if (data instanceof List<?> list) {
                    return list;
                }
            }
        } catch (Exception e) {
            log.error("GHN API Error at {}: {}", endpoint, e.getMessage());
        }
        return List.of();
    }

    /**
     * Dùng cho các endpoint trả về data dạng Map (fee, order...)
     */
    private Map<String, Object> callGHNApiMap(String endpoint, Object body) {
        try {
            Map<String, Object> fullResponse = doRequest(endpoint, body);
            if (fullResponse != null && "200".equals(String.valueOf(fullResponse.get("code")))) {
                Object data = fullResponse.get("data");
                if (data instanceof Map<?, ?> map) {
                    //noinspection unchecked
                    return (Map<String, Object>) map;
                }
            }
        } catch (Exception e) {
            log.error("GHN API Error at {}: {}", endpoint, e.getMessage());
        }
        return null;
    }

    /**
     * Thực hiện HTTP request tới GHN, trả về toàn bộ response body dạng Map
     */
    private Map<String, Object> doRequest(String endpoint, Object body) {
        WebClient client = webClientBuilder.build();
        WebClient.RequestHeadersSpec<?> request;

        if (body != null) {
            request = client.post()
                    .uri(apiUrl + endpoint)
                    .header("Token", token)
                    .header("ShopId", shopId)
                    .bodyValue(body);
        } else {
            request = client.get()
                    .uri(apiUrl + endpoint)
                    .header("Token", token);
        }

        return request.retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}