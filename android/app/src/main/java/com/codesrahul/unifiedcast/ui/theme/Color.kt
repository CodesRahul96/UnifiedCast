package com.codesrahul.unifiedcast.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium Sleek Modern TV Remote Palette
val DarkBackground = Color(0xFF121212)     // Pure Matte Dark
val SurfaceDark = Color(0xFF1E1E1E)        // Sleek Dark Surface
val CardBackground = Color(0xFF1E293B)     // Elevating Card Surface
val TextPrimary = Color(0xFFF8FAFC)        // Pure Crisp White
val TextSecondary = Color(0xFF94A3B8)      // Muted Cool Grey

// Modern Premium Light Theme Palette
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightCardBackground = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

// Accent Colors
val AccentCyan = Color(0xFF06B6D4)
val AccentPurple = Color(0xFF8B5CF6)
val AccentEmerald = Color(0xFF10B981)
val AccentRose = Color(0xFFF43F5E)
val AccentAmber = Color(0xFFFF9900)
val FireTvBlue = Color(0xFF1B64F2)

// Dynamic Gradients
val HeaderGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF090D16), Color(0xFF171738))
)

val HeaderGradientDark = HeaderGradient

val HeaderGradientLight = Brush.verticalGradient(
    colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
)

val PairButtonGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF9900), Color(0xFFFF5500), Color(0xFF8B5CF6))
)

val DPadOuterGradient = Brush.radialGradient(
    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
)

val OkButtonGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFF9900), Color(0xFFFF5500))
)
