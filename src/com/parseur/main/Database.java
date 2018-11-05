package com.parseur.main;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;

public class Database {
	private ArrayList<Classe> classes;
	private Metrics metrics;

	public Database() {
		classes = new ArrayList<Classe>();
	}

	/** 
	 * Ajoute un objet Classe avec ses attributs et méthodes extraites d'une instruction, dans la structure de données interne (si celui-ci n'existe pas déjà) 
	 * @param	instruction  séquence de caractères suivant une grammaire BNF décrivant un modèle UML
	 * @return	l'erreur trouvée si elle existe
	 */
	public String addClass(String instruction) {
		Pattern attrPattern = Pattern.compile("(ATTRIBUTES)[\\w|\\s|\\:|\\,]+(?=OPERATIONS)");
		Pattern operationPattern = Pattern.compile("(?:OPERATIONS)[\\w|\\s|\\:|\\,|\\(|\\)]+");
		Pattern methodPattern = Pattern.compile("([\\w]+)(?:\\s*\\()([\\w\\:\\s\\,]*)(?:\\)\\s+\\:\\s+)([\\w]+)");
		Matcher attrMatcher = attrPattern.matcher(instruction);
		Matcher operationMatcher = operationPattern.matcher(instruction);

		Classe newClass = new Classe();

		// Parsing du nom de la classe
		String className = instruction.split("\\s+")[1];
		if (classExists(classes, className) == null) {
			newClass.setName(className, instruction); // Nom de classe + description BNF

			// Parsing des attributs
			if (attrMatcher.find()) {
				// On récupére les attributs dans un tableau
				String rawAttributes = instruction.substring(attrMatcher.start(), attrMatcher.end()).trim();
				String attributes = rawAttributes.substring(11, rawAttributes.length()).trim(); // on retire le mot "OPERATIONS"

				String[] attrList = attributes.split("\\,");
				String formattedAttributes = "";

				for (int i = 0; i < attrList.length; i++) {
					// On affecte chaque attribut étant sous la forme "<nom> : <type>" dans la partie
					// attributs de "newClass" sous une nouvelle forme : "<type> <nom>,"
					String[] attribut = attrList[i].trim().split("\\:");
					formattedAttributes += (attribut[1].trim() + " " + attribut[0].trim()
							+ (i == attrList.length - 1 ? "" : ";"));
				}
				newClass.setAttributes(formattedAttributes, rawAttributes);
			}

			// Parsing de l'ensemble des méthodes
			if (operationMatcher.find()) {
				// On récupére les méthodes dans un tableau
				String rawMethods = instruction.substring(operationMatcher.start(), operationMatcher.end()).trim();
				
				if (rawMethods.length() > 11) {
					String methods = rawMethods.substring(11, rawMethods.length()).trim();
				
					Matcher methodMatcher = methodPattern.matcher(methods);
					String formattedMethods = "";
	 
					// On parse chaque méthode <nom>(<parametres>): <type>
					while (methodMatcher.find()) {
						String[] parameters = methodMatcher.group(2).split("\\,");
						String type = methodMatcher.group(3); // type de retour de la méthode
						formattedMethods += ((type.equals("void") ? "" : type + " ") + methodMatcher.group(1) + "("); // <type> <nom de methode>
	
						// On traite les parametres multiples de la méthode
						int count = parameters.length;
						if (count != 0) {
							for (int i = 0; i < count; i++) {
								String[] splitParams = parameters[i].split("\\:");
								if (splitParams.length == 2) {
									formattedMethods += splitParams[1].trim(); // on prend uniquement le type du parametre
								}
								formattedMethods += (i != count - 1) ? ", " : ");";
							}
	
						}
	
					}
					if (!formattedMethods.isEmpty()) {
						formattedMethods = formattedMethods.substring(0, formattedMethods.length() - 1); // retrait du ";"
					}
	
					newClass.setMethods(formattedMethods, rawMethods);
				}
			}

			// Une fois le nom, les attributs et les méthodes ajoutées, on ajoute la Classe
			// dans notre ArrayList classes
			classes.add(newClass);
			return "";
		} else
			return "Erreur : La classe " + className + " a été trouvé plusieurs fois.\n"
					+ "Seules les informations de la première occurence de " + className + " seront enregistrées.";
	}

	/** 
	 * Ajoute à la super classe concernée (Classe) des sous-classes étant donné une instruction, dans la structure de données interne 
	 * @param	instruction  séquence de caractères suivant une grammaire BNF décrivant un modèle UML
	 */
	public void addGeneralization(String instruction) {
		Pattern generaPattern = Pattern.compile("(?:GENERALIZATION\\s+)([\\w]+)(?:\\s+SUBCLASSES\\s+)([\\w|\\,|\\s]+)");
		Matcher generaMatcher = generaPattern.matcher(instruction);

		if (generaMatcher.find() && (generaMatcher.group(2) != null)) { // si au moins 1 sous-classe est spécifiée
			// on vérifie si la super classe indiquée existe
			Classe superClasse = classExists(classes, generaMatcher.group(1));
			if (superClasse != null) {
				// on vérifie que chaque sous-classe existe sinon on ne la considère pas
				String[] subClasses = generaMatcher.group(2).split("\\,");
				String formattedSubClasses = "";

				for (int i = 0; i < subClasses.length; i++) {
					Classe subClass = classExists(classes, subClasses[i].trim());
					if (subClass != null) {
						formattedSubClasses += (subClasses[i].trim() + ";");
					}
				}
				formattedSubClasses = formattedSubClasses.substring(0, formattedSubClasses.length() - 1); // on retire le dernier ";"

				superClasse.setSubClasses(formattedSubClasses, instruction);
			}
		}
	}

