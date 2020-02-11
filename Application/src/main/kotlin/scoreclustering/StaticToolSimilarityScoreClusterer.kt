package scoreclustering

import weka.estimators.KernelEstimator
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

fun main() {
    val scores = Paths.get("/home/haydencheers/Desktop/SENG1110A12017_Seeded/seng1110-seeded-jplag.txt")
    val reader = Files.newBufferedReader(scores)

    val similarityScores = reader.lines()
        .map { line -> line.split(":") }
        .map { SimilarityScore(it[0], it[1], it[2].toDouble()) }
        .use { it.toList() }

    val nonVariantScores = similarityScores.filter { !nameIsVariant(it.lhs) && !nameIsVariant(it.rhs) }
    val similarityThreshold = findClusterThreshold(nonVariantScores, 4)
    val notInLargestCluster = similarityScores.filter { it.score > similarityThreshold }
        .sortedByDescending { it.score }

    println("Not in cluster ${notInLargestCluster.size}")
    for (point in notInLargestCluster) {
        println(point)
    }
    println()

    val baseOrVariants = similarityScores.filter { isBaseOrVariantComparison(it) }
        .flatMap { listOf(it.lhs, it.rhs) }
        .distinct()

    val variantComparisons = notInLargestCluster.filter { isBaseOrVariantComparison(it) }
    val nonVariantComparisons = notInLargestCluster.filter { !isBaseOrVariantComparison(it) }
    println("Threshold ${similarityThreshold}")
    println("${variantComparisons.size} of ${notInLargestCluster.size} are correct")
}