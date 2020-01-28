import weka.attributeSelection.*
import weka.clusterers.*
import weka.core.Instance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Normalize
import weka.filters.unsupervised.attribute.Remove
import weka.filters.unsupervised.attribute.RemoveByName
import java.awt.Toolkit
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.sqrt
import kotlin.random.Random

private val TRAIN_LEVELS = arrayOf("L1", "L2")
private val TEST_LEVELS = arrayOf("L3", "L4", "L5")
//private val TRAIN_LEVELS = arrayOf<String>()
//private val TEST_LEVELS = arrayOf("L1", "L2", "L3", "L4", "L5")

private val RANDOM = Random(11121993)

fun main() {
    println("Loading data")
    val dataPath = Paths.get("/home/haydencheers/Desktop/SimulatedPlagiarism/features-average-graph.arff")
    var data = ArffUtil.load(dataPath)

    println("Normalizing features")
    val normalizeFilter = Normalize().apply {
        setInputFormat(data)
        this.scale = 1000.0
    }

    data = Filter.useFilter(data, normalizeFilter)

    println("Splitting data")
    var train = InstanceFilter.filterByLevel(data, 10)
//    var train = InstanceFilter.filter(data, Predicate { it.stringValue(0).contains("-L5-") })

    val trainIds = train.map { it.stringValue(0) }
    val baseIds = trainIds.map { it to it.split("-").drop(1).dropLast(2).singleOrNull() }
        .filter { it.second == null }
        .map { it.first }

//    var train = InstanceFilter.filter(data, TRAIN_LEVELS)

    println("GainRatio ATTR Selection")
    val grSelection = performSelection(
        train,
        GainRatioAttributeEval(),
        Ranker().apply {
            threshold = 0.0
        }
    )

    train = grSelection.reduceDimensionality(train)
    println("\tRetained ${train.numAttributes()} attributes")

    println("InfoGain ATTR Selection")
    val igSelection = performSelection(
        train,
        InfoGainAttributeEval(),
        Ranker().apply {
            threshold = 0.0
        }
    )

    train = igSelection.reduceDimensionality(train)
    println("\tRetained ${train.numAttributes()} attributes")

    println("ChiSquared ATTR Selection")
    val csSelection = performSelection(
        train,
        ChiSquaredAttributeEval(),
        Ranker().apply {
            threshold = 0.0
//            numToSelect = 60
        }
    )

    train = csSelection.reduceDimensionality(train)
    println("\tRetained ${train.numAttributes()} attributes")

    println("Correlation ATTR Selection")
    val corrSelection = performSelection (
        train,
        CorrelationAttributeEval(),
        Ranker().apply {
            threshold = 0.0
        }
    )

    train = corrSelection.reduceDimensionality(train)
    println("\tRetained ${train.numAttributes()} attributes")

    println("Cfs Subset ATTR Selection")
    val csfSelection = performSelection (
        train,
        CfsSubsetEval().apply {
            this.locallyPredictive = false
            this.numThreads = Runtime.getRuntime().availableProcessors()
        },
        GreedyStepwise().apply {
            this.threshold = 0.0
            this.searchBackwards = true
        }
    )

    train = csfSelection.reduceDimensionality(train)
    println("\tRetained ${train.numAttributes()} attributes")

//    val corrselection = weka.filters.supervised.attribute.AttributeSelection()
//    corrselection.setInputFormat(train)
//    corrselection.evaluator = CfsSubsetEval()
//    corrselection.search = GreedyStepwise().apply {
//        threshold = 0.0
//        searchBackwards = true
//    }
//
//    train = Filter.useFilter(train, corrselection)

    println("Reducing Attributes")
    val selectedAttributes = (0 until train.numAttributes())
        .map { train.attribute(it).name() }
        .toMutableList()

    if (!selectedAttributes.contains("id")) selectedAttributes.add(0, "id")
    if (selectedAttributes.contains("cls")) selectedAttributes.remove("cls")

    val attributeIndicies = selectedAttributes.map { data.attribute(it).index() }
        .sorted()
        .toIntArray()

    val attributeFilter = Remove().apply {
        setAttributeIndicesArray(attributeIndicies)
        invertSelection = true
        setInputFormat(data)
    }

    val filteredData = Filter.useFilter(data, attributeFilter)

    val filteredTrain = InstanceFilter.filter(filteredData, Predicate<Instance> { trainIds.contains(it.stringValue(0)) })
    val filteredTest = InstanceFilter.filter(filteredData, Predicate<Instance> { !trainIds.contains(it.stringValue(0)) || baseIds.contains(it.stringValue(0)) })

//    val filteredTrain = InstanceFilter.filter(filteredData, TRAIN_LEVELS)
//    val filteredTest = InstanceFilter.filter(filteredData, TEST_LEVELS)

    println("Performing Clustering")
    val clusterer = FilteredClusterer().apply {
        val filter = RemoveByName()
        filter.expression = Pattern.quote("id")

        val clusterer = EM()
        clusterer.numClusters = 8

//        clusterer.maxIterations = 10
//        val clusterer = SimpleKMeans()
//        clusterer.numClusters = 8

        this.filter = filter
        this.clusterer = clusterer
    }

    clusterer.buildClusterer(filteredTrain)

    val eval = ClusterEvaluation()
    eval.setClusterer(clusterer)
    eval.evaluateClusterer(filteredTest)

    println(eval.clusterResultsToString())

    println("**** Clusters ****")

    eval.clusterAssignments
        .mapIndexed { index, assignment ->
            filteredTest[index] to assignment
        }
        .groupBy { it.second  }
        .forEach { (assignment, instances) ->
            println("Cluster $assignment")

            instances.forEach { (instance, cluster) ->
                println("\t${instance.stringValue(0)}")
            }

            println()
        }

    println()
    println("**** Statistics - Dynamic Simulated Plagiarism ****")
    println("** Config")
    println("\t${filteredTest.numAttributes()} Features")
    println("\t${clusterer.clusterer.javaClass.simpleName} Clusterer")
//    println("\tTraining Set BASE ${TRAIN_LEVELS.joinToString(" ")}")

    println("** Counts")
    val ids = filteredTest.map { it.stringValue(0) }
    val groups = ids.groupBy { it.split("-").first() }
    for ((base, members) in groups.toList().sortedBy { it.first }) {
        println("Base ${base} - ${members.size}")
    }
    println()

    val clusters = eval.clusterAssignments
        .mapIndexed { index, assignment -> filteredTest[index] to assignment }
        .groupBy { it.second }

    val bases = setOf("P1", "P2", "P3", "P4", "P5")

    println("** Cluster Compositions")
    clusters.keys.sorted().forEach { cluster ->
        val elements = clusters[cluster]!!.map { it.first.stringValue(0) }
            .sorted()

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

    Toolkit.getDefaultToolkit().beep()
    System.exit(0)
}

fun performSelection(data: Instances, evaluator: ASEvaluation, search: ASSearch): AttributeSelection {
    val selection = AttributeSelection()
    selection.setEvaluator(evaluator)
    selection.setSearch(search)

    selection.SelectAttributes(data)

    return selection
}

object InstanceFilter {
    private val bases = setOf("P1", "P2", "P3", "P4", "P5")

    fun filter(data: Instances, pred: Predicate<Instance>): Instances {
        val instances = data.parallelStream()
            .filter(pred)
            .collect(Collectors.toList())

        val attributes = ArrayList((0 until data.numAttributes()).map { data.attribute(it) })
        val ds = Instances(data.relationName(), attributes, instances.size)
        instances.forEach { ds.add(it) }
        ds.setClassIndex(data.classIndex())

        return ds
    }

    fun filter(data: Instances, levels: Array<String>): Instances {
        val pred = Predicate<Instance> { instance ->
            val sig = instance.stringValue(0)
            val base = sig.split("-")
                .dropLast(1)
                .joinToString("-")

            if (bases.contains(base)) return@Predicate true

            val level = base.split("-")
                .drop(1)
                .dropLast(1)
                .single()

            return@Predicate levels.contains(level)
        }

        val instances = data.parallelStream().filter(pred).collect(Collectors.toList())
        val attributes = ArrayList((0 until data.numAttributes()).map { data.attribute(it) })

        val ds = Instances(data.relationName(), attributes, instances.size)
        instances.forEach { ds.add(it) }
        ds.setClassIndex(data.classIndex())

        return ds
    }

    fun filterByLevel(data: Instances, countPerLevel: Int): Instances {
        val selectedInstances = mutableListOf<Instance>()

        val groups = data.map { it.stringValue(0) to it }
            .groupBy { it.first.split("-")[0] }

        for ((base, group) in groups) {
            val multiplier = group.size / 150

            val levels = group.groupBy { it.first.split("-").drop(1).dropLast(2).singleOrNull() }

            // Select the instances from each group
            for ((level, members) in levels) {
                val selection = members.shuffled(RANDOM)
//                val selection = members.shuffled()
                    .take(multiplier * countPerLevel)
                    .map { it.second }

                selectedInstances.addAll(selection)
            }
        }

        val attributes = ArrayList((0 until data.numAttributes()).map { data.attribute(it) })

        val ds = Instances(data.relationName(), attributes, selectedInstances.size)
        selectedInstances.forEach { ds.add(it) }
        ds.setClassIndex(data.classIndex())

        return ds
    }
}