	/** 
	 * Modifie deux objets Classe pour respectivement mettre à jour leurs associations étant donné une instruction, dans la structure de données interne 
	 * @param	instruction  séquence de caractères suivant une grammaire BNF décrivant un modèle UML
	 */
	public void addAssociation(String instruction) {
		Pattern relationPattern = Pattern.compile(
				"RELATION\\s+(\\w+)\\s+ROLES\\s+CLASS[\\s]+([\\w]+)[\\s]+[\\w]+\\,[\\s]*CLASS[\\s]+([\\w]+)[\\s]+[\\w]+");
		Matcher relationMatcher = relationPattern.matcher(instruction);

		if (relationMatcher.find()) {
			String relationName = relationMatcher.group(1);
			String className1 = relationMatcher.group(2);
			String className2 = relationMatcher.group(3);

			Classe class1 = classExists(classes, className1);
			Classe class2 = classExists(classes, className2);

			// on vérifie que les deux classes existes sinon la relation est impossible
			if ((class1 != null) && (class2 != null)) {
				class1.addRelation("(R) " + relationName, instruction);
				class2.addRelation("(R) " + relationName, instruction);
			}
		}
	}

	/** 
	 * Modifie deux objets Classe pour respectivement mettre à jour leurs relations d'aggregation étant donné une instruction, dans la structure de données interne 
	 * @param	instruction  séquence de caractères suivant une grammaire BNF décrivant un modèle UML
	 */
	public void addAggregation(String instruction) {
		Pattern aggregationPattern = Pattern
				.compile("AGGREGATION\\s+CONTAINER\\s+CLASS\\s+(\\w+)\\s+\\w+\\s+PARTS([\\s|\\w|\\,]+)");
		Pattern partExtractPattern = Pattern.compile("CLASS\\s+(\\w+)\\s+\\w+");
		Matcher aggregationMatcher = aggregationPattern.matcher(instruction);

		if (aggregationMatcher.find()) {
			String containerClassName = aggregationMatcher.group(1).trim();
			Classe containerClass = classExists(classes, containerClassName);

			if (containerClass != null) {
				// on récupere les "parts"
				String[] splitParts = aggregationMatcher.group(2).trim().split("\\,");

				for (int i = 0; i < splitParts.length; i++) {
					Matcher partExtractMatcher = partExtractPattern.matcher(splitParts[i].trim());

					if (partExtractMatcher.find()) {
						// on vérifie si la classe trouvée existe
						String partClassName = partExtractMatcher.group(1).trim();
						Classe partClass = classExists(classes, partClassName);

						if (partClass != null) {
							containerClass.addRelation("(A) P_" + partClassName, instruction);
							partClass.addRelation("(A) C_" + containerClassName, instruction);
						}
					}
				}
			}
		}
	}

	/**
	 * Vérifie l'existence d'un objet de type Classe (identifié par son nom) dans la structure de données interne
	 * @param	searchedClass  nom de la classe à rechercher
	 * @return	la référence de la Classe cherchée si elle existe
	 */
	public static Classe classExists(ArrayList<Classe> classes, String searchedClass) {
		for (int i = 0; i < classes.size(); i++) {
			if (classes.get(i).getName().getValue().equals(searchedClass)) {
				return classes.get(i);
			}
		}
		return null;
	}

	/**
	 * Forme une liste de toutes les classes enregistrées étant graphiquement modélisable dans un JList
	 * @return	la liste des classes enregistrées
	 */
	// Retourne une ArrayList d'objet StringDetail détaillant le nom des classes
	public DefaultListModel<Classe> getClasses() {
		DefaultListModel<Classe> listModel = new DefaultListModel<Classe>();
		for (int i = 0; i < classes.size(); i++) {
			listModel.addElement(classes.get(i));
		}
		return listModel;
	}
	
	/**
	 * Calcule et associe à chaque classe les métriques encapsulées dans un objet MetricsData
	 */
	public void computeAllMetrics() {
		if (this.metrics == null) {
			metrics = new Metrics(classes);
		}
		
		for (int i = 0; i < classes.size(); i++) {				
			Classe currentClass = classes.get(i);
			System.out.println(currentClass + " : ");
			
			// Calcule les métriques de currentClass
			String[] data = metrics.computeMetricsOf(currentClass);
			
			if (data != null) {
				// Actualise les métriques de currentClass
				currentClass.setMetrics(data);
				System.out.println(currentClass.getMetrics());
			}
		}
	}

	/**
	 * Réinitialise la base de données et supprime les classes enregistrées
	 */
	public void resetDB() {
		classes.clear();
	}
}
