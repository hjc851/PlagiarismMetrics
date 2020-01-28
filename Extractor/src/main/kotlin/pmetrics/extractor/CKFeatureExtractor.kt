package pmetrics.extractor

import com.github.mauricioaniche.ck.CKNotifier
import com.github.mauricioaniche.ck.CKVisitor
import com.github.mauricioaniche.ck.metric.ClassLevelMetric
import com.github.mauricioaniche.ck.metric.MethodLevelMetric
import com.github.mauricioaniche.ck.util.MetricsFinder
import com.github.mauricioaniche.ck.util.ResultWriter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.eclipse.jdt.core.dom.CompilationUnit
import pmetrics.features.NumericFeature
import pmetrics.featureset.FeatureSet
import java.nio.file.Files
import java.util.concurrent.Callable
import java.util.stream.IntStream
import kotlin.streams.toList

object CKFeatureExtractor: FeatureExtractor() {
    override fun extract(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet) {
        // Metrics to extract
        val finder = MetricsFinder()
        val classMetrics = Callable<List<ClassLevelMetric>> { finder.allClassLevelMetrics() }
        val methodMetrics = Callable<List<MethodLevelMetric>> { finder.allMethodLevelMetrics() }

        // Handle storing the results intermediately
        val tdir = Files.createTempDirectory("ckfeature")
        val cm = tdir.resolve("class.csv")
        val mm = tdir.resolve("method.csv")
        val vm = tdir.resolve("variable.csv")
        val fm = tdir.resolve("field.csv")

        val writer = ResultWriter (
            cm.toString(),
            mm.toString(),
            vm.toString(),
            fm.toString()
        )

        val notifier = CKNotifier { result ->
            writer.printResult(result)
        }

        // Extract the metrics
        for (cu in cus) {
            val visitor = CKVisitor("", cu, classMetrics, methodMetrics)
            cu.accept(visitor)

            val collectedClasses = visitor.collectedClasses
            for (collectedClass in collectedClasses) {
                notifier.notify(collectedClass)
            }
        }
        writer.flushAndClose()

        // Put in dataset
        CSVParser(Files.newBufferedReader(cm), CSVFormat.DEFAULT).use { reader ->
            val records = reader.records.stream()
                .skip(1)
                .toList()

            addClassMetrics(records, pfs)
        }

        CSVParser(Files.newBufferedReader(mm), CSVFormat.DEFAULT).use { reader ->
            val records = reader.records.stream()
                .skip(1)
                .toList()

            addMethodMetrics(records, pfs)
        }

        CSVParser(Files.newBufferedReader(vm), CSVFormat.DEFAULT).use { reader ->
            val records = reader.records.stream()
                .skip(1)
                .toList()

            addVariableMetrics(records, pfs)
        }

        CSVParser(Files.newBufferedReader(fm), CSVFormat.DEFAULT).use { reader ->
            val records = reader.records.stream()
                .skip(1)
                .toList()

            addFieldMetrics(records, pfs)
        }

        // Cleanup
        Files.walk(tdir)
            .sorted(Comparator.reverseOrder())
            .forEach(Files::delete)
    }

