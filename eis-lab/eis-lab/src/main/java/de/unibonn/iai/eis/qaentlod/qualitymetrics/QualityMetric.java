package de.unibonn.iai.eis.qaentlod.qualitymetrics;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.qaentlod.datatypes.ProblemList;

/**
 * @author jdebattist
 * 
 */
public interface QualityMetric {

	/**
	 * This method should compute the metric.
	 * 
	 * @param The Quad <s,p,o,c> passed by the stream processor to the quality metric
	 */
	void compute(Quad quad);

	/**
	 * @return the value computed by the Quality Metric
	 */
	double metricValue();

	/**
	 * This method will return daQ triples which will be stored in the dataset
	 * QualityGraph.
	 * 
	 * @return a list of daQ triples
	 */
	List<Statement> toDAQTriples();

	/**
	 * @return returns the daQ URI of the Quality Metric
	 */
	Resource getMetricURI();
	
	/**
	 * @return returns a typed ProblemList which will be used to create a "quality report" of the metric.
	 */
	ProblemList<?> getQualityProblems();
}
