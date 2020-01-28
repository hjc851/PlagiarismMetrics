import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

data class ClusterResults (
    val clusters: MutableMap<Double, MutableList<String>> = mutableMapOf()
)

fun main() {
    val file = Paths.get("/home/haydencheers/Desktop/Algorithms/Algorithms-50split.txt")
    val reader = Files.newBufferedReader(file, Charset.forName("UTF-8"))
    val scanner = Scanner(reader)

    val clusterResults = mutableMapOf<String, ClusterResults>()
    var currentSetId: String? = null
    var currentClusterId: Double? = null

    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
            .replace(Regex("[\\n\\t]"), "")
            .replace("\uFEFF", "")

        when {

            line.startsWith("Clusters – ") -> {
                currentSetId = line
                clusterResults[currentSetId] = ClusterResults()
            }

            line.startsWith("Cluster ") -> {
                currentClusterId = line.removePrefix("Cluster ").toDouble()
                clusterResults[currentSetId]!!.clusters[currentClusterId] = mutableListOf()
            }

            line == "" -> { }

            else -> {
                clusterResults[currentSetId]!!.clusters[currentClusterId]!!.add(line)
            }
        }
    }

    val allBase = clusterResults.values
        .flatMap { it.clusters.values.flatten() }
        .map { it.split("-")[0] }
        .toSet()

    val validVariantPrefix = arrayOf("-COPY", "-L1", "-L2", "-L3", "-L4", "-L5")
    for ((id, results) in clusterResults) {
        println("Result Set ${id}")
        val clusters = results.clusters

        val groups = clusters.values.flatten()
            .groupBy { it.split("-")[0] }

        for ((num, members) in clusters.toList().sortedBy { it.first }) {

            val baseNonSplit = members.filterNot { id -> validVariantPrefix.map { id.contains(it) }.reduce { l, r -> l || r} }

            val bases = baseNonSplit.map { it.split("-")[0] }
                .toSet()

            if (bases.isEmpty()) {
                println("\tCluster $num - No Base")

            } else if (bases.count() == 1) {
                val base = bases.first()
                val corrects = members.filter { it.startsWith(base) }
                val coverage = corrects.size / groups[base]!!.size.toDouble()
                val ofCluster = corrects.size / members.size.toDouble()

                println("\tCluster $num - ${base} - ${String.format("%.2f", coverage * 100)}% of variants, ${String.format("%.2f", ofCluster * 100)}% of cluster")

                val fullSigBase = baseNonSplit.filter { it.startsWith(base) && it.contains("-") }
                if (fullSigBase.isNotEmpty()) {
                    val traceIds = fullSigBase.map { it.split("-").last() }.joinToString(",")
                    println("\t\tTrace Ids [${traceIds}]")

                    val sigs = fullSigBase.map { it.split("-").takeLast(2).joinToString("-") }
                        .toSet()

                    sigs.forEach { println("\t\t$it") }
                }

            } else {
                println("\tCluster $num - Indeterminate")

                for (base in bases) {
                    val corrects = members.filter { it.startsWith(base) }
                    val coverage = corrects.size / groups[base]!!.size.toDouble()
                    val ofCluster = corrects.size / members.size.toDouble()

                    println("\t\t${base} - ${String.format("%.2f", coverage * 100)}% of variants, ${String.format("%.2f", ofCluster * 100)}% of cluster")

                    val fullSigBase = baseNonSplit.filter { it.startsWith(base) }
                    if (fullSigBase.isNotEmpty()) {
                        val traceIds = fullSigBase.map { it.split("-").last() }.joinToString(",")
                        println("\t\t\tTrace Ids [${traceIds}]")

                        val sigs = fullSigBase.map { it.split("-").takeLast(2).joinToString("-") }
                            .toSet()

                        sigs.forEach { println("\t\t\t$it") }
                    }
                }
            }

            println()
        }

        println()
    }
}