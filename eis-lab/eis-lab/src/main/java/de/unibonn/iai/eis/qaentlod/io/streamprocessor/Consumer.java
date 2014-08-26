package de.unibonn.iai.eis.qaentlod.io.streamprocessor;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.qaentlod.datatypes.Object2Quad;
import de.unibonn.iai.eis.qaentlod.io.utilities.ConfigurationLoader;
import de.unibonn.iai.eis.qaentlod.io.utilities.DataSetResults;
import de.unibonn.iai.eis.qaentlod.io.utilities.UtilMail;
import de.unibonn.iai.eis.qaentlod.util.Dimension;
import de.unibonn.iai.eis.qaentlod.util.Metrics;
import de.unibonn.iai.eis.qaentlod.util.ResultDataSet;
import de.unibonn.iai.eis.qaentlod.util.Results;
import de.unibonn.iai.eis.qaentlod.util.ResultsHelper;

/**
 * This class read all the values produce by the producer an execute the metrics over the quad obtained
 * @author Carlos
 */
public class Consumer extends Thread {
	/**
	 * 
	 */
	private StreamManager streamManager;
	private Producer producer;
	private int cont = 0;
	private boolean running;
	private static String fileName = "C:\\Lab\\results.xml";
	private static List<DataSetResults> results;
	private String mail;
	/**
	 * Creator of the class
	 * @param streamManager, this class is the stream Manager to put all the values obtain
	 * @param producer, producer of the events to be read
	 */
	public Consumer(StreamManager streamManager, Producer producer) {
		this.streamManager = streamManager;
		this.producer = producer;
	}

	/**
	 * This method start the thread to run the consumer
	 */
	public void run() {
		Quad value;
		int contAux=0;
		this.setRunning(true);
		//Run the consumer while the producer is publishing data
		while(producer.isRunning()){
			value = new Object2Quad(streamManager.get()).getStatement();
			//Here we compute all the metrics
			//Verifiability Metrics
			this.streamManager.digMetric.compute(value);
			this.streamManager.autMetric.compute(value);
			//Free of error metrics
			this.streamManager.freeMetric.compute(value);
			//Aditional Metrics
			
			setCont(getCont() + 1);
			contAux++;
			if(contAux == 10000){
				contAux = 0;
				System.out.println("Read 10000 triples");
			}
		}
		this.writeFile();
		//System.out.println("The number of quads read is: " + cont);
		//this.stop();
		this.setRunning(false);
		//this.writeFile();
	}

	/**
	 * 
	 */
	public void writeFile(){
		Consumer.setResults(new ArrayList<DataSetResults>());
		DataSetResults result = new DataSetResults(this.producer.getServiceUrl(), streamManager.digMetric,
				streamManager.autMetric, streamManager.freeMetric);
		getResults().add(result);

		Metrics metric1 = new Metrics();
		metric1.setName("Authenticity of the Dataset");
		metric1.setValue(Double.toString(result.getAutMetric().metricValue()));

		Metrics metric2 = new Metrics();
		metric2.setName("Digital Signatures");
		metric2.setValue(Double.toString(result.getDigMetric().metricValue()));

		Dimension dimension1 = new Dimension();
		dimension1.setName("Verifiability");
		dimension1.getMetrics().add(metric1);
		dimension1.getMetrics().add(metric2);

		Metrics metric3 = new Metrics();
		metric3.setName("Free of Error");
		metric3.setValue(Double.toString(result.getFreeMetric().metricValue()));
		
		Metrics metric4 = new Metrics();
		metric4.setName("Measurability");
		//Stril you have to complete for your value
		metric4.setValue("0.0");
		
		Dimension dimension2 = new Dimension();
		dimension2.setName("Free of Error");
		dimension2.getMetrics().add(metric3);
		
		Dimension dimension3 = new Dimension();
		dimension3.setName("Measurability");
		dimension3.getMetrics().add(metric4);

		Results results = new Results();
		results.setUrl(this.producer.getServiceUrl());
		results.getDimensions().add(dimension1);
		results.getDimensions().add(dimension2);
		results.getDimensions().add(dimension3);
		
		
		try {
			ConfigurationLoader conf = new ConfigurationLoader();

			ResultDataSet resultToWrite = ResultsHelper.read(conf.loadDataBase());
					
			resultToWrite.setLastDate(new Date());
			boolean modified = false;
			for (Results resultAux : resultToWrite.getResults()) {
				if(resultAux.getUrl().equals(this.producer.getServiceUrl())){
					resultAux = results;
					modified = true;
				}
			}
			
			if(!modified)
				resultToWrite.getResults().add(results);
			
			ResultsHelper.write(resultToWrite, fileName);

			if(this.getMail() != null)
				UtilMail.sendMail(this.getMail());
			else
				UtilMail.sendMail(conf.loadMailDefault());
			
		} catch (Exception e) {
			System.out.println("****** Can't save the result because: "
					+ e.toString());
		}
	}

	
	
	
	/**
	 * @return the running
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * @param running the running to set
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * @return the results
	 */
	public static List<DataSetResults> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public static void setResults(List<DataSetResults> results) {
		Consumer.results = results;
	}

	/**
	 * @return the cont
	 */
	public int getCont() {
		return cont;
	}

	/**
	 * @param cont the cont to set
	 */
	public void setCont(int cont) {
		this.cont = cont;
	}
}

