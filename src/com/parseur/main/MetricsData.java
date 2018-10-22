package com.parseur.main;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetricsData {
	private Map<String, String> allMetrics;
	
	public MetricsData() {
		allMetrics = Collections.synchronizedMap( new LinkedHashMap<String, String>(10) );
		
		// Initialisation de toutes les métriques à 0 dans le dictionnaire
	    for(MetricNames metricName : MetricNames.values()){
	    	allMetrics.put(metricName.toString(), "0");
	    }
	}
	
	// Modifie les metriques stockees dans "allMetrics"
	public void updateMetrics(String[] args) {
		MetricNames[] metricNames = MetricNames.values();
		
		if (args.length == metricNames.length) {
			int counter = 0;
			
			for(MetricNames metric : metricNames){
				if(args[counter] instanceof String) { // verifie si l'argument est valide (String)
			    	allMetrics.put(metric.toString(), args[counter]);
				}
				
				counter++;
		    }
			
		} else throw new IllegalArgumentException();
	}
	
	public Map<String, String> getDict() {
		return allMetrics;
	}
	
	public String toString() {
		return allMetrics.toString();
	}
}
