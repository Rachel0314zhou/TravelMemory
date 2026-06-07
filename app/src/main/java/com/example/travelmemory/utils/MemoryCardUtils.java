package com.example.travelmemory.utils;

public class MemoryCardUtils {

    /**
     * Resolves localized travel memory easter egg text according to specified rules and priorities.
     */
    public static String getEasterEggText(int spotsCount, int photosCount, long durationDays, String highestExpenseCategory, String mostFrequentMood) {
        // Rule 1: Spots Count >= 8
        if (spotsCount >= 8) {
            return "打卡达人！这趟旅行被你走成了地图上的星光。";
        }
        // Rule 2: Highest spend is Food
        if ("餐饮".equals(highestExpenseCategory)) {
            return "这趟旅行，胃最满足。";
        }
        // Rule 3: Most frequent mood has "开心" (😊 or Chinese character)
        if (mostFrequentMood != null && (mostFrequentMood.contains("开心") || mostFrequentMood.contains("😊"))) {
            return "这是一段纯粹快乐的旅程。";
        }
        // Rule 4: Days >= 7
        if (durationDays >= 7) {
            return "一段真正深度的旅行，值得被好好收藏。";
        }
        // Rule 5: Photos >= 20
        if (photosCount >= 20) {
            return "镜头替你保存了太多闪光瞬间。";
        }
        // Default Rule
        return "每一次出发，都会变成独一无二的回忆。";
    }
}