    private fun addClassMetrics(records: List<CSVRecord>, projectFeatures: FeatureSet.ProjectFeatureSet) {
        val classNames = records.stream()
            .map { it -> it.get(1) }
            .toList()

        // Helper Function
        val mmtapcfunction = { ridx: Int, str: String ->
            val values = records.stream()
                .map { it -> Integer.parseInt(it.get(ridx!!)) }
                .toList()

            val min = NumericFeature("CK_CLASS_" + str + "_MIN", (values.stream().mapToInt { it -> it }.min().orElse(0).toDouble()))
            val max = NumericFeature("CK_CLASS_" + str + "_MAX", (values.stream().mapToInt { it -> it }.max().orElse(0).toDouble()))
            val total = NumericFeature("CK_CLASS_" + str + "_TOTAL", (values.stream().mapToInt { it -> it }.sum().toDouble()))
            val average = NumericFeature("CK_CLASS_" + str + "_AVERAGE", (values.stream().mapToInt { it -> it }.average().orElse(0.0)))
            val percalss = IntStream.range(0, values.size)
                .mapToObj { index -> NumericFeature("CK_CLASS_" + str + "_FOR_" + classNames[index], ( values[index].toDouble())) }
                .toList()

            projectFeatures.add(min)
            projectFeatures.add(max)
            projectFeatures.add(total)
            projectFeatures.add(average)
            projectFeatures.addAll(percalss)
        }

        // Coupling between Classes (CBOS)
        mmtapcfunction(3, "CBC")

        // Weighted Method Count
        mmtapcfunction(4, "WMC")

        // Depth of Inheritance Tree
        mmtapcfunction(5, "DIT")

        // Response for Class
        mmtapcfunction(6, "RFC")

        // Lack of Cohension of Methods
        mmtapcfunction(7, "LCOM")

        // Methods
        mmtapcfunction(8, "TOTALMETHOD")
        mmtapcfunction(9, "STATICMETHOD")
        mmtapcfunction(10, "PUBLICMETHOD")
        mmtapcfunction(11, "PRIVATEMETHOD")
        mmtapcfunction(12, "PROTECTEDMETHOD")
        mmtapcfunction(13, "DEFAULTMETHOD")
        mmtapcfunction(14, "ABSTRACTMETHOD")
        mmtapcfunction(15, "FINALMETHOD")
        mmtapcfunction(16, "SYNCHRONISEDMETHOD")

        // Fields
        mmtapcfunction(17, "TOTALFIELDS")
        mmtapcfunction(18, "STATICFIELDS")
        mmtapcfunction(19, "PUBLICFIELDS")
        mmtapcfunction(20, "PRIVATEFIELDS")
        mmtapcfunction(21, "PROTECTEDFIELDS")
        mmtapcfunction(23, "FINALFIELDS")

        // Number of Static Invocations
        mmtapcfunction(25, "NOSI")

        // Lines of Code
        mmtapcfunction(26, "LOC")

        // Statement & Expression Counts
        mmtapcfunction(27, "RETURNQTY")
        mmtapcfunction(28, "LOOPQTY")
        mmtapcfunction(29, "COMPARISONQTY")
        mmtapcfunction(30, "TRYCATCHQTY")
        mmtapcfunction(31, "PARENTHESISEDQTY")
        mmtapcfunction(32, "STRINGLITQTY")
        mmtapcfunction(33, "NUMBERLITQTY")
        mmtapcfunction(34, "ASSIGNMENTQTY")
        mmtapcfunction(35, "OPERATORQTY")
        mmtapcfunction(36, "VARIABLESQTY")
        mmtapcfunction(37, "MAXBLOCKDEPTH")
        mmtapcfunction(38, "ANONCLASSQTY")
        mmtapcfunction(39, "SUBCLASSQTY")
        mmtapcfunction(40, "LAMBDAQTY")
        mmtapcfunction(41, "UNIQUEWORDS")
        mmtapcfunction(42, "MODIFERS")
    }

    private fun addMethodMetrics(records: List<CSVRecord>, projectFeatures: FeatureSet.ProjectFeatureSet) {
        val methodName = records.stream()
            .map { it ->
                "${it.get(1)}--${it.get(2)}--${it.get(26)}"
                    .replace("<", "").replace(">", "").replace("[", "").replace("]", "").replace("(", "").replace(")", "").replace("/", "").replace(".", "").replace(",", "")
            }
            .toList()

        // Helper Function
        val mmtapcfunction = { ridx: Int, str: String ->
            val values = records.stream()
                .map { it -> Integer.parseInt(it.get(ridx!!)) }
                .toList()

            val min = NumericFeature("CK_METHOD_" + str + "_MIN", (values.stream().mapToInt { it -> it }.min().orElse(0).toDouble()))
            val max = NumericFeature("CK_METHOD_" + str + "_MAX", (values.stream().mapToInt { it -> it }.max().orElse(0).toDouble()))
            val total = NumericFeature("CK_METHOD_" + str + "_TOTAL", (values.stream().mapToInt { it -> it }.sum().toDouble()))
            val average = NumericFeature("CK_METHOD_" + str + "_AVERAGE", (values.stream().mapToInt { it -> it }.average().orElse(0.0)))
            val percalss = IntStream.range(0, values.size)
                .mapToObj { index -> NumericFeature("CK_METHOD_" + str + "_FOR_" + methodName[index], (values[index].toDouble())) }
                .toList()

            projectFeatures.add(min)
            projectFeatures.add(max)
            projectFeatures.add(total)
            projectFeatures.add(average)
            projectFeatures.addAll(percalss)
        }

        // Features
        mmtapcfunction(5, "CBC")
        mmtapcfunction(6, "WMC")
        mmtapcfunction(7, "RFC")
        mmtapcfunction(8, "LOC")
        mmtapcfunction(9, "RETURNQTY")
        mmtapcfunction(10, "VARIABLEQTY")
        mmtapcfunction(11, "PARAMETERQTY")
        mmtapcfunction(13, "LOOPQTY")
        mmtapcfunction(14, "COMPARISONQTY")
        mmtapcfunction(15, "TRYCATCHQTY")
        mmtapcfunction(16, "PARENTHESISEDQTY")
        mmtapcfunction(17, "STRINGLITQTY")
        mmtapcfunction(18, "NUMBERLITQTY")
        mmtapcfunction(19, "ASSIGNMENTQTY")
        mmtapcfunction(20, "OPERATORQTY")
        mmtapcfunction(21, "BLOCKDEPTH")
        mmtapcfunction(22, "ANONCLASSQTY")
        mmtapcfunction(23, "SUBCLASSQTY")
        mmtapcfunction(24, "LAMBDAQTY")
        mmtapcfunction(25, "UNIQUEWORDS")
        mmtapcfunction(26, "MODIFIERS")
    }

