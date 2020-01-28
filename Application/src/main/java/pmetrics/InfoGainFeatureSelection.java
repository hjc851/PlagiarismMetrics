package pmetrics;

import weka.attributeSelection.*;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

public class InfoGainFeatureSelection {
    public static void main(String[] args) throws Exception {
        Path dsf = Paths.get(args[0]);
        Path out = dsf.getParent().resolve("reduced.arff");

        // Create the out file
        Files.deleteIfExists(out);
        Files.createFile(out);

        // Setup he loader
        ArffLoader loader = new ArffLoader();
        loader.setSource(dsf.toUri().toURL());

        // Load the data
        System.out.println("Loading data ...");
        Instances ds = loader.getDataSet();
        ds.setClassIndex(0);
        System.out.println("Loaded " + ds.numAttributes() + " attributes.");

        // Setup the reduction

        GainRatioAttributeEval eval1 = new GainRatioAttributeEval();
        Ranker search1 = new Ranker();
        search1.setOptions(new String[] { "-T", "0.01" });	// information gain threshold
        AttributeSelection attSelect1 = new AttributeSelection();
        attSelect1.setEvaluator(eval1);
        attSelect1.setSearch(search1);

        // Perform the first reduction
        System.out.println("Performing GainRatio dimensionality reduction ...");
        attSelect1.SelectAttributes(ds);
        Instances reducedData = attSelect1.reduceDimensionality(ds);
        System.out.println("Reduction 1 removed " + (ds.numAttributes() - reducedData.numAttributes()) + " attributes.");

        // Setup the second reduction
        CorrelationAttributeEval eval2 = new CorrelationAttributeEval();
        Ranker search2 = new Ranker();
        search2.setOptions(new String[] { "-T", "0.01" });	// information gain threshold
        AttributeSelection attSelect2 = new AttributeSelection();
        attSelect2.setEvaluator(eval2);
        attSelect2.setSearch(search2);

        // Perform the second reduction
        System.out.println("Performing Correlation dimensionality reduction ...");
        attSelect2.SelectAttributes(reducedData);
        int lastAttrCount = reducedData.numAttributes();
        reducedData = attSelect2.reduceDimensionality(reducedData);
        System.out.println("Reduction 2 removed " + (lastAttrCount - reducedData.numAttributes()) + " attributes.");

        // Sace the data
        System.out.println("Saving data to " + out + " ...");
        reducedData.setRelationName(ds.relationName());
        ArffSaver saver = new ArffSaver();
        OutputStream fos = Files.newOutputStream(out);
        saver.setDestination(fos);
        saver.setInstances(reducedData);
        saver.writeBatch();

        fos.flush();
        fos.close();

        System.out.println("Left " + reducedData.numAttributes() + " attributes");
        System.out.println("Finished");
    }
}
