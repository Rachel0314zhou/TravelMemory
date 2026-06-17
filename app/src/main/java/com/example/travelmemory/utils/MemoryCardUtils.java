package com.example.travelmemory.utils;

public class MemoryCardUtils {

    public static String getEasterEggText(int spotsCount, int photosCount, long durationDays,
                                          String highestExpenseCategory, String mostFrequentMood) {
        return getEasterEggText(0, 0, 0, highestExpenseCategory, mostFrequentMood);
    }

    public static String getEasterEggText(int planCompletionRate, int regretCount, int packingCompletionRate,
                                          String highestExpenseCategory, String mostFrequentMood) {
        if (planCompletionRate >= 90) {
            return "\u8fd9\u8d9f\u65c5\u884c\u51e0\u4e4e\u5b8c\u6574\u5b9e\u73b0\u4e86\u6700\u521d\u7684\u671f\u5f85\u3002";
        }
        if (regretCount >= 3) {
            return "\u8fd8\u6709\u4e00\u4e9b\u6ca1\u5b8c\u6210\u7684\u5fc3\u613f\uff0c\u6b63\u597d\u6210\u4e3a\u4e0b\u4e00\u6b21\u51fa\u53d1\u7684\u7406\u7531\u3002";
        }
        if (packingCompletionRate == 100) {
            return "\u51fa\u53d1\u524d\u7684\u51c6\u5907\uff0c\u4e5f\u662f\u4e00\u79cd\u8ba4\u771f\u751f\u6d3b\u7684\u4eea\u5f0f\u611f\u3002";
        }
        if ("\u9910\u996e".equals(highestExpenseCategory)) {
            return "\u8fd9\u8d9f\u65c5\u884c\uff0c\u80c3\u6700\u6ee1\u8db3\u3002";
        }
        if (mostFrequentMood != null && mostFrequentMood.contains("\u5f00\u5fc3")) {
            return "\u8fd9\u662f\u4e00\u6bb5\u7eaf\u7cb9\u5feb\u4e50\u7684\u65c5\u7a0b\u3002";
        }
        return "\u6bcf\u4e00\u6b21\u51fa\u53d1\uff0c\u90fd\u4f1a\u53d8\u6210\u72ec\u4e00\u65e0\u4e8c\u7684\u56de\u5fc6\u3002";
    }
}
