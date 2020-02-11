package scoreclustering

import weka.estimators.KernelEstimator
import java.nio.file.Files
import java.nio.file.Paths

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

    val kde = KernelEstimator(0.01)
    for (score in similarityScores) kde.addValue(score.score, 1.0)

    for (score in similarityScores.sortedByDescending { it.score }) {
        println("${score.score}\t=\t${kde.getProbability(score.score)}")
    }
}