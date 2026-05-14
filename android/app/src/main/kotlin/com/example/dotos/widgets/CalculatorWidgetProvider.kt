package com.example.dotos.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.dotos.R
import java.util.Stack

class CalculatorWidgetProvider : AppWidgetProvider() {
    
    companion object {
        private const val PREFS_NAME = "calculator_prefs"
        private const val ACTION_PREFIX = "com.example.dotos.action.CALC"
        private const val ACTION_NUMBER = "${ACTION_PREFIX}_NUMBER"
        private const val ACTION_OPERATOR = "${ACTION_PREFIX}_OPERATOR"
        private const val ACTION_EQUALS = "${ACTION_PREFIX}_EQUALS"
        private const val ACTION_CLEAR = "${ACTION_PREFIX}_CLEAR"
        private const val ACTION_BACKSPACE = "${ACTION_PREFIX}_BACKSPACE"
        private const val ACTION_DOT = "${ACTION_PREFIX}_DOT"
        
        fun getExpressionKey(appWidgetId: Int) = "calc_expression_$appWidgetId"
        fun getResultKey(appWidgetId: Int) = "calc_result_$appWidgetId"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action?.startsWith(ACTION_PREFIX) == true) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
            if (appWidgetId != -1) {
                handleAction(context, intent.action!!, intent.getStringExtra("value"), appWidgetId)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun handleAction(context: Context, action: String, value: String?, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expressionKey = getExpressionKey(appWidgetId)
        val resultKey = getResultKey(appWidgetId)
        val currentExpression = prefs.getString(expressionKey, "") ?: ""
        val currentResult = prefs.getString(resultKey, "0") ?: "0"
        
        when (action) {
            ACTION_NUMBER -> {
                val newExpression = if (currentExpression == "0" || currentExpression.isEmpty()) {
                    value ?: ""
                } else {
                    currentExpression + (value ?: "")
                }
                prefs.edit()
                    .putString(expressionKey, newExpression)
                    .putString(resultKey, newExpression.ifEmpty { "0" })
                    .apply()
            }
            ACTION_OPERATOR -> {
                val newExpression = if (currentExpression.isEmpty()) {
                    currentResult + (value ?: "")
                } else {
                    currentExpression + (value ?: "")
                }
                prefs.edit()
                    .putString(expressionKey, newExpression)
                    .apply()
            }
            ACTION_DOT -> {
                val newExpression = if (currentExpression.isEmpty()) {
                    "0."
                } else if (!currentExpression.contains(".")) {
                    "$currentExpression."
                } else {
                    currentExpression
                }
                prefs.edit()
                    .putString(expressionKey, newExpression)
                    .putString(resultKey, newExpression)
                    .apply()
            }
            ACTION_EQUALS -> {
                try {
                    val result = evaluateExpression(currentExpression)
                    prefs.edit()
                        .putString(expressionKey, "")
                        .putString(resultKey, result)
                        .apply()
                } catch (e: Exception) {
                    prefs.edit()
                        .putString(resultKey, "Error")
                        .apply()
                }
            }
            ACTION_CLEAR -> {
                prefs.edit()
                    .putString(expressionKey, "")
                    .putString(resultKey, "0")
                    .apply()
            }
            ACTION_BACKSPACE -> {
                val newExpression = if (currentExpression.isNotEmpty()) {
                    currentExpression.dropLast(1)
                } else {
                    ""
                }
                prefs.edit()
                    .putString(expressionKey, newExpression)
                    .putString(resultKey, newExpression.ifEmpty { "0" })
                    .apply()
            }
        }
    }

    private fun evaluateExpression(expression: String): String {
        if (expression.isEmpty()) return "0"
        
        try {
            // Replace X with * for multiplication
            val normalizedExpression = expression.replace("X", "*").replace("x", "*")
            
            // Simple evaluation using stack-based approach
            val tokens = tokenize(normalizedExpression)
            val result = calculate(tokens)
            
            // Format result
            return if (result % 1.0 == 0.0) {
                result.toInt().toString()
            } else {
                String.format("%.2f", result).trimEnd('0').trimEnd('.')
            }
        } catch (e: Exception) {
            return "Error"
        }
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()
        
        for (char in expression) {
            when (char) {
                in '0'..'9', '.' -> currentNumber.append(char)
                '+', '-', '*', '/' -> {
                    if (currentNumber.isNotEmpty()) {
                        tokens.add(currentNumber.toString())
                        currentNumber.clear()
                    }
                    tokens.add(char.toString())
                }
            }
        }
        
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }
        
        return tokens
    }

