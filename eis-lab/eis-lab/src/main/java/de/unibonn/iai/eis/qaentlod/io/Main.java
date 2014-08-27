/**
 * 
 */
package de.unibonn.iai.eis.qaentlod.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import de.unibonn.iai.eis.qaentlod.io.streamprocessor.Consumer;
import de.unibonn.iai.eis.qaentlod.io.streamprocessor.Producer;
import de.unibonn.iai.eis.qaentlod.io.streamprocessor.StreamManager;
import de.unibonn.iai.eis.qaentlod.io.utilities.DataSetResults;
import de.unibonn.iai.eis.qaentlod.io.utilities.Menus;
import de.unibonn.iai.eis.qaentlod.util.Dimension;
import de.unibonn.iai.eis.qaentlod.util.Metrics;
import de.unibonn.iai.eis.qaentlod.util.ResultDataSet;
import de.unibonn.iai.eis.qaentlod.util.Results;
import de.unibonn.iai.eis.qaentlod.util.ResultsHelper;


/**
 * This class is the main class, it is in charge to load the data and create the
 * processes and clients to run faster.
 * 
 * @author Carlos Montoya
 */
public class Main {

	/**
	 * Variable to adjust the increment value of the call to the service
	 */
	private static int INCREMENT = 10000;

	private static String serviceUrl = "http://protein.bio2rdf.org/sparql";

	private static String fileName = "C:\\Users\\RaufA\\Desktop\\Lab\\results.xml";
	private static List<DataSetResults> results;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//int opt1 = Menus.menuMain();

		//String url = serviceUrl;
		
		String url = Menus.menuUrl();
		serviceUrl = url;

		String mail = Menus.menuMail();
		
		StreamManager streamQuads = new StreamManager();
		Producer p1 = new Producer(streamQuads, INCREMENT, serviceUrl);
		Consumer c1 = new Consumer(streamQuads, p1);
		c1.setMail(mail);
		p1.start();
		c1.start();
		
		/*while( c1.isRunning() ){
			System.out.print("");
		}*/
		
		System.out.println("The value of the metrics ");

		//writeFile(streamQuads);
	}
	


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
		System.out.println(dataBase);
		return result;
	}

	
	public static void writeFile(StreamManager streamQuads){
		System.out.println("entro");
		results = new ArrayList<DataSetResults>();
		DataSetResults result = new DataSetResults(serviceUrl, streamQuads.digMetric,
				streamQuads.autMetric, streamQuads.freeMetric, streamQuads.measurAbility);
		results.add(result);

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
		metric4.setValue(Double.toString(result.getMeasurMetric().metricValue()));
		
		Dimension dimension2 = new Dimension();
		dimension2.setName("Free of Error");
		dimension2.getMetrics().add(metric3);
		
		Dimension dimension3 = new Dimension();
		dimension3.setName("Measurability");
		dimension3.getMetrics().add(metric4);

		Results results = new Results();
		results.setUrl(serviceUrl);
		results.getDimensions().add(dimension1);
		results.getDimensions().add(dimension2);
		
		try {

			Main main = new Main();

			ResultDataSet resultToWrite = ResultsHelper.read(main.loadConfiguration());
					
			resultToWrite.setLastDate(new Date());
			resultToWrite.getResults().add(results);
			
			ResultsHelper.write(resultToWrite, fileName);

			
		} catch (Exception e) {
			System.out.println("****** Can't save the result because: "
					+ e.toString());
		}
	}
}
