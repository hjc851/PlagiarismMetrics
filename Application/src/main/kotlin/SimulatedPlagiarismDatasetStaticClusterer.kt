import weka.attributeSelection.ChiSquaredAttributeEval
import weka.attributeSelection.GainRatioAttributeEval
import weka.attributeSelection.InfoGainAttributeEval
import weka.attributeSelection.Ranker
import weka.clusterers.ClusterEvaluation
import weka.clusterers.EM
import weka.clusterers.FilteredClusterer
import weka.clusterers.SimpleKMeans
import weka.core.Instance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.unsupervised.attribute.Remove
import weka.filters.unsupervised.attribute.RemoveByName
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors

//private val TRAIN_LEVELS = arrayOf("L1", "L2")
//private val TEST_LEVELS = arrayOf("L3", "L4", "L5")
private val TRAIN_LEVELS = arrayOf<String>()
private val TEST_LEVELS = arrayOf("L1", "L2", "L3", "L4", "L5")

fun main() {
    println("Loading data")
    val dataPath = Paths.get("/home/haydencheers/Desktop/SimulatedPlagiarism/features-static.arff")
    val data = ArffUtil.load(dataPath)

    var train = StaticInstanceFilter.filter(data, TRAIN_LEVELS)

    println("GainRatio ATTR Selection")
    val grSelection = performSelection(
        train,
        GainRatioAttributeEval(),
        Ranker().apply {
            threshold = 0.0
        }
    )

    train = grSelection.reduceDimensionality(train)

    println("InfoGain ATTR Selection")
    val igSelection = performSelection(
        train,
        InfoGainAttributeEval(),
        Ranker().apply {
            threshold = 0.0
        }
    )

    train = igSelection.reduceDimensionality(train)

    println("ChiSquared ATTR Selection")
    val csSelection = performSelection(
        train,
        ChiSquaredAttributeEval(),
        Ranker().apply {
            threshold = 0.0
            numToSelect = 20
        }
    )

    train = csSelection.reduceDimensionality(train)

    println("Reducing Attributes")
    val selectedAttributes = (0 until train.numAttributes()).map { train.attribute(it).name() }
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
    val filteredTrain = StaticInstanceFilter.filter(filteredData, TRAIN_LEVELS)
    val filteredTest = StaticInstanceFilter.filter(filteredData, TEST_LEVELS)

    println("Performing Clustering")
    val clusterer = FilteredClusterer().apply {
        val filter = RemoveByName()
        filter.expression = Pattern.quote("id")

        val clusterer = EM()
//        val clusterer = SimpleKMeans()
        clusterer.numClusters = 5

        this.filter = filter
        this.clusterer = clusterer
    }

    clusterer.buildClusterer(filteredTrain)

    val eval = ClusterEvaluation()
    eval.setClusterer(clusterer)
    eval.evaluateClusterer(filteredTest)

    println(eval.clusterResultsToString())

    println()
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
    println("**** Statistics - Static Simulated Plagiarism ****")
    println("** Config")
    println("\t${filteredTest.numAttributes()} Features")
    println("\t${clusterer.clusterer.javaClass.simpleName} Clusterer")
    println("\tTraining Set BASE ${TRAIN_LEVELS.joinToString(" ")}")

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

            for (base in bases) {
                if (members.contains(base))
                    println("\t\tContains base ${base}")
            }
        }

        println()
    }
}

private object StaticInstanceFilter {
    private val bases = setOf("P1", "P2", "P3", "P4", "P5")

    fun filter(data: Instances, levels: Array<String>): Instances {
        val pred = Predicate<Instance> { instance ->
            val sig = instance.stringValue(0)

            if (bases.contains(sig)) return@Predicate true

            val level = sig.split("-")
                .drop(1)
                .dropLast(1)
                .single()

            return@Predicate levels.contains(level)
        }

        val instances = data.parallelStream().filter(pred).collect(Collectors.toList())
        val attributes = ArrayList((0 until data.numAttributes()).map { data.attribute(it) })

        val ds = Instances(data.relationName(), attributes, instances.size)
        instances.forEach { ds.add(it) }

        return ds
    }
}