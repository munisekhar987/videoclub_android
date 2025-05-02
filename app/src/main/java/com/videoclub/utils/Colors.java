package com.videoclub.utilities;

import androidx.annotation.ColorInt;

/**
 * Colors utility class
 * Equivalent to the React Native Colors.js utility
 */
public class Colors {
    // Basic colors
    @ColorInt public static final int Black = 0xFF000000;
    @ColorInt public static final int White = 0xFFFFFFFF;
    @ColorInt public static final int Red = 0xFFFF0000;
    @ColorInt public static final int Green = 0xFF00FF00;
    @ColorInt public static final int Blue = 0xFF0000FF;
    @ColorInt public static final int Yellow = 0xFFFFEB3B;

    // UI colors
    @ColorInt public static final int PrimaryColor = 0xFFFFEB3B;  // Yellow
    @ColorInt public static final int TextColor = 0xFF212121;     // Dark text
    @ColorInt public static final int LightTextColor = 0xFF757575; // Light text

    // Status colors
    @ColorInt public static final int SuccessColor = 0xFF4CAF50; // Green
    @ColorInt public static final int ErrorColor = 0xFFF44336;  // Red
    @ColorInt public static final int WarningColor = 0xFFFF9800; // Orange

    // Background colors
    @ColorInt public static final int BackgroundDark = 0xFF212121;
    @ColorInt public static final int BackgroundLight = 0xFFF5F5F5;

    // Player specific colors
    @ColorInt public static final int PlayerControlsBackground = 0x80000000; // Semi-transparent black
}