package com.parseur.main;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

public class Metrics {
	private float ANA;
	private int NOM, NOA, ITC, ETC, CAC, DIT, CLD, NOC, NOD;
	
	public Metrics (Classe c, ArrayList<Classe> allClasses) {
		ANA = computeANA(c);
		NOM = computeNOM();
		NOA = computeNOA();
		ITC = computeITC();
		ETC = computeETC();
		CAC = computeCAC();
		DIT = computeDIT();
		CLD = computeCLD();
		NOC = computeNOC(c);
		NOD = computeNOD();
	}
	

	private float computeANA(Classe c) {
		return 0;
	}

	private int computeNOM() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeNOA() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeITC() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeETC() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeCAC() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeDIT() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeCLD() {
		// TODO Auto-generated method stub
		return 0;
	}

	private int computeNOC(Classe c) {
		DefaultListModel<StringDetail> subClasses = c.getSubClasses();
		System.out.println(subClasses);
		return subClasses.getSize();
	}

	private int computeNOD() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String toString() {
		return "\nANA = "+ ANA + 
				"\nNOM = " + NOM + 
				"\nNOA = " + NOA + 
				"\nITC = " + ITC + 
				"\nETC = " + ETC + 
				"\nCAC = " + CAC + 
				"\nDIT = " + DIT + 
				"\nCLD = " + CLD + 
				"\nNOC = " + NOC + 
				"\nNOD = " + NOD;
	}
	
}
