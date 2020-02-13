package plot

import com.orsoncharts.Chart3DFactory
import com.orsoncharts.Chart3DPanel
import com.orsoncharts.data.xyz.XYZSeries
import com.orsoncharts.data.xyz.XYZSeriesCollection
import com.orsoncharts.plot.XYZPlot
import scoreclustering.SimilarityScore
import weka.estimators.KernelEstimator
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.JFrame

fun main() {
    val scoresf = Paths.get("/home/haydencheers/Desktop/SENG2050 Datasets/SENG2050_A1_2019-results-dynamic.txt")

    val reader = Files.newBufferedReader(scoresf)
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
    similarityScores.sortBy { it.score }

    val kde = KernelEstimator(5.0)
    similarityScores.forEach { kde.addValue(it.score, 0.1) }

    val scores = similarityScores.sortedBy { it.score }
        .map { kde.getProbability(it.score) }
        .toDoubleArray()

    val maximae = findMaximae(scores)
    val minimae = findMinimae(scores)

    println("Maximae")
    maximae.forEach { println(it) }

    println("Minimae")
    minimae.forEach { println(it) }

    val maxScores = maximae.map { similarityScores[it] }
    val minScores = minimae.map { similarityScores[it] }

    val series = XYZSeries<String>("Scores")
    scores.forEachIndexed { index, score ->
        series.add(index.toDouble(), score, 0.0)
    }

    val dataset = XYZSeriesCollection<String>()
    dataset.add(series)

    val chart = Chart3DFactory.createScatterChart(
        scoresf.fileName.toString(),
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
    frame.isVisible = true}

fun findMaximae(arr: DoubleArray): IntArray {
    val maximae = mutableListOf<Int>()

    for (i in 1 until arr.size-1) {
        val l = arr[i-1]
        val m = arr[i]
        val r = arr[i+1]

        if (m > l && m > r)
            maximae.add(i)
    }

    return maximae.toIntArray()
}

fun findMinimae(arr: DoubleArray): IntArray {
    val minimae = mutableListOf<Int>()

    for (i in 1 until arr.size-1) {
        val l = arr[i-1]
        val m = arr[i]
        val r = arr[i+1]

        if (m < l && m < r)
            minimae.add(i)
    }

    return minimae.toIntArray()
}