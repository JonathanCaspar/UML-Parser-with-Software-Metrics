package com.parseur.main;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;

public class Metrics {	
	private ArrayList<Classe> classes;
	private Pattern parenthesis = Pattern.compile("[\\w\\s]+\\(([\\w\\,\\s]+)+\\)"); // detecte tout ce qui se trouve entre parenthèses
	public static String[] metricsDefinition = {
			"Nombre moyen d’arguments des methodes locales pour la classe CLASSNAME.",
			"Nombre de methodes locales/heritees de la classe CLASSNAME. Dans le cas où une methode est heritee et redefinie localement "
			+ "(même nom, même ordre et types des arguments et même type de retour), elle ne compte qu’une fois.",
			"Nombre d’attributs locaux/herites de la classe CLASSNAME.",
			"Nombre de fois où d’autres classes du diagramme apparaissent comme types des arguments des methodes de CLASSNAME.",
			"Nombre de fois où CLASSNAME apparaît comme type des arguments dans les methodes des autres classes du diagramme.",
			"Nombre d’associations (incluant les agregations) locales/heritees auxquelles participe une classe CLASSNAME.",
			"Taille du chemin le plus long reliant une classe CLASSNAME à une classe racine dans le graphe d’heritage.",
			"Taille du chemin le plus long reliant une classe CLASSNAME à une classe feuille dans le graphe d’heritage.",
			"Nombre de sous-classes directes de CLASSNAME.",
			"Nombre de sous-classes directes et indirectes de CLASSNAME." };

	public Metrics (ArrayList<Classe> classes) {
		this.classes = classes;
	}
	
	/**
	 * Nombre moyen d’arguments des methodes locales pour la classe c
	 * @param c	classe initiale
	 */
	private float computeANA(Classe c) {		
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(methodsCount == 0) return 0;
		
		// Pour chaque methode de la classe, on inspecte chaque argument
		for(int i = 0; i < methodsCount; i++) {
			 // Recupère tout ce qui se trouve entre parenthèses
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
	 * Nombre de methodes locales/heritees de la classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes dejà visitees lors du parcours recursif
	 * @param visitedMethods	liste des methodes dejà visitees lors du parcours recursif
	 */
	private int computeNOM(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedMethods) {
		DefaultListModel<StringDetail> methods = classeInit.getMethods();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedMethods == null) visitedMethods = new ArrayList<String>();

		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // evite qu'on visite à nouveau la classe courante
		
		//On stocke la liste des methodes de la classe courante (dejà rencontres)
		for (int i = 0; i < methodsCount; i++) {
			String methodName = methods.getElementAt(i).getValue();
			if(!visitedMethods.contains(methodName)) {	// la methode n'a pas dejà ete rencontree 
				visitedMethods.add(methodName); // on stocke chaque methode
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dejà visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recupère la reference vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la sous classe est la classe initiale, alors on est sûr que la super-classe en est une
					if(subClass.equals(classeInit)) {
						// On ajoute recursivement le nombre d'arguments de la super classe en question
						argCount += computeNOM(potentialSuperClass, visitedClasses, visitedMethods);
					}
				}
			}
			
		}
		return argCount;
	}

	/**
	 * Nombre d’attributs locaux/herites de la classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes dejà visitees lors du parcours recursif
	 * @param visitedAttributes	liste des attributs dejà visites lors du parcours recursif
	 */
	private int computeNOA(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedAttributes) {
		DefaultListModel<StringDetail> attributes = classeInit.getAttributes();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedAttributes == null) visitedAttributes = new ArrayList<String>();

		int attributesCount = getSizeOfModelList(attributes);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // evite qu'on visite à nouveau la classe courante
		
