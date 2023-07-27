package passwordStore.utils

import java.util.*


/** @return lexical similarity value in the range [0,1]
 */
fun String.distance(to: String): Double {
    val tokens1 = this.uppercase().split("\\s".toRegex()).toMutableList()
    val tokens2 = to.uppercase().split("\\s".toRegex()).toMutableList()
    return tokens1.map { token1 ->
        var mindist = Double.MAX_VALUE
        for (token2 in tokens2) {
            val dist = LevenshteinDistance.defaultInstance.apply(token1, token2) / token2.length.toDouble()
            if (dist < mindist) {
                mindist = dist
            }
        }
        1 - mindist
    }.average()
}

fun String.titlecase() =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

