package pmetrics

import jcinterpret.parser.FileUtils
import jcinterpret.parser.Parser
import pmetrics.extractor.ASTFeatureExtractor
import pmetrics.extractor.CKFeatureExtractor
import pmetrics.extractor.TermFrequencyFeatureExtractor
import pmetrics.extractor.TokenFeatureExtractor
import pmetrics.features.NumericFeature
import pmetrics.featureset.FeatureNameCollisionException
import pmetrics.featureset.FeatureSet
import weka.core.converters.ArffLoader
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import kotlin.streams.toList

fun main(args: Array<String>) {
    val root = Paths.get(args[0])
    val libs = args[1].split(";")
        .map { Paths.get(it) }

    val file = root.resolve("features-static.arff")
    Files.deleteIfExists(file)
    Files.createFile(file)

    val projects = Files.list(root)
        .filter { Files.isDirectory(it) && !Files.isHidden(it) }
        .toList()
        .sortedBy { it.fileName.toString() }

    val classes = projects.map { it.fileName.toString() }
        .map { it.split("-")[0] }
        .toSet()

    val extractors = listOf (
        ASTFeatureExtractor,
        CKFeatureExtractor,
        TermFrequencyFeatureExtractor
    )

    FeatureSet().use { fs ->
        val count = projects.count()
        println("Processing $count projects ...")

        val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-4)

        val futures = projects.map { project ->
            CompletableFuture.runAsync(Runnable {
                val id = project.fileName.toString()
                val pfs = fs.getFeatureSet(id)

                val srcs = FileUtils.listFiles(project, ".java")
                val dirs = FileUtils.listDirectories(project)
                val cus = Parser.parse(srcs, libs, dirs)

                for (extractor in extractors) {
                    try {
                        extractor.extract(cus, pfs)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw e
                    }
                }

                fs.cacheFeatureSet(id)
                println("\t$id")
            }, pool)
        }

        futures.forEach { it.get() }

        Files.newBufferedWriter(file).use { fw ->
            val fout = PrintWriter(fw)
            val featureNames = fs.getFeatureNames().toList()

            println("Writing headers ...")

            fout.println("@RELATION plagiarismmetrics")
            fout.println()
            fout.printf("@ATTRIBUTE id {%s}\n", fs.getFeatureSetIds().joinToString(","))
            for ((name, desc) in fs.getDescriptors()) {
                fout.println("@ATTRIBUTE $name ${desc.descriptor()}")

            }
            fout.printf("@ATTRIBUTE cls {%s}\n", classes.joinToString(","))
            fout.println()
            fout.println("@DATA")

            println("Writing attributes ...")
            fs.getFeatureSetIds().forEachIndexed { index, id ->
                println("\t$id - ${index+1} of $count")

                val pfs = fs.getFeatureSet(id)
                val features = pfs.features()

                fout.print("{0 $id,")

                for (i in 0 until featureNames.count()) {
                    val name = featureNames[i]
                    val feature = features[name]

                    if (feature is NumericFeature) {
                        fout.printf("%d %.4f,", i+1, feature.value.toDouble())
                    }
                }

                val cls = id.split("-")[0]
                fout.println("${featureNames.size+1} $cls}")
                fs.cacheFeatureSet(id)
            }
        }
    }

    try {
        println("Validating file format ...")
        val loader = ArffLoader()
        loader.setFile(file.toFile())
        val instances = loader.dataSet
    } catch (e: Exception) {
        e.printStackTrace()
    }

    println("Done!")
    System.exit(0)
}