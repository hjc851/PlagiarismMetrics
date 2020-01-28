package pmetrics.featureset

import pmetrics.features.Feature
import pmetrics.features.FeatureDescriptor

class FeatureSetCollisionException(val name: String, val existing: FeatureDescriptor<*>, val new: FeatureDescriptor<*>): Exception()
class FeatureNameCollisionException(val name: String, val existing: Feature<*>, val new: Feature<*>): Exception()
class UnknownFeatureName(val name: String): Exception()