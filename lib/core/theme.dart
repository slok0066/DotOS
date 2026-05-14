import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class NothingTheme {
  // Colors - Dark Mode (Default)
  static const Color black = Color(0xFF000000);
  static const Color surface = Color(0xFF111111);
  static const Color surfaceRaised = Color(0xFF1A1A1A);
  static const Color border = Color(0xFF222222);
  static const Color borderVisible = Color(0xFF333333);
  
  static const Color textDisabled = Color(0xFF666666);
  static const Color textSecondary = Color(0xFF999999);
  static const Color textPrimary = Color(0xFFE8E8E8);
  static const Color textDisplay = Color(0xFFFFFFFF);
  
  static const Color accent = Color(0xFFD71921);
  static const Color success = Color(0xFF4A9E5C);
  static const Color warning = Color(0xFFD4A843);

  // Spacing
  static const double spaceXS = 4.0;
  static const double spaceSM = 8.0;
  static const double spaceMD = 16.0;
  static const double spaceLG = 24.0;
  static const double spaceXL = 32.0;
  static const double space2XL = 48.0;
  static const double space3XL = 64.0;
  static const double space4XL = 96.0;

  static ThemeData darkTheme() {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      scaffoldBackgroundColor: black,
      colorScheme: const ColorScheme.dark(
        primary: textDisplay,
        surface: surface,
        onSurface: textPrimary,
        outline: borderVisible,
      ),
      textTheme: TextTheme(
        displayLarge: GoogleFonts.spaceGrotesk(
          fontSize: 72,
          fontWeight: FontWeight.w400,
          color: textDisplay,
          letterSpacing: -2.16,
        ),
        displayMedium: GoogleFonts.spaceGrotesk(
          fontSize: 48,
          fontWeight: FontWeight.w400,
          color: textDisplay,
          letterSpacing: -0.96,
        ),
        headlineMedium: GoogleFonts.spaceGrotesk(
          fontSize: 24,
          fontWeight: FontWeight.w400,
          color: textPrimary,
        ),
        bodyLarge: GoogleFonts.spaceGrotesk(
          fontSize: 16,
          fontWeight: FontWeight.w400,
          color: textPrimary,
        ),
        bodySmall: GoogleFonts.spaceMono(
          fontSize: 12,
          fontWeight: FontWeight.w400,
          color: textSecondary,
          letterSpacing: 0.48,
        ),
        labelSmall: GoogleFonts.spaceMono(
          fontSize: 11,
          fontWeight: FontWeight.w400,
          color: textSecondary,
          letterSpacing: 0.88,
        ),
      ),
    );
  }

  static ThemeData lightTheme() {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
      scaffoldBackgroundColor: const Color(0xFFF5F5F0),
      colorScheme: const ColorScheme.light(
        primary: Color(0xFF000000),
        surface: Color(0xFFFFFFFF),
        onSurface: Color(0xFF000000),
        outline: Color(0xFFDDDDDD),
      ),
      textTheme: TextTheme(
        displayLarge: GoogleFonts.spaceGrotesk(
          fontSize: 72,
          fontWeight: FontWeight.w400,
          color: const Color(0xFF000000),
          letterSpacing: -2.16,
        ),
        displayMedium: GoogleFonts.spaceGrotesk(
          fontSize: 48,
          fontWeight: FontWeight.w400,
          color: const Color(0xFF000000),
          letterSpacing: -0.96,
        ),
        headlineMedium: GoogleFonts.spaceGrotesk(
          fontSize: 24,
          fontWeight: FontWeight.w400,
          color: const Color(0xFF000000),
        ),
        bodyLarge: GoogleFonts.spaceGrotesk(
          fontSize: 16,
          fontWeight: FontWeight.w400,
          color: const Color(0xFF000000),
        ),
        bodySmall: GoogleFonts.spaceMono(
          fontSize: 12,
          fontWeight: FontWeight.w400,
          color: const Color(0xFF666666),
          letterSpacing: 0.48,
        ),
        labelSmall: GoogleFonts.spaceMono(
          fontSize: 11,
          fontWeight: FontWeight.w400,
          color: const Color(0xFF666666),
          letterSpacing: 0.88,
        ),
      ),
    );
  }

  static TextStyle doto({
    double fontSize = 72,
    Color color = textDisplay,
    double? letterSpacing,
  }) {
    try {
      return GoogleFonts.getFont(
        'Doto',
        fontSize: fontSize,
        fontWeight: FontWeight.w400,
        color: color,
        letterSpacing: letterSpacing ?? (-0.03 * fontSize),
      );
    } catch (e) {
      // Fallback to Space Mono if Doto is not found
      return GoogleFonts.spaceMono(
        fontSize: fontSize,
        fontWeight: FontWeight.w400,
        color: color,
        letterSpacing: letterSpacing ?? (-0.03 * fontSize),
      );
    }
  }

  static TextStyle mono({
    double fontSize = 11,
    Color color = textSecondary,
    bool allCaps = true,
  }) {
    return GoogleFonts.spaceMono(
      fontSize: fontSize,
      fontWeight: FontWeight.w400,
      color: color,
      letterSpacing: 0.08 * fontSize,
    );
  }

  static TextStyle grotesk({
    double fontSize = 16,
    Color color = textPrimary,
    FontWeight fontWeight = FontWeight.w400,
    double? letterSpacing,
  }) {
    return GoogleFonts.spaceGrotesk(
      fontSize: fontSize,
      fontWeight: fontWeight,
      color: color,
      letterSpacing: letterSpacing,
    );
  }
}
