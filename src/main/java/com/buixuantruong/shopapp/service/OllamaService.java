package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.AnalyticsOverviewResponse;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Variant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OllamaService implements AiTextService {

    WebClient.Builder webClientBuilder;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    @NonFinal
    String baseUrl;

    @Value("${ai.ollama.model:llama3.2}")
    @NonFinal
    String model;

    @Override
    public String analyzeShopData(AnalyticsOverviewResponse data) {
        return callModel(buildAnalyticsPrompt(data));
    }

    @Override
    public String getRecommendations(String query, List<Product> products) {
        return callModel(buildSearchPrompt(query, products));
    }

    @Override
    public String generateProductDescription(String productName, String categoryName, String specs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ban la mot chuyen gia copywriter cho thuong mai dien tu.\n");
        sb.append("Nhiem vu: viet mo ta san pham hap dan, chuan SEO cho san pham sau:\n\n");
        sb.append("- Ten san pham: ").append(productName).append("\n");
        sb.append("- Danh muc: ").append(categoryName).append("\n");
        if (specs != null && !specs.isBlank()) {
            sb.append("- Thong so ky thuat/Dac diem: ").append(specs).append("\n");
        }
        sb.append("\nYeu cau bai viet:\n");
        sb.append("1. Giong van chuyen nghiep, tap trung vao loi ich khach hang.\n");
        sb.append("2. Cau truc ro rang: gioi thieu ngan, dac diem noi bat, thong so ky thuat, loi ket.\n");
        sb.append("3. Dinh dang bang Markdown.\n");
        sb.append("4. Tra loi bang tieng Viet.\n");
        return callModel(sb.toString());
    }

    @Override
    public String getSupportResponse(String query, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ban la tro ly ao ho tro khach hang cua cua hang truc tuyen ShopApp.\n");
        sb.append("Can tra loi lich su, than thien va chinh xac dua tren thong tin duoc cung cap.\n\n");
        sb.append("=== THONG TIN NGU CANH ===\n");
        sb.append(context).append("\n\n");
        sb.append("=== CAU HOI KHACH HANG ===\n");
        sb.append('"').append(query).append('"').append("\n\n");
        sb.append("=== HUONG DAN ===\n");
        sb.append("1. Neu du thong tin thi tra loi truc tiep, ro rang.\n");
        sb.append("2. Neu thieu thong tin thi yeu cau khach cung cap them hoac lien he hotline 1900-1234.\n");
        sb.append("3. Khong tu dat thong tin khong co trong ngu canh.\n");
        sb.append("4. Tra loi bang tieng Viet, co the dung Markdown.\n");
        return callModel(sb.toString());
    }

    private String callModel(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false
            );

            Map<?, ?> response = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/api/generate")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Error calling Ollama API: {}", e.getMessage());
            return "Khong the ket noi AI luc nay. Vui long thu lai sau.";
        }
    }

    private String buildSearchPrompt(String query, List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ban la tro ly mua sam AI cho mot cua hang thuong mai dien tu.\n");
        sb.append("Nhiem vu la tu van san pham dua tren nhu cau khach hang.\n\n");
        sb.append("=== YEU CAU KHACH HANG ===\n");
        sb.append('"').append(query).append('"').append("\n\n");
        sb.append("=== DANH SACH SAN PHAM ===\n");

        products.forEach(product -> sb.append(String.format(
                "- ID: %d | Ten: %s | Gia: %,.0f VND | Mo ta: %s%n",
                product.getId(),
                product.getName(),
                resolveProductPrice(product),
                product.getDescription() != null ? product.getDescription() : "Khong co mo ta"
        )));

        sb.append("\n=== HUONG DAN ===\n");
        sb.append("1. Phan tich nhu cau khach hang.\n");
        sb.append("2. Chon 3-5 san pham phu hop nhat.\n");
        sb.append("3. Giai thich ngan gon vi sao phu hop.\n");
        sb.append("4. Neu khong co san pham phu hop, noi ro va de xuat lua chon gan nhat.\n");
        sb.append("5. Tra loi bang tieng Viet, phong cach than thien, chuyen nghiep, dung Markdown.\n");
        return sb.toString();
    }

    private String buildAnalyticsPrompt(AnalyticsOverviewResponse data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ban la chuyen gia phan tich kinh doanh cho mot cua hang thuong mai dien tu tai Viet Nam.\n");
        sb.append("Dua vao du lieu ben duoi, hay dua ra:\n");
        sb.append("1. Nhan xet tong quan ve tinh hinh kinh doanh trong 2-3 cau.\n");
        sb.append("2. Diem tich cuc va diem can cai thien.\n");
        sb.append("3. Top 3 hanh dong cu the cho admin.\n\n");
        sb.append("=== DU LIEU THONG KE ===\n");
        sb.append(String.format("Tong doanh thu: %,d VND%n", defaultLong(data.getTotalRevenue())));
        sb.append(String.format("Doanh thu thang nay: %,d VND%n", defaultLong(data.getRevenueThisMonth())));
        sb.append(String.format("Doanh thu thang truoc: %,d VND%n", defaultLong(data.getRevenueLastMonth())));
        sb.append(String.format("Tang truong doanh thu: %.1f%%%n", data.getRevenueGrowthRate()));
        sb.append(String.format("Tong don hang: %d%n", defaultLong(data.getTotalOrders())));
        sb.append(String.format("Don hang thang nay: %d%n", defaultLong(data.getOrdersThisMonth())));
        if (data.getOrdersByStatus() != null) {
            sb.append("Trang thai don hang: ").append(data.getOrdersByStatus()).append("\n");
        }
        sb.append(String.format("Danh gia trung binh: %.1f/5 (%d danh gia)%n",
                defaultDouble(data.getAverageRating()),
                defaultLong(data.getTotalReviews())));
        if (data.getTopSellingProducts() != null && !data.getTopSellingProducts().isEmpty()) {
            sb.append("Top san pham ban chay:\n");
            data.getTopSellingProducts().forEach(product ->
                    sb.append(String.format("  - %s: %d san pham, %,d VND doanh thu%n",
                            product.getProductName(),
                            defaultLong(product.getTotalSold()),
                            defaultLong(product.getTotalRevenue())))
            );
        }
        sb.append("\nTra loi bang tieng Viet, ngan gon va thuc te.\n");
        return sb.toString();
    }

    private String extractTextFromResponse(Map<?, ?> response) {
        if (response == null) {
            return "AI khong tra ve phan hoi.";
        }
        Object content = response.get("response");
        if (content instanceof String text && !text.isBlank()) {
            return text;
        }
        return "AI khong co phan hoi.";
    }

    private long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private float resolveProductPrice(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return 0F;
        }
        BigDecimal minPrice = product.getVariants().stream()
                .filter(variant -> !Boolean.FALSE.equals(variant.getIsActive()))
                .map(Variant::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        return minPrice.floatValue();
    }
}