    private fun calculate(tokens: List<String>): Double {
        if (tokens.isEmpty()) return 0.0
        if (tokens.size == 1) return tokens[0].toDoubleOrNull() ?: 0.0
        
        // Handle multiplication and division first
        val processedTokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if (i + 2 < tokens.size && (tokens[i + 1] == "*" || tokens[i + 1] == "/")) {
                val left = tokens[i].toDoubleOrNull() ?: 0.0
                val right = tokens[i + 2].toDoubleOrNull() ?: 0.0
                val result = if (tokens[i + 1] == "*") left * right else left / right
                processedTokens.add(result.toString())
                i += 3
            } else {
                processedTokens.add(tokens[i])
                i++
            }
        }
        
        // Handle addition and subtraction
        var result = processedTokens[0].toDoubleOrNull() ?: 0.0
        i = 1
        while (i < processedTokens.size) {
            if (i + 1 < processedTokens.size) {
                val operator = processedTokens[i]
                val operand = processedTokens[i + 1].toDoubleOrNull() ?: 0.0
                result = when (operator) {
                    "+" -> result + operand
                    "-" -> result - operand
                    else -> result
                }
                i += 2
            } else {
                i++
            }
        }
        
        return result
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val expression = prefs.getString(getExpressionKey(appWidgetId), "") ?: ""
        val result = prefs.getString(getResultKey(appWidgetId), "0") ?: "0"
        
        val views = RemoteViews(context.packageName, R.layout.widget_calculator_layout)
        views.setTextViewText(R.id.calc_expression, expression)
        views.setTextViewText(R.id.calc_result, result)
        
        // Number buttons
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_0, "0")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_1, "1")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_2, "2")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_3, "3")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_4, "4")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_5, "5")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_6, "6")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_7, "7")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_8, "8")
        setNumberButton(context, views, appWidgetId, R.id.calc_btn_9, "9")
        
        // Operator buttons
        setOperatorButton(context, views, appWidgetId, R.id.calc_btn_add, "+")
        setOperatorButton(context, views, appWidgetId, R.id.calc_btn_sub, "-")
        setOperatorButton(context, views, appWidgetId, R.id.calc_btn_mul, "X")
        setOperatorButton(context, views, appWidgetId, R.id.calc_btn_div, "/")
        
        // Special buttons
        setDotButton(context, views, appWidgetId)
        setEqualsButton(context, views, appWidgetId)
        setClearButton(context, views, appWidgetId)
        setBackspaceButton(context, views, appWidgetId)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun setNumberButton(context: Context, views: RemoteViews, appWidgetId: Int, buttonId: Int, value: String) {
        val intent = Intent(context, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_NUMBER
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("value", value)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, appWidgetId * 100 + value.toInt(), intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(buttonId, pendingIntent)
    }

    private fun setOperatorButton(context: Context, views: RemoteViews, appWidgetId: Int, buttonId: Int, operator: String) {
        val intent = Intent(context, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_OPERATOR
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("value", operator)
        }
        val requestCode = appWidgetId * 100 + when(operator) {
            "+" -> 10
            "-" -> 11
            "X" -> 12
            "/" -> 13
            else -> 14
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, requestCode, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(buttonId, pendingIntent)
    }

    private fun setDotButton(context: Context, views: RemoteViews, appWidgetId: Int) {
        val intent = Intent(context, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_DOT
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, appWidgetId * 100 + 15, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.calc_btn_dot, pendingIntent)
    }

    private fun setEqualsButton(context: Context, views: RemoteViews, appWidgetId: Int) {
        val intent = Intent(context, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_EQUALS
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, appWidgetId * 100 + 16, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.calc_btn_equals, pendingIntent)
    }

    private fun setClearButton(context: Context, views: RemoteViews, appWidgetId: Int) {
        val intent = Intent(context, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_CLEAR
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, appWidgetId * 100 + 17, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.calc_clear, pendingIntent)
    }

    private fun setBackspaceButton(context: Context, views: RemoteViews, appWidgetId: Int) {
        val intent = Intent(context, CalculatorWidgetProvider::class.java).apply {
            action = ACTION_BACKSPACE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context, appWidgetId * 100 + 18, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.calc_backspace, pendingIntent)
    }
}
