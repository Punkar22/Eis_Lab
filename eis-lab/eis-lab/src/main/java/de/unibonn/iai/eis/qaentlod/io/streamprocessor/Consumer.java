package de.unibonn.iai.eis.qaentlod.io.streamprocessor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.hp.hpl.jena.sparql.core.Quad;

import de.unibonn.iai.eis.qaentlod.datatypes.Object2Quad;
import de.unibonn.iai.eis.qaentlod.io.Main;
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
			this.streamManager.digMetric.compute(value);
			this.streamManager.autMetric.compute(value);
			//this.streamManager.freeMetric.compute(value);
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

		Dimension dimension2 = new Dimension();
		dimension2.setName("Free of Error");
		dimension2.getMetrics().add(metric3);

		Results results = new Results();
		results.setUrl(this.producer.getServiceUrl());
		results.getDimensions().add(dimension1);
		results.getDimensions().add(dimension2);
		
		try {
			Main main = new Main();

			ResultDataSet resultToWrite = ResultsHelper.read(main.loadConfiguration());
					
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
				UtilMail.sendMail(this.loadMailDefault());
			
		} catch (Exception e) {
			System.out.println("****** Can't save the result because: "
					+ e.toString());
		}
	}

	/**
	 * This method read from a local file the directory where is saved the Dataset processed
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	public String loadConfiguration() throws IOException {

		String result = "";
		Properties prop = new Properties();
		String propFileName = "config.properties";

		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		// get the property value and print it out
		String dataBase = prop.getProperty("dataBase");

		result = dataBase;
		return result;
	}
	
	/**
	 * This method read from a local file the directory where is saved the Dataset processed
	 * @return The path of the file in the server
	 * @throws IOException
	 */
	public String loadMailDefault() throws IOException {

		String result = "";
		Properties prop = new Properties();
		String propFileName = "config.properties";

		InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream(propFileName);
		prop.load(inputStream);
		if (inputStream == null) {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		// get the property value and print it out
		String dataBase = prop.getProperty("defaultMail");

		result = dataBase;
		return result;
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

