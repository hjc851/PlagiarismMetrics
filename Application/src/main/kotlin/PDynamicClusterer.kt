import weka.attributeSelection.*
import weka.clusterers.ClusterEvaluation
import weka.clusterers.EM
import weka.clusterers.FilteredClusterer
import weka.core.Instance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.unsupervised.attribute.AddID
import weka.filters.unsupervised.attribute.Remove
import weka.filters.unsupervised.attribute.RemoveByName
import weka.filters.unsupervised.attribute.RenameAttribute
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.regex.Pattern
import kotlin.streams.toList

fun main() {
    println("Loading data")

    val dataPath = Paths.get("/home/haydencheers/Desktop/SimulatedPlagiarism/features-average-graph.arff")
    var data = ArffUtil.load(dataPath)
    data = Filter.useFilter(data, AddID().apply { setInputFormat(data) })

    val ids = mutableMapOf<Double, String>()
    for (instance in data) {
        ids[instance.value(0)] = instance.stringValue(1)
    }

    data = Filter.useFilter(data, RemoveByName().apply { expression = Pattern.quote("id"); setInputFormat(data) })

    val bases = setOf("P1", "P2", "P3", "P4", "P5")
    val testPred = object: Predicate<Instance> {
        val levels = arrayOf("L3", "L4", "L5")
//        val levels = arrayOf("L1", "L2", "L3", "L4", "L5")

        override fun test(instance: Instance): Boolean {
            val sig = ids[instance.value(0)]!!
            val base = sig.split("-")
                .dropLast(1)
                .joinToString("-")

            if (bases.contains(base)) return true

            val level = base.split("-")
                .drop(1)
                .dropLast(1)
                .single()

            return levels.contains(level)
        }
    }

    val trainPred = object: Predicate<Instance> {
        val levels = arrayOf("L1", "L2")
//        val levels = emptyArray<String>()

        override fun test(instance: Instance): Boolean {
            val sig = ids[instance.value(0)]!!
            val base = sig.split("-")
                .dropLast(1)
                .joinToString("-")

            if (bases.contains(base)) return true

            val level = base.split("-")
                .drop(1)
                .dropLast(1)
                .single()

            return levels.contains(level)
        }
    }

    val allData = Instances(data)

    var testInstances = data.stream().filter(testPred).toList()
    var trainInstances = data.stream().filter(trainPred).toList()
    data.clear()

    var testData = Instances(data, testInstances.size)
    var trainData = Instances(data, trainInstances.size)

    testData.addAll(testInstances)
    trainData.addAll(trainInstances)

    data = allData

//    println("GainRatio ATTR Selection")
//    val attrSelection0 = AttributeSelection()
//    attrSelection0.setEvaluator(GainRatioAttributeEval())
//    attrSelection0.setSearch(Ranker().apply { threshold = 0.0 })
//    attrSelection0.SelectAttributes(trainData)
//
//    testData = attrSelection0.reduceDimensionality(testData)
//    trainData = attrSelection0.reduceDimensionality(trainData)

    println("InfoGain ATTR Selection")
    val attrSelection1 = AttributeSelection()
    attrSelection1.setEvaluator(InfoGainAttributeEval())
    attrSelection1.setSearch(Ranker().apply {
        threshold = 0.0
        numToSelect = 60
    })
    attrSelection1.SelectAttributes(trainData)

//    testData = attrSelection1.reduceDimensionality(testData)
//    trainData = attrSelection1.reduceDimensionality(trainData)

    println("Copying selected attributes")
    var selectedAttributeNames = attrSelection1.selectedAttributes().map { data.attribute(it).name() }

    if (!selectedAttributeNames.contains("ID"))
        selectedAttributeNames = listOf("ID", *selectedAttributeNames.toTypedArray())

    val selectedAttributeIndicies = selectedAttributeNames.map { data.attribute(it).index() }
        .sorted()
        .toIntArray()

    data = Filter.useFilter(data, Remove().apply { setAttributeIndicesArray(selectedAttributeIndicies); setInputFormat(data) })

    testInstances = data.stream().filter(testPred).toList()
    trainInstances = data.stream().filter(trainPred).toList()
    data.clear()

    testData = Instances(data, testInstances.size)
    trainData = Instances(data, trainInstances.size)

    testData.addAll(testInstances)
    trainData.addAll(trainInstances)

    println("Remove class attribute")
    testData.setClassIndex(-1)
    trainData.setClassIndex(-1)

    val removeClsFilter = RemoveByName().apply {
        expression = Pattern.quote("cls")
        invertSelection = false
        setInputFormat(testData)
    }

    testData = Filter.useFilter(testData, removeClsFilter)
    trainData = Filter.useFilter(trainData, removeClsFilter)

    println("Cluster Evaluation")
    val clusterer = FilteredClusterer().apply {
        val filter = RemoveByName()
        filter.expression = Pattern.quote("ID")

        val clusterer = EM()
        clusterer.numClusters = 8

        this.filter = filter
        this.clusterer = clusterer
    }

    clusterer.buildClusterer(trainData)

    val eval = ClusterEvaluation()
    eval.setClusterer(clusterer)
    eval.evaluateClusterer(testData)

    println(eval.clusterResultsToString())

    println("Clusters")
    eval.clusterAssignments.mapIndexed { index, assignment ->
        testData.get(index) to assignment
    }.groupBy { it.second }
    .forEach { (assignment, instances) ->
        println("Cluster $assignment")
        instances.forEach {
            val name = it.first.value(0)!!
            val id = ids[name]!!

            println("\t$id")
        }
        println()
    }
}