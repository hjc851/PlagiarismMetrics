package scoreclustering

import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
import kotlin.math.roundToInt

data class SimilarityScore(val lhs: String, val rhs: String, val score: Double): Clusterable {
    override fun getPoint(): DoubleArray = doubleArrayOf(score)
}

fun findClusterThreshold(similarityScores: List<SimilarityScore>, densityFactor: Int): Double {
    val scoreValues = similarityScores.map { it.score }.toDoubleArray()

    val stddev = scoreValues.stddev() ?: 0.0
    val average = scoreValues.average()

    val ubound = average + stddev
    val lbound = average - stddev
    val bound = lbound..ubound

    val scoresInBound = scoreValues.filter { it in bound }

    val eps = stddev
    val minpts = scoresInBound.count().toDouble().div(densityFactor).roundToInt()

    val clusterer = DBSCANClusterer<SimilarityScore>(eps, minpts)
    val clusters = clusterer.cluster(similarityScores)
    val largestCluster = clusters.maxBy { it.points.size }!!

    val similarityThreshold = largestCluster.points.maxBy { it.score }!!
    return similarityThreshold.score
}

fun nameIsVariant(name: String): Boolean {
    if (name.endsWith("-L1")) return true
    if (name.endsWith("-L2")) return true
    if (name.endsWith("-L3")) return true
    if (name.endsWith("-L4")) return true
    if (name.endsWith("-L5")) return true

    return false
}

val bases = listOf (
    "SENG1110_A1_2017_9AD9DC8455122FA85F3651055CFCE036",
    "SENG1110_A1_2017_EF11C90DE680328951F7216E98733249",
    "SENG1110_A1_2017_0BD88A4C124FF464E8BCCDC40A8DD0F9"
)

fun isBaseOrVariantComparison(score: SimilarityScore): Boolean {
    var lIsBaseOrVariant = false
    var rIsBaseOrVariant = false

    for (base in bases) {
        if (score.lhs.startsWith(base) && score.rhs.startsWith(base)) {
            lIsBaseOrVariant = true
            rIsBaseOrVariant = true
        }
    }

    return lIsBaseOrVariant && rIsBaseOrVariant
}

@Strictfp
fun DoubleArray.stddev(): Double? {
    if (this.isEmpty()) return null

    return Math.sqrt(this.variance() ?: 0.0)
}

@Strictfp
fun DoubleArray.variance(): Double? {
    if (this.isEmpty()) return null

    val sum = this.sum()
    val sumsq = this.sumByDouble { it*it }
    val mean = sum / this.size
    return sumsq/this.size - mean*mean
}