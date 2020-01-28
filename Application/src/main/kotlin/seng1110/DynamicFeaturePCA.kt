package seng1110

import ArffUtil
import InstanceFilter
import performSelection
import weka.attributeSelection.GainRatioAttributeEval
import weka.attributeSelection.PrincipalComponents
import weka.attributeSelection.Ranker
import weka.clusterers.ClusterEvaluation
import weka.clusterers.EM
import weka.clusterers.HierarchicalClusterer
import weka.clusterers.SimpleKMeans
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Normalize
import weka.filters.unsupervised.attribute.Remove
import java.nio.file.Paths
import java.util.*
import java.util.function.Predicate

private val random = Random(11121993)

fun main() {
    println("Loading data")
    val dataPath = Paths.get("/home/haydencheers/Desktop/SENG1110A12017/features-average-graph.arff")
    var data = ArffUtil.load(dataPath)
    val ids = data.map { it.stringValue(0) }
    val classes = data.map { it.stringValue(it.classIndex()) }

    println("Splitting data into test & train")
//    var train = InstanceFilter.filterByLevel(data, 10)
    var train =
        InstanceFilter.filter(data, Predicate { random.nextBoolean() })
    val trainIds = train.map { it.stringValue(0) }
    val trainClasses = train.map { it.stringValue(it.classIndex()) }

    var test = InstanceFilter.filter(data, Predicate { !trainIds.contains(it.stringValue(0)) })
    val testIds = test.map { it.stringValue(0) }
    val testClasses = test.map { it.stringValue(it.classIndex()) }

    println("Normalizing features")
    val normalizeFilter = Normalize().apply {
        setInputFormat(data)
    }

    data = Filter.useFilter(data, normalizeFilter)
    train = Filter.useFilter(train, normalizeFilter)
    test = Filter.useFilter(test, normalizeFilter)

    println("Performing InfoGain")
    val infoSelection = performSelection(
        train,
        GainRatioAttributeEval(),
        Ranker().apply {
            threshold = 0.0
        }
    )

    println("\tRetained ${infoSelection.numberAttributesSelected()} features")

    train = infoSelection.reduceDimensionality(train)
    test = infoSelection.reduceDimensionality(test)

    println("Removing id and class attributes")
    val removeIdAndClassFilter = Remove().apply {
        this.setAttributeIndicesArray(intArrayOf(0, data.classIndex()))
        setInputFormat(data)
    }

    data = Filter.useFilter(data, removeIdAndClassFilter)
    train = Filter.useFilter(train, removeIdAndClassFilter)
    test = Filter.useFilter(test, removeIdAndClassFilter)

    data.setClassIndex(-1)
    train.setClassIndex(-1)
    test.setClassIndex(-1)

    println("Performing PCA")
    val pca = PrincipalComponents()
    val pcaSelection = performSelection(
        train,
        pca,
        Ranker().apply {
            threshold = 0.0
        }
    )

    train = pcaSelection.reduceDimensionality(train)
    test = pcaSelection.reduceDimensionality(test)

    println("Performing clustering")
    val clusterer = SimpleKMeans()
//    val clusterer = HierarchicalClusterer()
//    val clusterer = EM()
    clusterer.numClusters = test.numInstances()
    clusterer.buildClusterer(train)

    println("Cluster Evaluation")
    val eval = ClusterEvaluation()
    eval.setClusterer(clusterer)
    eval.evaluateClusterer(test)

    println(eval.clusterResultsToString())

    println("**** Clusters ****")
    eval.clusterAssignments
        .mapIndexed { index, assignment ->
            testIds[index] to assignment
        }
        .groupBy { it.second }
        .forEach { (assignment, instances) ->
            println("Cluster ${assignment}")
            instances.forEach { (id, cluster) ->
                println("\t$id")
            }
            println()
        }

    println()
    println("**** Statistics - Dynamic Simulated Plagiarism ****")
    println("** Config")
    println("\t${test.numAttributes()} Features")
    println("\t${test.numInstances()} of ${data.numInstances()} Instances")
    println("\t${clusterer.javaClass.simpleName} Clusterer")
//    println("\tTraining Set BASE ${TRAIN_LEVELS.joinToString(" ")}")

//    println("** Counts")
    val groups = testIds.groupBy { it.split("-").first() }
//    for ((base, members) in groups.toList().sortedBy { it.first }) {
//        println("Base ${base} - ${members.size}")
//    }
//    println()

    val clusters = eval.clusterAssignments
        .mapIndexed { index, assignment -> testIds[index] to assignment }
        .groupBy { it.second }

    val bases = setOf("P1", "P2", "P3", "P4", "P5")

    println("** Cluster Compositions")
    clusters.keys.sorted().forEach { cluster ->
        val elements = clusters.getValue(cluster)
            .map { it.first }

        val consistency = elements.groupBy { it.split("-").first() }
            .toList()
            .sortedBy { it.first }

        println("Cluster $cluster - ${elements.size}")

        for ((base, members) in consistency) {
            val ofCluster = members.size.toDouble().div(elements.size)*100
            val ofType = members.size.toDouble().div(groups.getValue(base).size)*100

            println("\t$base - ${String.format("%d\n\t\t%05.2f of cluster\n\t\t%05.2f of type", members.size, ofCluster, ofType)}")

            val baseEntries = members.filter { bases.contains(it.split("-").dropLast(1).joinToString("-")) }
                .sorted()
                .groupBy { it.split("-").first() }

            baseEntries[base]?.let {
                it.forEach {
                    println("\t\tContains base ${it}")
                }
            }
        }

        println()
    }

    System.exit(0)
}