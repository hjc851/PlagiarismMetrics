package pmetrics.extractor

import org.eclipse.jdt.core.dom.CompilationUnit
import pmetrics.featureset.FeatureSet

abstract class FeatureExtractor {
    abstract fun extract(cus: List<CompilationUnit>, pfs: FeatureSet.ProjectFeatureSet)
}