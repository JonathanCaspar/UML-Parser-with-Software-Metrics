package com.parseur.main;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;

public class Database {
	private ArrayList<Classe> classes; 

	public Database() {
		classes = new ArrayList<Classe>();
	}
	
	public void addClass(String instruction) {
		Pattern attrPattern   = Pattern.compile("(ATTRIBUTES)[\\w|\\s|\\:|\\,]+(?=OPERATIONS)");
		Pattern operationPattern = Pattern.compile("(?<=OPERATIONS)[\\w|\\s|\\:|\\,|\\(|\\)]+");
		Pattern methodPattern = Pattern.compile("([\\w]+)(?:\\s*\\()([\\w\\:\\s\\,]*)(?:\\)\\s+\\:\\s+)([\\w]+)");
		Matcher attrMatcher   = attrPattern.matcher(instruction);
		Matcher operationMatcher   = operationPattern.matcher(instruction);

		Classe newClass = new Classe();
		
		//Parsing du nom de la classe
		String[] words = instruction.split("\\s+");
		newClass.setName(words[1], instruction); //Nom de classe + description BNF
		
		//Parsing des attributs
		if(attrMatcher.find()) {
			// On r�cup�re les attributs dans un tableau 
			String rawAttributes = instruction.substring(attrMatcher.start(), attrMatcher.end()).trim();
			String attributes = rawAttributes.substring(11, rawAttributes.length()).trim(); // on retire le mot "OPERATIONS"
			
			String[] attrList = attributes.split("\\,");
			String formattedAttributes = "";
			
			for(int i = 0; i < attrList.length; i++) {
				// On affecte chaque attribut �tant sous la forme "<nom> : <type>" dans la partie 
				// attributs de "newClass" sous une nouvelle forme : "<type> <nom>,"
				String[] attribut = attrList[i].trim().split("\\:");
				formattedAttributes += (attribut[1].trim()+ " " + attribut[0].trim() + (i==attrList.length-1 ? "":";") );
			}
			newClass.setAttributes(formattedAttributes, rawAttributes);
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
						formattedMethods += (i != count-1)? ", ": ");"; 
					}
					
				}
				
			}
			if (!formattedMethods.isEmpty()) {
				formattedMethods = formattedMethods.substring(0, formattedMethods.length()-1); // retrait du ";"
			}
			
			newClass.setMethods(formattedMethods, "test");
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
	
	// Retourne une ArrayList d'objet StringDetail d�taillant le nom des classes
    public DefaultListModel<Classe> getClasses() {
    	DefaultListModel<Classe> listModel = new DefaultListModel();
		for(int i = 0; i < classes.size(); i++) {
			listModel.addElement(classes.get(i));
		}
		return listModel;
	}
	
	public void showDBcontent() {
		for(int i = 0; i < classes.size(); i++) {
			Classe selectedClass = classes.get(i);
			System.out.println("Classe: " + selectedClass.getName());
			System.out.println("Attributs: " + selectedClass.getAttributes());
			System.out.println("Methodes: " + selectedClass.getMethods());
			System.out.println("--------------------------------------");
		}
	}
}
