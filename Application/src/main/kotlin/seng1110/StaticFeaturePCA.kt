package seng1110

import ArffUtil
import InstanceFilter
import performSelection
import weka.attributeSelection.*
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
    val dataPath = Paths.get("/home/haydencheers/Desktop/SENG1110A12017_Seeded/features-static.arff")
    var data = ArffUtil.load(dataPath)
    val ids = data.map { it.stringValue(0) }
    val classes = data.map { it.stringValue(it.classIndex()) }

    println("Splitting data into test & train")
    var train = InstanceFilter.filter(data, Predicate { random.nextBoolean() })
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

    data = infoSelection.reduceDimensionality(data)
    train = infoSelection.reduceDimensionality(train)
    test = infoSelection.reduceDimensionality(test)

//    println("Performing Cfs")
//    val cfsEval = performSelection(
//        train,
//        CfsSubsetEval(),
//        GreedyStepwise().apply {
//            numExecutionSlots = 12
//            searchBackwards = true
//            threshold = 0.0
//        }
//    )
//
//    println("\tRetained ${cfsEval.numberAttributesSelected()} features")
//
//    data = cfsEval.reduceDimensionality(data)
//    train = cfsEval.reduceDimensionality(train)
//    test = cfsEval.reduceDimensionality(test)

    println("Removing id and class attributes")
    if (train.attribute("id") != null) {
        val id = train.attribute("id")

        val removeIdFilter = Remove().apply {
            setAttributeIndicesArray(intArrayOf(id.index()))
            setInputFormat(train)
        }

        data = Filter.useFilter(data, removeIdFilter)
        train = Filter.useFilter(train, removeIdFilter)
        test = Filter.useFilter(test, removeIdFilter)
    }

    if (train.attribute("cls") != null) {
        val id = train.attribute("cls")

        val removeClsFilter = Remove().apply {
            setAttributeIndicesArray(intArrayOf(id.index()))
            setInputFormat(train)
        }

        data = Filter.useFilter(data, removeClsFilter)
        train = Filter.useFilter(train, removeClsFilter)
        test = Filter.useFilter(test, removeClsFilter)
    }

    data.setClassIndex(-1)
    train.setClassIndex(-1)
    test.setClassIndex(-1)

    println("Performing PCA")
    val pcaSelection = performSelection(
        train,
        PrincipalComponents().apply {
            centerData = true
            maximumAttributeNames = -1
        },
        Ranker().apply {
            threshold = 0.0
        }
    )

    data = pcaSelection.reduceDimensionality(data)
    train = pcaSelection.reduceDimensionality(train)
    test = pcaSelection.reduceDimensionality(test)

    println("Performing clustering")
//    val clusterer = SimpleKMeans()
    val clusterer = HierarchicalClusterer()
//    val clusterer = EM()

    clusterer.numClusters = data.numInstances()-15
    clusterer.buildClusterer(data)

//    clusterer.numClusters = test.numInstances()
//    clusterer.buildClusterer(train)

    println("Cluster Evaluation")
    val eval = ClusterEvaluation()
    eval.setClusterer(clusterer)
    eval.evaluateClusterer(data)

    println(eval.clusterResultsToString())

    println("**** Clusters ****")
    eval.clusterAssignments
        .mapIndexed { index, assignment ->
            ids[index] to assignment
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
    println("\t${data.numAttributes()} Features")
    println("\t${data.numInstances()} of ${data.numInstances()} Instances")
    println("\t${clusterer.javaClass.simpleName} Clusterer")
    println("** Features")
    for (i in 0 until data.numAttributes()) {
        val attr = data.attribute(i)
        println(attr.name())
    }
    println()

    val clusters = eval.clusterAssignments
        .mapIndexed { index, assignment -> ids[index] to assignment }
        .groupBy { it.second }

    println("** Cluster Compositions")
    clusters.keys.sorted().forEach { cluster ->
        val elements = clusters.getValue(cluster)
            .map { it.first }

        println("Cluster $cluster - ${elements.size}")

        for (element in elements) {
            println("\t${element}")
        }

        println()
    }

    System.exit(0)
}