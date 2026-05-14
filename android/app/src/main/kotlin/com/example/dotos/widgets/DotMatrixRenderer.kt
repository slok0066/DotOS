package com.example.dotos.widgets

import android.graphics.*

/**
 * Renders text in dot-matrix style (Nothing Phone Doto font aesthetic)
 * Each character is made up of dots in a grid pattern
 */
object DotMatrixRenderer {
    
    // Dot matrix patterns for digits 0-9 and colon
    // Each digit is 5x7 dots
    private val digitPatterns = mapOf(
        '0' to arrayOf(
            " ███ ",
            "█   █",
            "█   █",
            "█   █",
            "█   █",
            "█   █",
            " ███ "
        ),
        '1' to arrayOf(
            "  █  ",
            " ██  ",
            "  █  ",
            "  █  ",
            "  █  ",
            "  █  ",
            " ███ "
        ),
        '2' to arrayOf(
            " ███ ",
            "█   █",
            "    █",
            "   █ ",
            "  █  ",
            " █   ",
            "█████"
        ),
        '3' to arrayOf(
            " ███ ",
            "█   █",
            "    █",
            "  ██ ",
            "    █",
            "█   █",
            " ███ "
        ),
        '4' to arrayOf(
            "   █ ",
            "  ██ ",
            " █ █ ",
            "█  █ ",
            "█████",
            "   █ ",
            "   █ "
        ),
        '5' to arrayOf(
            "█████",
            "█    ",
            "████ ",
            "    █",
            "    █",
            "█   █",
            " ███ "
        ),
        '6' to arrayOf(
            " ███ ",
            "█   █",
            "█    ",
            "████ ",
            "█   █",
            "█   █",
            " ███ "
        ),
        '7' to arrayOf(
            "█████",
            "    █",
            "   █ ",
            "  █  ",
            " █   ",
            " █   ",
            " █   "
        ),
        '8' to arrayOf(
            " ███ ",
            "█   █",
            "█   █",
            " ███ ",
            "█   █",
            "█   █",
            " ███ "
        ),
        '9' to arrayOf(
            " ███ ",
            "█   █",
            "█   █",
            " ████",
            "    █",
            "█   █",
            " ███ "
        ),
        ':' to arrayOf(
            "     ",
            "  █  ",
            "  █  ",
            "     ",
            "  █  ",
            "  █  ",
            "     "
        ),
        ' ' to arrayOf(
            "     ",
            "     ",
            "     ",
            "     ",
            "     ",
            "     ",
            "     "
        ),
        '%' to arrayOf(
            "██  █",
            "██ █ ",
            "  █  ",
            " █   ",
            "█  ██",
            "  ██ ",
            "     "
        ),
        '°' to arrayOf(
            " ██  ",
            "█  █ ",
            " ██  ",
            "     ",
            "     ",
            "     ",
            "     "
        ),
        'C' to arrayOf(
            " ███ ",
            "█   █",
            "█    ",
            "█    ",
            "█    ",
            "█   █",
            " ███ "
        ),
        'G' to arrayOf(
            " ███ ",
            "█   █",
            "█    ",
            "█  ██",
            "█   █",
            "█   █",
            " ███ "
        ),
        'H' to arrayOf(
            "█   █",
            "█   █",
            "█   █",
            "█████",
            "█   █",
            "█   █",
            "█   █"
        ),
        'B' to arrayOf(
            "████ ",
            "█   █",
            "█   █",
            "████ ",
            "█   █",
            "█   █",
            "████ "
        ),
        'M' to arrayOf(
            "█   █",
            "██ ██",
            "█ █ █",
            "█   █",
            "█   █",
            "█   █",
            "█   █"
        ),
        '.' to arrayOf(
            "     ",
            "     ",
            "     ",
            "     ",
            "     ",
            "  █  ",
            "  █  "
        )
    )
    
    /**
     * Renders text in dot-matrix style
     * @param canvas Canvas to draw on
     * @param text Text to render
     * @param x Starting X position
     * @param y Starting Y position (top of text)
     * @param dotSize Size of each dot
     * @param dotSpacing Spacing between dots
     * @param color Color of the dots
     */
    fun drawDotMatrixText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        dotSize: Float,
        dotSpacing: Float,
        color: Int
    ) {
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        var currentX = x
        
        for (char in text) {
            val pattern = digitPatterns[char] ?: digitPatterns[' ']!!
            
            // Draw each row of the pattern
            for (row in pattern.indices) {
                val rowPattern = pattern[row]
                for (col in rowPattern.indices) {
                    if (rowPattern[col] == '█') {
                        val dotX = currentX + (col * (dotSize + dotSpacing))
                        val dotY = y + (row * (dotSize + dotSpacing))
                        canvas.drawCircle(dotX, dotY, dotSize / 2, paint)
                    }
                }
            }
            
            // Move to next character position
            currentX += (5 * (dotSize + dotSpacing)) + (dotSpacing * 2)
        }
    }
    
    /**
     * Calculate the width of dot-matrix text
     */
    fun measureDotMatrixText(text: String, dotSize: Float, dotSpacing: Float): Float {
        val charWidth = 5 * (dotSize + dotSpacing)
        val charGap = dotSpacing * 2
        return text.length * (charWidth + charGap) - charGap
    }
}
