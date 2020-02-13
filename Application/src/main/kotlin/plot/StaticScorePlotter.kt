package plot

import com.orsoncharts.Chart3DFactory
import com.orsoncharts.Chart3DPanel
import com.orsoncharts.data.xyz.XYZSeries
import com.orsoncharts.data.xyz.XYZSeriesCollection
import com.orsoncharts.plot.XYZPlot
import scoreclustering.SimilarityScore
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JFileChooser
import javax.swing.JFrame
import kotlin.streams.toList

fun main() {
    val scores = Paths.get("/home/haydencheers/Desktop/SENG2050 Datasets/SENG2050_A1_2017-results-jplag.txt")
    val reader = Files.newBufferedReader(scores)

    val similarityScores = reader.lines()
        .map { line -> line.split(":") }
        .map { SimilarityScore(it[0], it[1], it[2].toDouble()) }
        .use { it.toList() }

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