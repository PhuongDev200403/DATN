package com.buixuantruong.shopapp.service;

import com.buixuantruong.shopapp.dto.response.AnalyticsOverviewResponse;
import com.buixuantruong.shopapp.model.Product;
import com.buixuantruong.shopapp.model.Variant;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SpringAiTextService implements AiTextService {

    private final ChatClient chatClient;

    public SpringAiTextService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String analyzeShopData(AnalyticsOverviewResponse data) {
        String prompt = buildAnalyticsPrompt(data);
        return callModel(prompt);
    }

    @Override
    public String getRecommendations(String query, List<Product> products) {
        String prompt = buildSearchPrompt(query, products);
        return callModel(prompt);
    }

    @Override
    public String generateProductDescription(String productName, String categoryName, String specs) {
        String prompt = buildProductDescriptionPrompt(productName, categoryName, specs);
        return callModel(prompt);
    }

    @Override
    public String getSupportResponse(String query, String context) {
        String prompt = buildSupportPrompt(query, context);
        return callModel(prompt);
    }

    private String callModel(String prompt) {
        String response = chatClient.prompt()
                .system("""
                        You are a helpful AI assistant for a Vietnamese e-commerce platform.
                        Respond in Vietnamese unless the user explicitly asks otherwise.
                        Keep answers practical, concise, and accurate.
                        """)
                .user(prompt)
                .call()
                .content();
        return response == null ? "" : response.trim();
    }

    private String buildAnalyticsPrompt(AnalyticsOverviewResponse data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Phan tich kinh doanh tu du lieu sau:\n\n");
        sb.append(String.format("Tong doanh thu: %,d VND%n", defaultLong(data.getTotalRevenue())));
        sb.append(String.format("Doanh thu thang nay: %,d VND%n", defaultLong(data.getRevenueThisMonth())));
        sb.append(String.format("Doanh thu thang truoc: %,d VND%n", defaultLong(data.getRevenueLastMonth())));
        sb.append(String.format("Tang truong doanh thu: %.1f%%%n", defaultDouble(data.getRevenueGrowthRate())));
        sb.append(String.format("Tong don hang: %d%n", defaultLong(data.getTotalOrders())));
        sb.append(String.format("Don hang thang nay: %d%n", defaultLong(data.getOrdersThisMonth())));
        if (data.getOrdersByStatus() != null) {
            sb.append("Trang thai don hang: ").append(data.getOrdersByStatus()).append('\n');
        }
        if (data.getTopSellingProducts() != null && !data.getTopSellingProducts().isEmpty()) {
            sb.append("Top san pham ban chay:\n");
            data.getTopSellingProducts().forEach(product ->
                    sb.append(String.format(
                            "- %s | totalSold=%d | totalRevenue=%,d VND%n",
                            product.getProductName(),
                            defaultLong(product.getTotalSold()),
                            defaultLong(product.getTotalRevenue())
                    )));
        }
        sb.append(String.format("Danh gia trung binh: %.1f/5%n", defaultDouble(data.getAverageRating())));
        sb.append(String.format("Tong so danh gia: %d%n", defaultLong(data.getTotalReviews())));
        sb.append("""

                Yeu cau:
                1. Neu ra nhan xet tong quan 2-3 cau.
                2. Neu ra diem tich cuc va diem can cai thien.
                3. De xuat 3 hanh dong cu the cho admin.
                4. Tra loi bang tieng Viet, co dinh dang Markdown.
                """);
        return sb.toString();
    }

    private String buildSearchPrompt(String query, List<Product> products) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tim san pham phu hop voi nhu cau sau:\n");
        sb.append(query == null ? "" : query.trim()).append("\n\n");
        sb.append("Danh sach san pham:\n");

        products.stream()
                .limit(20)
                .forEach(product -> sb.append(String.format(
                        "- ID: %d | Ten: %s | Gia: %s | Mo ta: %s%n",
                        product.getId(),
                        safe(product.getName()),
                        formatPrice(resolveProductPrice(product)),
                        safe(product.getDescription())
                )));

        sb.append("""

                Yeu cau:
                1. Chon 3-5 san pham phu hop nhat.
                2. Giai thich ngan gon vi sao phu hop.
                3. Neu khong co san pham phu hop thi noi ro.
                4. Tra loi bang tieng Viet, dung Markdown.
                """);
        return sb.toString();
    }

    private String buildProductDescriptionPrompt(String productName, String categoryName, String specs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hay viet mo ta san pham va goi y thong so ky thuat cho san pham sau:\n\n");
        sb.append("- Ten san pham: ").append(safe(productName)).append('\n');
        sb.append("- Danh muc: ").append(safe(categoryName)).append('\n');
        if (specs != null && !specs.isBlank()) {
            sb.append("- Thong tin da co: ").append(specs.trim()).append('\n');
        }
        sb.append("""

                Yeu cau:
                1. Viet theo phong cach thuong mai dien tu, chuyen nghiep.
                2. Tao ra mo ta san pham hoan chinh, ngan gon nhung day du.
                3. Neu thong tin ky thuat con thieu, hay goi y them cac thong so hop ly.
                4. Tieu de ro rang, co the dung Markdown.
                5. Tra loi bang tieng Viet.
                """);
        return sb.toString();
    }

    private String buildSupportPrompt(String query, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Thong tin can canh:\n");
        sb.append(context == null ? "" : context.trim()).append("\n\n");
        sb.append("Cau hoi khach hang:\n");
        sb.append(query == null ? "" : query.trim()).append("\n\n");
        sb.append("""

                Yeu cau:
                1. Tra loi ngan gon, lich su va chinh xac.
                2. Chi dua ra thong tin co trong canh.
                3. Neu thieu thong tin, yeu cau khach bo sung hoac lien he hotline.
                4. Tra loi bang tieng Viet.
                """);
        return sb.toString();
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0d : value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatPrice(float price) {
        if (price <= 0) {
            return "0 VND";
        }
        return String.format("%,.0f VND", price);
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
