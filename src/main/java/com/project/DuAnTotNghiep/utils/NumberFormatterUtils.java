package com.project.DuAnTotNghiep.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class NumberFormatterUtils {
    public static String formatNumber(double number) {
        NumberFormat nf = new DecimalFormat("##.###");
        return nf.format(number);
    }
}