    private fun addVariableMetrics(records: List<CSVRecord>, projectFeatures: FeatureSet.ProjectFeatureSet) {
        val groups = records.groupBy {
            "${it.get(1)}_${it.get(2)}".replace("<", "").replace(">", "").replace("[", "").replace("]", "").replace("(", "").replace(")", "").replace("/", "").replace(".", "").replace(",", "")
        }

        for ((key, grecords) in groups) {
            val varcount = NumericFeature("CK_VARIABLE_${key}_VARCOUNT", (grecords.count().toDouble()))
            projectFeatures.add(varcount)

            val counts = grecords.map { it.get(4).toInt() }
            val mincount = NumericFeature("CK_VARIABLE_${key}_VARUSAGECOUNT_MIN", (counts.sorted().first().toDouble()))
            val maxcount = NumericFeature("CK_VARIABLE_${key}_VARUSAGECOUNT_MAX", (counts.sortedDescending().first().toDouble()))
            val avgcount = NumericFeature("CK_VARIABLE_${key}_VARUSAGECOUNT_AVF", (counts.average()))

            projectFeatures.add(mincount)
            projectFeatures.add(maxcount)
            projectFeatures.add(avgcount)

            for (grecord in grecords) {
                val name = grecord.get(3)
                val count = grecord.get(4).toInt()

                val usagecount = NumericFeature("CK_VARIABLE_${key}_${name}_VARUSAGE_COUNT", (count.toDouble()))
                projectFeatures.add(usagecount)
            }
        }
    }

    private fun addFieldMetrics(records: List<CSVRecord>, projectFeatures: FeatureSet.ProjectFeatureSet) {
        val groups = records.groupBy {
            "${it.get(1)}_${it.get(2)}".replace("<", "").replace(">", "").replace("[", "").replace("]", "").replace("(", "").replace(")", "").replace("/", "").replace(".", "").replace(",", "")
        }

        for ((key, grecords) in groups) {
            val varcount = NumericFeature("CK_FIELD_${key}_VARCOUNT", (grecords.count().toDouble()))
            projectFeatures.add(varcount)

            val counts = grecords.map { it.get(4).toInt() }
            val mincount = NumericFeature("CK_FIELD_${key}_VARUSAGECOUNT_MIN", (counts.sorted().first().toDouble()))
            val maxcount = NumericFeature("CK_FIELD_${key}_VARUSAGECOUNT_MAX", (counts.sortedDescending().first().toDouble()))
            val avgcount = NumericFeature("CK_FIELD_${key}_VARUSAGECOUNT_AVG", (counts.average().toDouble()))

            projectFeatures.add(mincount)
            projectFeatures.add(maxcount)
            projectFeatures.add(avgcount)

            for (grecord in grecords) {
                val name = grecord.get(3)
                val count = grecord.get(4).toInt()

                val usagecount = NumericFeature("CK_FIELD_${key}_${name}_VARUSAGE_COUNT", (count.toDouble()))
                projectFeatures.add(usagecount)
            }
        }
    }
}