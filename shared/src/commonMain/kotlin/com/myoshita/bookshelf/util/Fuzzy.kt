package com.myoshita.bookshelf.util

object Fuzzy {
    fun jaroWinklerSimilarity(s1: String, s2: String): Double {
        val s1Length = s1.length
        val s2Length = s2.length

        if (s1Length == 0 && s2Length == 0) return 1.0
        if (s1Length == 0 || s2Length == 0) return 0.0

        val matchDistance = (kotlin.math.max(s1Length, s2Length) / 2) - 1
        val s1Matches = BooleanArray(s1Length)
        val s2Matches = BooleanArray(s2Length)

        var matches = 0
        for (i in s1.indices) {
            val start = kotlin.math.max(0, i - matchDistance)
            val end = kotlin.math.min(i + matchDistance + 1, s2Length)
            for (j in start until end) {
                if (s2Matches[j]) continue
                if (s1[i] == s2[j]) {
                    s1Matches[i] = true
                    s2Matches[j] = true
                    matches++
                    break
                }
            }
        }

        if (matches == 0) return 0.0

        var transpositions = 0
        var k = 0
        for (i in s1.indices) {
            if (s1Matches[i]) {
                while (!s2Matches[k]) k++
                if (s1[i] != s2[k]) transpositions++
                k++
            }
        }

        val jaroSimilarity = (matches / s1Length.toDouble() +
            matches / s2Length.toDouble() +
            (matches - transpositions / 2.0) / matches) / 3.0

        // Winkler adjustment
        val prefixLength = s1.commonPrefixWith(s2).length.coerceAtMost(4)
        val winklerAdjustment = 0.1 * prefixLength * (1 - jaroSimilarity)

        return jaroSimilarity + winklerAdjustment
    }
}
