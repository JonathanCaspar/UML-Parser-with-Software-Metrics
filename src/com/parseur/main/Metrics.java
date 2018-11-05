package com.parseur.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;

public class Metrics {	
	private ArrayList<Classe> classes;
	private Classe c;
	private Pattern parenthesis = 
			Pattern.compile("[\\w\\s]+\\(([\\w\\,\\s]+)+\\)");// Récupère tout ce qui se trouve entre parenthèses

	
	
	
	public Metrics (ArrayList<Classe> classes) {
		this.classes = classes;
	}

	private float computeANA() {		
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(methodsCount == 0) return 0;
		
		for(int i = 0; i < methodsCount; i++) {
			Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
			if(parenMatch.find()) {
				String methodNames = parenMatch.group(1);
				if(! methodNames.isEmpty()) {
					argCount += (methodNames.split("\\,").length);
				}
			}
		}
		
		return (float) argCount/methodsCount;
	}

	private int computeNOM(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedMethods) {
		DefaultListModel<StringDetail> methods = classeInit.getMethods();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedMethods == null) visitedMethods = new ArrayList<String>();

		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(c); // on omet l'analyse de la classe elle-même
		
		//On stocke la liste des méthodes déjà rencontrées
		for (int i = 0; i < methodsCount; i++) {
			String methodName = methods.getElementAt(i).getValue();
			if(!visitedMethods.contains(methodName)) {	// la méthode n'a pas déjà été visitée 
				visitedMethods.add(methodName); // on stocke chaque méthode
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes déjà visitées
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On vérifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On récupère la référence vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la potentielle super-classe possède la sous-classe initiale "c"
					if(subClass.equals(classeInit)) {
						argCount += computeNOM(potentialSuperClass, visitedClasses, visitedMethods);
					}
				}
			}
			
		}
		return argCount;
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

	private int computeNOC(Classe c) {	// nombre de sous-classes directs
		DefaultListModel<StringDetail> subClasses = c.getSubClasses();
		return getSizeOfModelList(subClasses);
	}


	private int computeNOD() {  // nombre de sous-classes directs et indirects
		return countAllSubClasses(c, new ArrayList<Classe>());
	}
	
	private int countAllSubClasses(Classe classe, ArrayList<Classe> visitedClasses) {
		DefaultListModel<StringDetail> subClassesString = classe.getSubClasses();
		int count = computeNOC(classe); // nombre de sous-classes directes
		int limit = count;

		visitedClasses.add(classe);
		if(count > 0){
			for (int i = 0; i < limit; i++) {
				Classe subClass = Database.classExists(classes, subClassesString.getElementAt(i).getValue());
				if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas déjà été visitée, on possède la référence à cette derniere
					count += countAllSubClasses(subClass, visitedClasses);
				}
			}	
		}
		return count;
	}
	
	public int getSizeOfModelList(DefaultListModel<StringDetail> list) {
		if( list.firstElement().getValue().length() != 0){  
			return list.getSize();
		}
		return 0;
	}

	public String[] computeMetricsOf(Classe currentClass) {
		this.c = currentClass; // on stocke la classe actuelle dans Metrics pour qu'elle soit accessible par toutes les méthodes "ComputeXXX"
		DecimalFormat df = new DecimalFormat("#.##");
		
		String[] result = {df.format(computeANA()),
						   String.valueOf(computeNOM(c, null,null)),
						   computeNOA(),
						   computeITC(),
						   computeETC(),
						   computeCAC(),
						   computeDIT(),
						   computeCLD(),
						   String.valueOf(computeNOC(c)),
						   String.valueOf(computeNOD())};
		return result;
		
	}
}
