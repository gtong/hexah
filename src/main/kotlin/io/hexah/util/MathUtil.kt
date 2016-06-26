package io.hexah.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

fun median(sorted: List<Int>): Int {
    if (sorted.size % 2 == 0) {
        val mid = sorted.size / 2 - 1
        return Math.round((sorted[mid] + sorted[mid + 1]) / 2.0).toInt()
    } else {
        return sorted[sorted.size / 2]
    }
}

fun averageInt(values: List<Int>): Double {
    if (values.size == 0) {
        return 0.0
    }
    return values.sum().toDouble() / values.size
}

fun averageDouble(values: List<Double>): Double {
    if (values.size == 0) {
        return 0.0
    }
    return values.sum() / values.size
}

fun round(value: Double): BigDecimal {
    val format = DecimalFormat("#.0000")
    format.roundingMode = RoundingMode.HALF_UP
    return BigDecimal(format.format(value))
}

