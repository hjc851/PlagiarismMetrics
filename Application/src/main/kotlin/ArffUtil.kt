import weka.core.Instances
import weka.core.converters.ArffLoader
import java.nio.file.Path

object ArffUtil {
    fun load(path: Path): Instances {
        val loader = ArffLoader()
        loader.setFile(path.toFile())
        val data = loader.dataSet
        data.setClassIndex(data.numAttributes()-1)
        return data
    }
}