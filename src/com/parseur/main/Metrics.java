package com.parseur.main;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

public class Metrics {	
	private ArrayList<Classe> classes;
	private Classe c;
	
	public Metrics (ArrayList<Classe> classes) {
		this.classes = classes;
	}

	private String computeANA() {
		return "5";
	}

	private String computeNOM() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeNOA() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeITC() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeETC() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeCAC() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeDIT() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeCLD() {
		// TODO Auto-generated method stub
		return "0";
	}

	private String computeNOC() {
		DefaultListModel<StringDetail> subClasses = c.getSubClasses();
		if( subClasses.firstElement().getValue().length() != 0){
			return String.valueOf(subClasses.getSize());
		}
		return "0";
	}

	private String computeNOD() {
		// TODO Auto-generated method stub
		return "0";
	}

	public String[] computeMetricsOf(Classe currentClass) {
		this.c = currentClass; // on stocke la classe actuelle dans Metrics pour qu'elle soit accessible par toutes les méthodes "ComputeXXX"
		
		String[] result = {computeANA(),
						   computeNOM(),
						   computeNOA(),
						   computeITC(),
						   computeETC(),
						   computeCAC(),
						   computeDIT(),
						   computeCLD(),
						   computeNOC(),
						   computeNOD()};
		return result;
		
	}
}
