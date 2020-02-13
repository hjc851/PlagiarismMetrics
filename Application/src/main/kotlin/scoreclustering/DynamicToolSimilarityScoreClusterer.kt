package scoreclustering

import java.nio.file.Files
import java.nio.file.Paths

fun main() {
//    val scores = Paths.get("/home/haydencheers/Desktop/SENG1110A12017_Seeded/comparisonresults.txt")
//    val scores = Paths.get("/home/haydencheers/Desktop/COMP2240_A1_2018_Seeded/results-dynamic.txt")
    val scores = Paths.get("/home/haydencheers/Desktop/SENG2050 Datasets/SENG2050_A1_2017-results-dynamic.txt")

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

    val nonVariantScores = similarityScores.filter { !nameIsVariant(it.lhs) && !nameIsVariant(it.rhs) }
    val similarityThreshold = findClusterThreshold(nonVariantScores, 6, 0.001)
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
    println("Threshold ${similarityThreshold*100}")
    println("${variantComparisons.size} of ${notInLargestCluster.size} are correct")
}
