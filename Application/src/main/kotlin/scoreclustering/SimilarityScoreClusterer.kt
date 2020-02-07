package scoreclustering

import org.apache.commons.math3.ml.clustering.Clusterable
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
import java.nio.file.Files
import java.nio.file.Paths

data class SimilarityScore(val lhs: String, val rhs: String, val score: Double): Clusterable {
    override fun getPoint(): DoubleArray = doubleArrayOf(score)
}

fun main() {
    val scores = Paths.get("/home/haydencheers/Desktop/SENG1110A12017_Seeded/comparisonresults.txt")
    val reader = Files.newBufferedReader(scores)
    val similarityScores = mutableListOf<SimilarityScore>()

    var header: String? = reader.readLine()
    while (header != null) {
        if (header.startsWith("Comparing ")) {
            header = header.removePrefix("Comparing ")

            val components = header.split(" vs ")
            val lproj = components[0]
            val rproj = components[1]

            val lscore = reader.readLine().removePrefix("LSIM: ").toDouble()
            val rscore = reader.readLine().removePrefix("RSIM: ").toDouble()

            similarityScores.add(SimilarityScore(lproj, rproj, lscore))
            similarityScores.add(SimilarityScore(rproj, lproj, rscore))

            reader.readLine()
        }

        header = reader.readLine()
    }
    reader.close()

    fun nameIsVariant(name: String): Boolean {
        if (name.endsWith("-L1")) return true
        if (name.endsWith("-L2")) return true
        if (name.endsWith("-L3")) return true
        if (name.endsWith("-L4")) return true
        if (name.endsWith("-L5")) return true

        return false
    }

    val nonVariantScores = similarityScores.filter { !nameIsVariant(it.lhs) && !nameIsVariant(it.rhs) }
    val similarityThreshold = findClusterThreshold(nonVariantScores)
    val notInLargestCluster = similarityScores.filter { it.score > similarityThreshold }
        .sortedByDescending { it.score }

    println("Not in cluster ${notInLargestCluster.size}")
    for (point in notInLargestCluster) {
        println(point)
    }
    println()

    val bases = listOf("SENG1110_A1_2017_9AD9DC8455122FA85F3651055CFCE036", "SENG1110_A1_2017_EF11C90DE680328951F7216E98733249", "SENG1110_A1_2017_0BD88A4C124FF464E8BCCDC40A8DD0F9")
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

    val baseOrVariants = similarityScores.filter { isBaseOrVariantComparison(it) }
        .flatMap { listOf(it.lhs, it.rhs) }
        .distinct()

    val variantComparisons = notInLargestCluster.filter { isBaseOrVariantComparison(it) }
    val nonVariantComparisons = notInLargestCluster.filter { !isBaseOrVariantComparison(it) }
    println("${variantComparisons.size} of ${notInLargestCluster.size} are correct")
}

private fun findClusterThreshold(similarityScores: List<SimilarityScore>): Double {
    val scoreValues = similarityScores.map { it.score }.toDoubleArray()
//    val _s = similarityScores.sortedByDescending { it.score }.take(90)
//    val groups = scoreValues.groupBy { it.times(100).div(10).toInt() }

    val stddev = scoreValues.stddev() ?: 0.0
    val average = scoreValues.average()

    val ubound = average + stddev
    val lbound = average - stddev
    val bound = lbound..ubound

    val scoresInBound = scoreValues.filter { it in bound }

    val eps = stddev/2.0
    val minpts = scoresInBound.count().div(6).toInt()

    val clusterer = DBSCANClusterer<SimilarityScore>(eps, minpts)
    val clusters = clusterer.cluster(similarityScores)
    val largestCluster = clusters.sortedByDescending { it.points.size }.first()

    val similarityThreshold = largestCluster.points.sortedByDescending { it.score }.first()
    return similarityThreshold.score
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