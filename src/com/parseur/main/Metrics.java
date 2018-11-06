package com.parseur.main;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.xml.crypto.Data;

public class Metrics {	
	private ArrayList<Classe> classes;

	private Pattern parenthesis = Pattern.compile("[\\w\\s]+\\(([\\w\\,\\s]+)+\\)");

	public Metrics (ArrayList<Classe> classes) {
		this.classes = classes;
	}
	
	/**
	 * Nombre moyen d’arguments des méthodes locales pour la classe c
	 * @param c	classe initiale
	 */
	private float computeANA(Classe c) {		
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(methodsCount == 0) return 0;
		
		// Pour chaque méthode de la classe, on inspecte chaque argument
		for(int i = 0; i < methodsCount; i++) {
			 // Récupère tout ce qui se trouve entre parenthèses
			Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
			if(parenMatch.find()) {
				String methodParams = parenMatch.group(1);
				if(! methodParams.isEmpty()) {
					argCount += (methodParams.split("\\,").length);
				}
			}
		}
		
		return (float) argCount/methodsCount;
	}
	
	/**
	 * Nombre de méthodes locales/héritées de la classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes déjà visitées lors du parcours récursif
	 * @param visitedMethods	liste des méthodes déjà visitées lors du parcours récursif
	 */
	private int computeNOM(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedMethods) {
		DefaultListModel<StringDetail> methods = classeInit.getMethods();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedMethods == null) visitedMethods = new ArrayList<String>();

		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // évite qu'on visite à nouveau la classe courante
		