		//On stocke la liste des methodes dejà rencontrees
		for (int i = 0; i < attributesCount; i++) {
			String attributeName = attributes.getElementAt(i).getValue();
			if(!visitedAttributes.contains(attributeName)) {	// la methode n'a pas dejà ete visitee 
				visitedAttributes.add(attributeName); // on stocke chaque methode
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dejà visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recupère la reference vers la sous-classe de la potentielle super classe
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
	 * Nombre de fois où d’autres classes du diagramme apparaissent comme types des arguments des methodes de c
	 * @param c		classe initiale
	 */
	private int computeITC(Classe c) {
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int count = 0;
		
		for(int i = 0; i < methodsCount; i++) {
			Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
			if(parenMatch.find()) {
				// on recupère les types d'arguments de la methode
				String methodParams = parenMatch.group(1);
				
				if(! methodParams.isEmpty()) {
					String[] params = methodParams.split("\\,");
					
					// on verifie pour chaque paramètre si une Classe du même nom existe
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
	 *  Nombre de fois où classeInit apparaît comme type des arguments dans les methodes des autres classes du diagramme
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
					// on recupère les types d'arguments de la methode
					String methodParams = parenMatch.group(1);
					
					if(! methodParams.isEmpty()) {
						String[] params = methodParams.split("\\,");
						
						// on verifie si une Classe "classeInit" se trouve dans les paramètres de la methode 
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
	 * Nombre d’associations (incluant les agregations) locales/heritees auxquelles participe une classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes dejà visitees lors du parcours recursif
	 * @param visitedRelations	liste des relations dejà visites lors du parcours recursif
	 */
	private int computeCAC(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedRelations) {
		DefaultListModel<StringDetail> relations = classeInit.getRelations();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedRelations == null) visitedRelations = new ArrayList<String>();

		int relationsCount = getSizeOfModelList(relations);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // on omet l'analyse de la classe elle-même
		
		//On stocke la liste des relations dejà rencontrees
		for (int i = 0; i < relationsCount; i++) {
			String relationName = relations.getElementAt(i).getValue();
			if(!visitedRelations.contains(relationName)) {	// la relation n'a pas dejà ete visitee 
				visitedRelations.add(relationName); // on stocke chaque relation
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dejà visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recupère la reference vers la sous-classe de la potentielle super classe
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
	 * Taille du chemin le plus long reliant une classe ci à une classe racine dans le graphe d’heritage
	 * @param classeInit		classe initiale à partir de laquelle on fait une remontee dans l'arbre d'heritage
	 * @param depth				profondeur de recursion lors du parcours des super-classes
	 * @param visitedClasses	liste des classes dejà visitees lors du parcours recursif
	 */
	private int computeDIT(Classe classeInit, int depth, ArrayList<Classe> visitedClasses) {
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();

		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // on omet l'analyse de la classe elle-même
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dejà visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recupère la reference vers la sous-classe de la potentielle super classe
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
	 * Taille du chemin le plus long reliant une classe ci à une classe feuille dans le graphe d’heritage
	 * @param c					classe initiale à partir de laquelle on fait une remontee dans l'arbre d'heritage
	 * @param depth				profondeur de recursion lors du parcours des super-classes
	 * @param visitedClasses	liste des classes dejà visitees lors du parcours recursif
	 */
	private int computeCLD(Classe c, int depth, ArrayList<Classe> visitedClasses) {
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		DefaultListModel<StringDetail> subClassesString = c.getSubClasses();
		int count = computeNOC(c); // nombre de sous-classes directes

		visitedClasses.add(c);
		
		// Pour toutes les sous-classes de la classe courante : on cherche un chemin permettant de descendre dans l'arbre d'heritage
		for (int i = 0; i < count; i++) {
			String className = subClassesString.getElementAt(i).getValue();
			Classe subClass = Database.classExists(classes, className);
			if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas dejà ete visitee, on possède la reference à cette derniere
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
	 *  @param visitedClasse	liste des classes dejà visitees lors du parcours recursif
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
				if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas dejà ete visitee, on possède la reference à cette derniere
					count += computeNOD(subClass, visitedClasses);
				}
			}	
		}
		return count;
	}
	
	/**
	 * Calcule la taille d'une liste de type DefaultListModel
	 * @param list
	 * @return le nombre d'elements contenus dans une DefaultListModel
	 */
	public int getSizeOfModelList(DefaultListModel<StringDetail> list) {
		if( list.firstElement().getValue().length() != 0){  
			return list.getSize();
		}
		return 0;
	}

	/**
	 * Calcule les metriques du classe donnee
	 * @param currentClass
	 * @return	un tableau contenant chaque metrique
	 */
	public String[] computeMetricsOf(Classe currentClass) {
		Classe c = currentClass; // on stocke la classe actuelle dans Metrics pour qu'elle soit accessible par toutes les methodes "ComputeXXX"
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
		otherSymbols.setDecimalSeparator('.'); 
		DecimalFormat df = new DecimalFormat("#.##", otherSymbols);
		
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
