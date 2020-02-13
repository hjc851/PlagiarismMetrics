package scoreclustering

import com.orsoncharts.Chart3DFactory
import com.orsoncharts.Chart3DPanel
import com.orsoncharts.data.xyz.XYZSeries
import com.orsoncharts.data.xyz.XYZSeriesCollection
import com.orsoncharts.plot.XYZPlot
import weka.estimators.KernelEstimator
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JFrame
import kotlin.streams.toList

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

            val lscore = reader.readLine().removePrefix("LSIM: ").toDouble() * 100.0
            val rscore = reader.readLine().removePrefix("RSIM: ").toDouble() * 100.0

            similarityScores.add(SimilarityScore(lproj, rproj, lscore))
            similarityScores.add(SimilarityScore(rproj, lproj, rscore))

            reader.readLine()
        }

        header = reader.readLine()
    }
    reader.close()

    val ids = similarityScores.flatMap { listOf(it.lhs, it.rhs) }
        .sorted()
        .toSet()
        .toTypedArray()

    val series = XYZSeries<String>("Scores")
    for (score in similarityScores) {
        val lindex = ids.indexOf(score.lhs).toDouble()
        val rindex = ids.indexOf(score.rhs).toDouble()
        series.add(lindex, rindex, score.score)
    }

    val dataset = XYZSeriesCollection<String>()
    dataset.add(series)

    val chart = Chart3DFactory.createScatterChart(
        scores.fileName.toString(),
        "",
        dataset,
        "LHS",
        "RHS",
        "Similarity"
    )

    (chart.plot as XYZPlot).zAxis.setRange(0.0, 100.0)

    val panel = Chart3DPanel(chart)

    val frame = JFrame()
    frame.add(panel)
    frame.setSize(600, 400)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}