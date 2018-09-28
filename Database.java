package com.parseur.main;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Database {
	private ArrayList<String[]> classes; 

	public Database() {
		classes = new ArrayList<String[]>();
	}
	
	public void addClass(String instruction) {
		Pattern attrPattern   = Pattern.compile("(?<=ATTRIBUTES)[\\w|\\s|\\:|\\,]+(?=OPERATIONS)");
		Pattern operationPattern = Pattern.compile("(?<=OPERATIONS)[\\w|\\s|\\:|\\,|\\(|\\)]+");
		Pattern methodPattern = Pattern.compile("([\\w]+)(?:\\s*\\()([\\w\\:\\s\\,]*)(?:\\)\\s+\\:\\s+)([\\w]+)");
		Matcher attrMatcher   = attrPattern.matcher(instruction);
		Matcher operationMatcher   = operationPattern.matcher(instruction);

		String[] newClass = new String[6];
		
		//Parsing du nom de la classe
		String[] words = instruction.split("\\s+");
		newClass[0] = words[1]; //Nom de classe
		
		//Parsing des attributs
		if(attrMatcher.find()) {
			// On r�cup�re les attributs dans un tableau 
			String[] attrList = instruction.substring(attrMatcher.start(), attrMatcher.end()).split("\\,");
			String formattedAttributes = "";
			
			for(int i = 0; i < attrList.length; i++) {
				// On affecte chaque attribut �tant sous la forme "<nom> : <type>" dans la partie 
				// attributs de "newClass" sous une nouvelle forme : "<type> <nom>,"
				String[] attribut = attrList[i].trim().split("\\:");
				formattedAttributes += (attribut[1].trim()+ " " + attribut[0].trim() + (i==attrList.length-1 ? "":";") );
			}
			newClass[1] = formattedAttributes;
		}
		
		//Parsing de l'ensemble des m�thodes
		if(operationMatcher.find()) {
			// On r�cup�re les m�thodes dans un tableau 
			String rawMethods = instruction.substring(operationMatcher.start(), operationMatcher.end());
			Matcher methodMatcher   = methodPattern.matcher(rawMethods);
			String formattedMethods = "";
			
			// On parse chaque m�thode <nom>(<parametres>): <type>
			while(methodMatcher.find()) {
				String[] parameters = methodMatcher.group(2).split("\\,");
				String type = methodMatcher.group(3); // type de retour de la m�thode
				formattedMethods += ( (type.equals("void")? "": type + " ") + methodMatcher.group(1) + "("); // <type> <nom de methode>
				
				//On traite les parametres multiples de la m�thode
				int count = parameters.length;
				if (count != 0) {
					for(int i = 0; i < count; i++) {
						String[] splitParams = parameters[i].split("\\:");
						if (splitParams.length == 2) {
							formattedMethods += splitParams[1].trim(); // on prend uniquement le type du parametre
						}
						formattedMethods += (i != count-1)? ", ": ")";
					}
				}
			}
			newClass[2] = formattedMethods;
		}
		
		// Une fois le nom, les attributs et les m�thodes ajout�es, on ajoute la Classe dans notre ArrayList classes
		classes.add(newClass);
	}
	
	public void addGeneralization(String instruction) {
		// to do
	}
	
	public void addRelation(String instruction) {
		// to do
	}
	
	public void addAggregation(String instruction) {
		// to do
	}
	
	public String[] getClasses() {
		String[] classesList= new String[classes.size()];
		for(int i = 0; i < classes.size(); i++) {
			classesList[i] = classes.get(i)[0];
		}
		return classesList;
	}
	
	public void showDBcontent() {
		for(int i = 0; i < classes.size(); i++) {
			String[] selectedClass = classes.get(i);
			System.out.println("Classe: " + selectedClass[0]);
			System.out.println("Attributs: " + selectedClass[1]);
			System.out.println("Methodes: " + selectedClass[2]);
			System.out.println("--------------------------------------");
		}
	}
}
