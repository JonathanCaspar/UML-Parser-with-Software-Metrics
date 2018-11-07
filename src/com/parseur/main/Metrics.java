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
	private Pattern parenthesis = Pattern.compile("[\\w\\s]+\\(([\\w\\,\\s]+)+\\)"); // detecte tout ce qui se trouve entre parenth�ses
	public static String[] metricsDefinition = {
			"Nombre moyen d�arguments des methodes locales pour la classe CLASSNAME.",
			"Nombre de methodes locales/heritees de la classe CLASSNAME. Dans le cas o� une methode est heritee et redefinie localement "
			+ "(m�me nom, m�me ordre et types des arguments et m�me type de retour), elle ne compte qu�une fois.",
			"Nombre d�attributs locaux/herites de la classe CLASSNAME.",
			"Nombre de fois o� d�autres classes du diagramme apparaissent comme types des arguments des methodes de CLASSNAME.",
			"Nombre de fois o� CLASSNAME appara�t comme type des arguments dans les methodes des autres classes du diagramme.",
			"Nombre d�associations (incluant les agregations) locales/heritees auxquelles participe une classe CLASSNAME.",
			"Taille du chemin le plus long reliant une classe CLASSNAME � une classe racine dans le graphe d�heritage.",
			"Taille du chemin le plus long reliant une classe CLASSNAME � une classe feuille dans le graphe d�heritage.",
			"Nombre de sous-classes directes de CLASSNAME.",
			"Nombre de sous-classes directes et indirectes de CLASSNAME." };

	public Metrics (ArrayList<Classe> classes) {
		this.classes = classes;
	}
	
	/**
	 * Nombre moyen d�arguments des methodes locales pour la classe c
	 * @param c	classe initiale
	 */
	private float computeANA(Classe c) {		
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(methodsCount == 0) return 0;
		
		// Pour chaque methode de la classe, on inspecte chaque argument
		for(int i = 0; i < methodsCount; i++) {
			 // Recup�re tout ce qui se trouve entre parenth�ses
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
	 * @param visitedClasses	liste des classes dej� visitees lors du parcours recursif
	 * @param visitedMethods	liste des methodes dej� visitees lors du parcours recursif
	 */
	private int computeNOM(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedMethods) {
		DefaultListModel<StringDetail> methods = classeInit.getMethods();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedMethods == null) visitedMethods = new ArrayList<String>();

		int methodsCount = getSizeOfModelList(methods);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // evite qu'on visite � nouveau la classe courante
		
		//On stocke la liste des methodes de la classe courante (dej� rencontres)
		for (int i = 0; i < methodsCount; i++) {
			String methodName = methods.getElementAt(i).getValue();
			if(!visitedMethods.contains(methodName)) {	// la methode n'a pas dej� ete rencontree 
				visitedMethods.add(methodName); // on stocke chaque methode
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dej� visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recup�re la reference vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la sous classe est la classe initiale, alors on est s�r que la super-classe en est une
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
	 * Nombre d�attributs locaux/herites de la classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes dej� visitees lors du parcours recursif
	 * @param visitedAttributes	liste des attributs dej� visites lors du parcours recursif
	 */
	private int computeNOA(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedAttributes) {
		DefaultListModel<StringDetail> attributes = classeInit.getAttributes();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedAttributes == null) visitedAttributes = new ArrayList<String>();

		int attributesCount = getSizeOfModelList(attributes);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // evite qu'on visite � nouveau la classe courante
		
		//On stocke la liste des methodes dej� rencontrees
		for (int i = 0; i < attributesCount; i++) {
			String attributeName = attributes.getElementAt(i).getValue();
			if(!visitedAttributes.contains(attributeName)) {	// la methode n'a pas dej� ete visitee 
				visitedAttributes.add(attributeName); // on stocke chaque methode
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dej� visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recup�re la reference vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la sous classe est la classe initiale, alors on est s�r que la super-classe en est une
					if(subClass.equals(classeInit)) {
						argCount += computeNOA(potentialSuperClass, visitedClasses, visitedAttributes);
					}
				}
			}
			
		}
		return argCount;
	}

	/**
	 * Nombre de fois o� d�autres classes du diagramme apparaissent comme types des arguments des methodes de c
	 * @param c		classe initiale
	 */
	private int computeITC(Classe c) {
		DefaultListModel<StringDetail> methods = c.getMethods();
		int methodsCount = getSizeOfModelList(methods);
		int count = 0;
		
		for(int i = 0; i < methodsCount; i++) {
			Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
			if(parenMatch.find()) {
				// on recup�re les types d'arguments de la methode
				String methodParams = parenMatch.group(1);
				
				if(! methodParams.isEmpty()) {
					String[] params = methodParams.split("\\,");
					
					// on verifie pour chaque param�tre si une Classe du m�me nom existe
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
	 *  Nombre de fois o� classeInit appara�t comme type des arguments dans les methodes des autres classes du diagramme
	 * @param classeInit	classe initiale
	 */
	private int computeETC(Classe classeInit) {
		int count = 0;
		
		// on parcours chaque classe (� part "c" elle m�me)
		for(Classe otherClass : classes) {
			if (otherClass.equals(classeInit)) continue; // ignorer le traitement de la classe initiale
			
			DefaultListModel<StringDetail> methods = otherClass.getMethods();
			int methodsCount = getSizeOfModelList(methods);
			
			for(int i = 0; i < methodsCount; i++) {
				Matcher parenMatch = parenthesis.matcher(methods.getElementAt(i).getValue());
				if(parenMatch.find()) {
					// on recup�re les types d'arguments de la methode
					String methodParams = parenMatch.group(1);
					
					if(! methodParams.isEmpty()) {
						String[] params = methodParams.split("\\,");
						
						// on verifie si une Classe "classeInit" se trouve dans les param�tres de la methode 
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
	 * Nombre d�associations (incluant les agregations) locales/heritees auxquelles participe une classe c
	 * @param classeInit		classe initiale
	 * @param visitedClasses	liste des classes dej� visitees lors du parcours recursif
	 * @param visitedRelations	liste des relations dej� visites lors du parcours recursif
	 */
	private int computeCAC(Classe classeInit, ArrayList<Classe> visitedClasses, ArrayList<String> visitedRelations) {
		DefaultListModel<StringDetail> relations = classeInit.getRelations();
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();
		if(visitedRelations == null) visitedRelations = new ArrayList<String>();

		int relationsCount = getSizeOfModelList(relations);
		int argCount = 0;
		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // on omet l'analyse de la classe elle-m�me
		
		//On stocke la liste des relations dej� rencontrees
		for (int i = 0; i < relationsCount; i++) {
			String relationName = relations.getElementAt(i).getValue();
			if(!visitedRelations.contains(relationName)) {	// la relation n'a pas dej� ete visitee 
				visitedRelations.add(relationName); // on stocke chaque relation
				argCount++;
			}
		}
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dej� visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recup�re la reference vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la potentielle super-classe poss�de la sous-classe initiale "c"
					if(subClass.equals(classeInit)) {
						argCount += computeCAC(potentialSuperClass, visitedClasses, visitedRelations);
					}
				}
			}
			
		}
		return argCount;
	}

	/**
	 * Taille du chemin le plus long reliant une classe ci � une classe racine dans le graphe d�heritage
	 * @param classeInit		classe initiale � partir de laquelle on fait une remontee dans l'arbre d'heritage
	 * @param depth				profondeur de recursion lors du parcours des super-classes
	 * @param visitedClasses	liste des classes dej� visitees lors du parcours recursif
	 */
	private int computeDIT(Classe classeInit, int depth, ArrayList<Classe> visitedClasses) {
		if(visitedClasses == null) visitedClasses = new ArrayList<Classe>();

		if(!visitedClasses.contains(classeInit)) visitedClasses.add(classeInit); // on omet l'analyse de la classe elle-m�me
		
		// On analyse toutes les autres classes qui sont potentiellement des super-classes de la classe initiale
		for (Classe potentialSuperClass : classes) {
			if(!visitedClasses.contains(potentialSuperClass)) { // on omet l'analyse des classes dej� visitees
				DefaultListModel<StringDetail> subClassesOfSuperClass = potentialSuperClass.getSubClasses(); 
				
				// On verifie si la classe initiale se trouve dans les sous-classes de nos potentielles super-classes
				int subClassesOfSuperClassCount = getSizeOfModelList(subClassesOfSuperClass);
				
				for(int i = 0; i <  subClassesOfSuperClassCount; i++) {
					// On recup�re la reference vers la sous-classe de la potentielle super classe
					Classe subClass = Database.classExists(classes, subClassesOfSuperClass.getElementAt(i).getValue());
					
					// Si la sous classe est la classe initiale, alors on est s�r que la super-classe en est une
					if(subClass.equals(classeInit)) {
						return computeDIT(potentialSuperClass, depth+1, visitedClasses);
					}
				}
			}
			
		}
		return depth;
	}
	
	/**
	 * Taille du chemin le plus long reliant une classe ci � une classe feuille dans le graphe d�heritage
	 * @param c					classe initiale � partir de laquelle on fait une remontee dans l'arbre d'heritage
	 * @param depth				profondeur de recursion lors du parcours des super-classes
	 * @param visitedClasses	liste des classes dej� visitees lors du parcours recursif
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
			if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas dej� ete visitee, on poss�de la reference � cette derniere
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
	 *  @param visitedClasse	liste des classes dej� visitees lors du parcours recursif
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
				if (subClass != null && !visitedClasses.contains(subClass)) { // la classe existe et n'a pas dej� ete visitee, on poss�de la reference � cette derniere
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
