package pmetrics.extractor

import org.eclipse.jdt.core.dom.*
import pmetrics.features.NumericFeature
import pmetrics.features.StringFeature
import pmetrics.featureset.FeatureSet
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

object TermFrequencyFeatureExtractor: FeatureExtractor() {
    override fun extract(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet) {
        if (cus.isEmpty()) return

        val indvcounts = cus.map { TermFrequencyCounter.count(it) }
        val counts = indvcounts.flatMap { it.entries }
            .stream()
            .collect(Collectors.toMap(
                { e: Map.Entry<String, Int> -> e.key },
                { e: Map.Entry<String, Int> -> e.value },
                { l, r -> l + r }
            ))

        val totalTermCount = counts.map { it.value }.sum()

        // No. of terms
        pfs.add(NumericFeature("LEX_CU_TERMCOUNT", (counts.size.toDouble())))

        // Most common term
        val mct = counts.entries
            .sortedByDescending { it.value }
            .first()

//        pfs.add(StringFeature("LEX_CU_TERM_MOSTCOMMON_NAME", mct.key))
//        pfs.add(NumericFeature("LEX_CU_TERM_MOSTCOMMON_COUNT", mct.value))

        // Count of each term
        for ((name, count) in counts) {
            pfs.add(NumericFeature("LEX_CU_TERM_${name}_COUNT", (count.toDouble())))
        }

        // Percentage breakdown of terms
        for ((name, count) in counts) {
            val perc = count.toDouble().div(totalTermCount)
            pfs.add(NumericFeature("LEX_CU_TERM_${name}_PERC", (perc.toDouble())))
        }

        // TF-IDF
        val docCount = cus.count()
        for ((name, count) in counts) {
            val presentCount = indvcounts.count { it.containsKey(name) }
            val tf = count.toDouble() / totalTermCount
            val idf = presentCount.toDouble() / docCount
            val tfidf = tf * idf

            pfs.add(NumericFeature("LEX_CU_TERM_${name}_TFIDF", (tfidf.toDouble())))
        }
    }

    object TermFrequencyCounter {
        fun count(cu: CompilationUnit): Map<String, Int> {
            val visitor = NameCountingVisitor()
            cu.accept(visitor)
            return visitor.counts
                .map { it.key to it.value.get() }
                .toMap()
        }

        class NameCountingVisitor: ASTVisitor() {
            val counts = mutableMapOf<String, AtomicInteger>()

            override fun visit(node: SimpleName): Boolean {
                counts.getOrPut(node.identifier) { AtomicInteger(0) }
                    .incrementAndGet()
                return true
            }
        }
    }
}