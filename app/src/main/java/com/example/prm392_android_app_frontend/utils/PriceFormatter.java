package com.example.prm392_android_app_frontend.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceFormatter {
    
    private static final DecimalFormat SIMPLE_FORMAT = new DecimalFormat("###,###,###");
    private static final NumberFormat VN_FORMAT = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    
    /**
     * Định dạng giá đơn giản với dấu phẩy phân cách hàng nghìn và đơn vị "đ"
     * Ví dụ: 150000 -> "150,000đ"
     */
    public static String formatPrice(double price) {
        return SIMPLE_FORMAT.format(price) + "đ";
    }
    
    /**
     * Định dạng giá theo locale Việt Nam
     * Ví dụ: 150000 -> "150.000 ₫"
     */
    public static String formatPriceWithLocale(double price) {
        return VN_FORMAT.format(price);
    }
    
    /**
     * Định dạng giá rút gọn cho số lớn
     * Ví dụ: 1500000 -> "1.5M đ"
     */
    public static String formatPriceShort(double price) {
        if (price >= 1_000_000_000) {
            return String.format("%.1fB đ", price / 1_000_000_000);
        } else if (price >= 1_000_000) {
            return String.format("%.1fM đ", price / 1_000_000);
        } else if (price >= 1_000) {
            return String.format("%.1fK đ", price / 1_000);
        } else {
            return SIMPLE_FORMAT.format(price) + "đ";
        }
    }
    
    /**
     * Parse chuỗi giá về số
     * Ví dụ: "150,000đ" -> 150000.0
     */
    public static double parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return 0.0;
        }
        
        // Loại bỏ ký tự không phải số và dấu chấm/phẩy
        String cleanPrice = priceString.replaceAll("[^0-9.,]", "");
        
        // Xử lý dấu phẩy và chấm
        cleanPrice = cleanPrice.replace(",", "");
        
        try {
            return Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}