		//On stocke la liste des méthodes de la classe courante (déjà rencontrés)
		for (int i = 0; i < methodsCount; i++) {
			String methodName = methods.getElementAt(i).getValue();
			if(!visitedMethods.contains(methodName)) {	// la méthode n'a pas déjà été rencontrée 
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
					
					// Si la sous classe est la classe initiale, alors on est sûr que la super-classe en est une
					if(subClass.equals(classeInit)) {
						// On ajoute récursivement le nombre d'arguments de la super classe en question
						argCount += computeNOM(potentialSuperClass, visitedClasses, visitedMethods);
					}
				}
			}
			
		}
		return argCount;
	}

	/**
	 * Nombre d’attributs locaux/hérités de la classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes déjà visitées lors du parcours récursif
	 * @param visitedAttributes	liste des attributs déjà visités lors du parcours récursif
	 */
	private int computeNOA(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedAttributes) {
		DefaultListModel<StringDetail> attributes = classeInit.getAttributes();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedAttributes == null) visitedAttributes = new ArrayList<String>();

		int attributesCount = getSizeOfModelList(attributes);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // évite qu'on visite à nouveau la classe courante
		
		//On stocke la liste des méthodes déjà rencontrées
		for (int i = 0; i < attributesCount; i++) {
			String attributeName = attributes.getElementAt(i).getValue();
			if(!visitedAttributes.contains(attributeName)) {	// la méthode n'a pas déjà été visitée 
				visitedAttributes.add(attributeName); // on stocke chaque méthode
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
					
					// Si la sous classe est la classe initiale, alors on est sûr que la super-classe en est une
					if(subClass.equals(classeInit)) {
						argCount += computeNOA(potentialSuperClass, visitedClasses, visitedAttributes);
					}
				}
			}
			
		}
		return argCount;
	}

	/**
	 * Nombre de fois où d’autres classes du diagramme apparaissent comme types des arguments des méthodes de c
	 * @param c		classe initiale
	 */
	private int computeITC(Classe c) {
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int count = 0;
		
		for(int i = 0; i < methodsCount; i++) {
			Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
			if(parenMatch.find()) {
				// on récupère les types d'arguments de la méthode
				String methodParams = parenMatch.group(1);
				
				if(! methodParams.isEmpty()) {
					String[] params = methodParams.split("\\,");
					
					// on vérifie pour chaque paramètre si une Classe du même nom existe
					for (String param : params) {
						Classe otherClass = Database.classExists(classes, param);
						if(otherClass != null) { // l'argument est bien une classe
							count++;
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 *  Nombre de fois où classeInit apparaît comme type des arguments dans les méthodes des autres classes du diagramme
	 * @param classeInit	classe initiale
	 */
	private int computeETC(Classe classeInit) {
		int count = 0;
		
		// on parcours chaque classe (à part "c" elle même)
		for(Classe otherClass : classes) {
			if (otherClass.equals(classeInit)) continue; // ignorer le traitement de la classe initiale
			
			DefaultListModel<StringDetail> methods = otherClass.getMethods();
			int methodsCount = getSizeOfModelList(methods);
			
			for(int i = 0; i < methodsCount; i++) {
				Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
				if(parenMatch.find()) {
					// on récupère les types d'arguments de la méthode
					String methodParams = parenMatch.group(1);
					
					if(! methodParams.isEmpty()) {
						String[] params = methodParams.split("\\,");
						
						// on vérifie si une Classe "classeInit" se trouve dans les paramètres de la méthode 
						for (String param : params) {
							if(param.equals(classeInit.getName().getValue())) { // l'argument est bien une classe
								count++;
							}
						}
					}
				}
			}
		}
		return count;
	}

	/**
	 * Nombre d’associations (incluant les agrégations) locales/héritées auxquelles participe une classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes déjà visitées lors du parcours récursif
	 * @param visitedRelations	liste des relations déjà visités lors du parcours récursif
	 */
	private int computeCAC(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedRelations) {
		DefaultListModel<StringDetail> relations = classeInit.getRelations();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedRelations == null) visitedRelations = new ArrayList<String>();

		int relationsCount = getSizeOfModelList(relations);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // on omet l'analyse de la classe elle-même
		
		//On stocke la liste des relations déjà rencontrées
		for (int i = 0; i < relationsCount; i++) {
			String relationName = relations.getElementAt(i).getValue();
			if(!visitedRelations.contains(relationName)) {	// la relation n'a pas déjà été visitée 
				visitedRelations.add(relationName); // on stocke chaque relation
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
						argCount += computeCAC(potentialSuperClass, visitedClasses, visitedRelations);
					}
				}
			}
			
		}
		return argCount;
	}

	/**
	 * Taille du chemin le plus long reliant une classe ci à une classe racine dans le graphe d’héritage
	 * @param classeInit		classe initiale à partir de laquelle on fait une remontée dans l'arbre d'héritage
	 * @param depth				profondeur de récursion lors du parcours des super-classes
	 * @param visitedClasses	liste des classes déjà visitées lors du parcours récursif
	 */
	private int computeDIT(Classe classeInit, int depth, ArrayList<Classe> visitedClasses) {
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();

		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // on omet l'analyse de la classe elle-même
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes déjà visitées
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On vérifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On récupère la référence vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la sous classe est la classe initiale, alors on est sûr que la super-classe en est une
					if(subClass.equals(classeInit)) {
						return computeDIT(potentialSuperClass, depth+1, visitedClasses);
					}
				}
			}
			
		}
		return depth;
	}
	
	/**
	 * Taille du chemin le plus long reliant une classe ci à une classe feuille dans le graphe d’héritage
	 * @param c					classe initiale à partir de laquelle on fait une remontée dans l'arbre d'héritage
	 * @param depth				profondeur de récursion lors du parcours des super-classes
	 * @param visitedClasses	liste des classes déjà visitées lors du parcours récursif
	 */
	private int computeCLD(Classe c, int depth, ArrayList<Classe> visitedClasses) {
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		DefaultListModel<StringDetail> subClassesString = c.getSubClasses();
		int count = computeNOC(c); // nombre de sous-classes directes

		visitedClasses.add(c);
		
		// Pour toutes les sous-classes de la classe courante : on cherche un chemin permettant de descendre dans l'arbre d'héritage
		for (int i = 0; i < count; i++) {
			String className = subClassesString.getElementAt(i).getValue();
			Classe subClass = Database.classExists(classes, className);
			if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas déjà été visitée, on possède la référence à cette derniere
				return computeCLD(subClass, depth+1, visitedClasses); // on cherche si on peut descendre d'une profondeur de plus
			}
		}	
		
		return depth;
	}


	/**
	 *  Nombre de sous-classes directes de c
	 *  @param c	classe initiale
	 */
	private int computeNOC(Classe c) {	// nombre de sous-classes directs
		DefaultListModel<StringDetail> subClasses = c.getSubClasses();
		return getSizeOfModelList(subClasses);
	}

	/**
	 *  Nombre de sous-classes directes et indirectes de c
	 *  @param c				classe initiale
	 *  @param visitedClasse	liste des classes déjà visitées lors du parcours récursif
	 */
	private int computeNOD(Classe c, ArrayList<Classe> visitedClasses) {  // nombre de sous-classes directs et indirects
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		DefaultListModel<StringDetail> subClassesString = c.getSubClasses();
		int count = computeNOC(c); // nombre de sous-classes directes
		int limit = count;

		visitedClasses.add(c);
		if(count > 0){
			for (int i = 0; i < limit; i++) {
				Classe subClass = Database.classExists(classes, subClassesString.getElementAt(i).getValue());
				if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas déjà été visitée, on possède la référence à cette derniere
					count += computeNOD(subClass, visitedClasses);
				}
			}	
		}
		return count;
	}
	
	/**
	 * Calcule la taille d'une liste de type DefaultListModel
	 * @param list
	 * @return le nombre d'éléments contenus dans une DefaultListModel
	 */
	public int getSizeOfModelList(DefaultListModel<StringDetail> list) {
		if( list.firstElement().getValue().length() != 0){  
			return list.getSize();
		}
		return 0;
	}

	/**
	 * Calcule les métriques du classe donnéee
	 * @param currentClass
	 * @return	un tableau contenant chaque métrique
	 */
	public String[] computeMetricsOf(Classe currentClass) {
		Classe c = currentClass; // on stocke la classe actuelle dans Metrics pour qu'elle soit accessible par toutes les méthodes "ComputeXXX"
		DecimalFormat df = new DecimalFormat("#.##");
		
		String[] result = {df.format(computeANA(c)),
						   String.valueOf(computeNOM(c, null, null)),
						   String.valueOf(computeNOA(c, null, null)),
						   String.valueOf(computeITC(c)),
						   String.valueOf(computeETC(c)),
						   String.valueOf(computeCAC(c, null, null)),
						   String.valueOf(computeDIT(c, 0, null)),
						   String.valueOf(computeCLD(c, 0, null)),
						   String.valueOf(computeNOC(c)),
						   String.valueOf(computeNOD(c, null))};
		return result;
		
	}
}
