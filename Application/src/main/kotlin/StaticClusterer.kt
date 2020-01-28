import weka.attributeSelection.AttributeSelection
import weka.attributeSelection.GainRatioAttributeEval
import weka.attributeSelection.InfoGainAttributeEval
import weka.attributeSelection.Ranker
import weka.clusterers.ClusterEvaluation
import weka.clusterers.EM
import weka.clusterers.FilteredClusterer
import weka.core.Instance
import weka.core.Instances
import weka.filters.Filter
import weka.filters.unsupervised.attribute.RemoveByName
import java.nio.file.Paths
import java.util.function.Predicate
import java.util.regex.Pattern
import kotlin.streams.toList

fun main() {
    println("Loading data")
    val dataPath = Paths.get("/home/haydencheers/Desktop/Algorithms/features-static.arff")
    var data = ArffUtil.load(dataPath)

    val testPred = object: Predicate<Instance> {
        val validVariantPrefix = arrayOf("-L3", "-L4", "-L5")
//        val validVariantPrefix = arrayOf("-COPY", "-L1", "-L2", "-L3", "-L4", "-L5")

        override fun test(instance: Instance): Boolean {
            val id = instance.stringValue(0)
            val cls = instance.stringValue(instance.classIndex())

            val idNoClass = id.removePrefix(cls)

            for (prefix in validVariantPrefix) {
                if (idNoClass.startsWith(prefix))
                    return true
            }

            val dashCount = id.count { it == '-' }
            return idNoClass.isEmpty() || dashCount == 2
        }
    }

    val trainPred = object: Predicate<Instance> {
        val validVariantPrefix = arrayOf("-COPY", "-L1", "-L2")
//        val validVariantPrefix = emptyArray<String>()

        override fun test(instance: Instance): Boolean {
            val id = instance.stringValue(0)
            val cls = instance.stringValue(instance.classIndex())

            val idNoClass = id.removePrefix(cls)

            for (prefix in validVariantPrefix) {
                if (idNoClass.startsWith(prefix))
                    return true
            }

            val dashCount = id.count { it == '-' }
            return idNoClass.isEmpty() || dashCount == 2
        }
    }

    val testInstances = data.stream().filter(testPred).toList()
    val trainInstances = data.stream().filter(trainPred).toList()
    data.clear()

    var testData = Instances(data, testInstances.size)
    var trainData = Instances(data, trainInstances.size)

    testData.addAll(testInstances)
    trainData.addAll(trainInstances)

    println("GainRatio ATTR Selection")
    val attrSelection0 = AttributeSelection()
    attrSelection0.setEvaluator(GainRatioAttributeEval())
    attrSelection0.setSearch(Ranker().apply { threshold = 0.0 })
    attrSelection0.SelectAttributes(trainData)

    testData = attrSelection0.reduceDimensionality(testData)
    trainData = attrSelection0.reduceDimensionality(trainData)

    println("InfoGain ATTR Selection")
    val attrSelection1 = AttributeSelection()
    attrSelection1.setEvaluator(InfoGainAttributeEval())
    attrSelection1.setSearch(Ranker().apply {
        threshold = 0.0
        numToSelect = 61
    })
    attrSelection1.SelectAttributes(trainData)

    testData = attrSelection1.reduceDimensionality(testData)
    trainData = attrSelection1.reduceDimensionality(trainData)

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
        filter.expression = Pattern.quote("id")

        val clusterer = EM()
        clusterer.numClusters = 28

        this.filter = filter
        this.clusterer = clusterer
    }

    clusterer.buildClusterer(trainData)

    val eval = ClusterEvaluation()
    eval.setClusterer(clusterer)
    eval.evaluateClusterer(testData)

    println("Clusters")
    eval.clusterAssignments.mapIndexed { index, assignment ->
        testData.get(index) to assignment
    }.groupBy { it.second }
        .forEach { (assignment, instances) ->
            println("Cluster $assignment")
            instances.forEach {
                println("\t${it.first.stringValue(0)}")
            }
            println()
        }
}