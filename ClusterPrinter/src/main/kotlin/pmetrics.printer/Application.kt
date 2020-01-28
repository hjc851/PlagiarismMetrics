package pmetrics.printer

import java.nio.file.Files
import java.nio.file.Paths

data class Instance (
    val id: String,
    val cluster: String
)

fun main(args: Array<String>) {
    val file = Paths.get(args[0])
    val reader = Files.newBufferedReader(file)

    val data = mutableListOf<Instance>()

    var line: String? = null
    do {
        line = reader.readLine()
        if (line != null && line.equals("@data", true)) break
    } while (line != null)

    if (line == null) {
        println("Cannot find @data section of arff file")
        return
    }

    do {
        line = reader.readLine() ?: break

        val components = line.split(",")
        val id = components[components.size-2]
        val cluster = components[components.size-1]

        data.add(Instance(id, cluster))

    } while (true)

    val groups = data.groupBy(Instance::cluster)

    for ((cluster, instances) in groups) {
        println(cluster)
        instances.forEach { println("\t${it.id}") }
        println()
    }
}