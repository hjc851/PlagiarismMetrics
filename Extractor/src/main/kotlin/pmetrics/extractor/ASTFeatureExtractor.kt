package pmetrics.extractor

import org.eclipse.jdt.core.dom.*
import pmetrics.features.NumericFeature
import pmetrics.featureset.FeatureSet

object ASTFeatureExtractor: FeatureExtractor() {
    override fun extract(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet) {
        kgramFeatures(cus, pfs)
        sequenceFeatures(cus, pfs)
    }

    private fun kgramFeatures(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet) {
        val grams = cus.map { ASTGramExtractor.extract(3, it) }

        val sizes = grams.map { it.size }

        val gramCount = sizes.sum()
        val gramPerCU = gramCount.toDouble().div(cus.size)
        val minCount = sizes.min() ?: 0
        val maxCount = sizes.max() ?: 0

        pfs.add(NumericFeature("AST_GRAMS_COUNT", (gramCount.toDouble())))
        pfs.add(NumericFeature("AST_GRAMS_PER_CU", (gramPerCU)))
        pfs.add(NumericFeature("AST_GRAMS_MIN", (minCount.toDouble())))
        pfs.add(NumericFeature("AST_GRAMS_MAX", (maxCount.toDouble())))

        val allGrams = grams.flatten()
        val gramsByType = allGrams.groupBy { it.nodes.joinToString("_") { it.javaClass.simpleName } }

        for ((type, grams) in gramsByType) {
            val perc = grams.size.toDouble().div(allGrams.size)

//            pfs.add(NumericFeature("AST_GRAMS_${type}_PERC", perc))
            pfs.add(NumericFeature("AST_GRAMS_${type}_LOG", Math.log(perc)))
//            pfs.add(NumericFeature("AST_GRAMS_${type}_COUNT", grams.size))
        }
    }

    private fun sequenceFeatures(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet) {
        val sequences = cus.map { ASTNodeSequenceExtractor.extract(it) }
        val sizes = sequences.map { it.size }

        val scount = sizes.sum()
        val saverage = scount.toDouble().div(cus.size)
        val smin = sizes.min() ?: 0
        val smax = sizes.max() ?: 0

        pfs.add(NumericFeature("AST_SEQUENCE_COUNT", (scount.toDouble())))
        pfs.add(NumericFeature("AST_SEQUENCE_PER_CU", (saverage.toDouble())))
        pfs.add(NumericFeature("AST_SEQUENCE_MIN", (smin.toDouble())))
        pfs.add(NumericFeature("AST_SEQUENCE_MAX", (smax.toDouble())))

        val all = sequences.flatten()
        val groupedByType = all.groupBy { it.javaClass.simpleName }
        for ((type, group) in groupedByType) {
            val perc = group.size.toDouble().div(all.size)

//            pfs.add(NumericFeature("AST_SEQUENCE_${type}_PERC", perc))
            pfs.add(NumericFeature("AST_SEQUENCE_${type}_LOG", Math.log(perc)))
//            pfs.add(NumericFeature("AST_SEQUENCE_${type}_COUNT", group.size))
        }
    }
    
    class Gram(val nodes: Array<ASTNode>)
    
    object ASTGramExtractor {
        fun extract(k: Int, node: ASTNode): List<Gram> {
            val visitor = GramVisitor(k)
            node.accept(visitor)
            return visitor.grams
        }

        class GramVisitor(val size: Int): ASTVisitor() {
            val grams = mutableListOf<Gram>()
            var currentDepth = 0

            override fun preVisit(node: ASTNode) {
                val gramNodes = Array<ASTNode?>(size) { null }
                gramNodes[0] = node
                var lastNode: ASTNode = node

                for (i in 1 until size) {
                    val nextNode = lastNode.parent
                    gramNodes[i] = nextNode

                    if (nextNode == null) return
                    lastNode = nextNode
                }

                grams.add(Gram(gramNodes.requireNoNulls()))
            }

        }
    }

    object ASTNodeSequenceExtractor {
        fun extract(root: ASTNode): List<ASTNode> {
            val visitor = SequenceVisitor()
            root.accept(visitor)
            return visitor.nodes
        }

        class SequenceVisitor: ASTVisitor() {
            val nodes = mutableListOf<ASTNode>()

            override fun preVisit(node: ASTNode) {
                nodes.add(node)
            }
        }
    }
}