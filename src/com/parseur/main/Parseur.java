package com.parseur.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

public class Parseur extends javax.swing.JFrame {

	public Database database;
	private File file;

	private JLabel closeButton;
	private JButton loadFileButton;
	private JButton cvsButton;
	private JButton metricsButton;

	private JLabel jLabel_Classe;
	private JPanel jPanelInfo;
	private JPanel jPanelClass;
	private JPanel jPanelClassName;
	private JPanel jPanelFile;
	
	private JScrollPane attributePane;
	private JScrollPane methodPane;
	private JScrollPane subClassPane;
	private JScrollPane relationPane;
	private JScrollPane detailPane;
	private JScrollPane metricsPane;
	private JTextArea textDetails;
	private JTextField filename;
	
	private boolean metricsComputed;
	private boolean fileLoaded;
	private String selectedClass;

	public Parseur() {

		initComponents();
		database = new Database();
		metricsComputed = false;
		fileLoaded = false;
		
		loadFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				file = searchFile();
				if (file != null) {
					filename.setText(file.getName());
					filename.setEditable(false);

					try {
						// reintialisation des donnees apres chaque chargement de fichier
						database.resetDB();
						jPanelClass.removeAll();
						selectedClass = "";
						metricsComputed = false;
						fileLoaded = false;

						String fileStrings = readFile(file.toPath().toString(), Charset.forName("UTF-8"));

						// decoupage des instructions separees par ";" (classe, generalisation, relations)
						String[] tab = fileStrings.split("\\;");

						for (int j = 0; j < tab.length; j++) {
							// analyse le type d'instruction puis la traite
							findAndTreatType(tab[j]);
						}
						
						// base de donnees construite : on l'affiche dans la JFrame Parseur
						afficherDansJPanel();


					} catch (IOException ex) {
						System.out.println("Could not read file");
					}
				} else {
					System.out.println("File access cancelled by user.");
				}
			}

		});
	}

	/** 
	 * Met le contenu d'un fichier dans un String
	 * @param path	adresse du fichier
	 * @param encoding type d'encodage
	 * @return le contenu d'un fichier
	 * @throws IOException erreur de lecture
	 */
	public String readFile(String path, Charset encoding) throws IOException {
		fileLoaded = true;
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	/** 
	 * Cherche un fichier dans l'ordinateur
	 * @return le fichier selectionne
	 */
	public File searchFile() {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

		int returnVal = jfc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File chosenFile = jfc.getSelectedFile();
			String[] splitName = chosenFile.getName().split("\\.");
			String format = splitName[splitName.length-1];

			if(format.equals("ucd")) {
				return chosenFile;
			}
			else {
				JOptionPane.showMessageDialog(this,
					    "Vous avez selectionne un fichier au format \"." + format + "\".\nSeul le format \".ucd\" est accepte.",
					    "Format de fichier incorrect",
					    JOptionPane.WARNING_MESSAGE);
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Detecte le type d'une instruction (attributs, methodes, relations ...) en grammaire BNF et fait le traitement adequat
	 * @param instruction	sequence de caractÃ¨res suivant une grammaire BNF decrivant un modÃ¨le UML
	 */
	public void findAndTreatType(String instruction) {
		Pattern patternClass = Pattern.compile("CLASS\\s\\w+\\s+ATTRIBUTES\\s+(\\w|\\:|\\,|\\(|\\)|\\s)*OPERATIONS(\\w|\\:|\\,|\\(|\\)|\\s)*");
		Pattern patternGeneralization = Pattern.compile("GENERALIZATION[\\s]+[\\w]+[\\s]+SUBCLASSES[\\w|\\,|\\s]+");
		Pattern patternRelation = Pattern.compile("RELATION\\s\\w+\\s+ROLES\\s+(CLASS \\w+\\s\\w+\\,*\\s+){2}");
		Pattern patternAggregation = Pattern.compile("AGGREGATION\\s+CONTAINER\\s+CLASS\\s\\w+\\s\\w+\\s+PARTS\\s+CLASS\\s\\w+\\s\\w+\\s+");

		Matcher matcherClass = patternClass.matcher(instruction);
		Matcher matcherGeneralization = patternGeneralization.matcher(instruction);
		Matcher matcherRelation = patternRelation.matcher(instruction);
		Matcher matcherAggregation = patternAggregation.matcher(instruction);

		if (matcherClass.find()) {
			String result = database.addClass(matcherClass.group(0));
			if (!result.isEmpty()) {
				// result a retourne un string non vide donc contient un message d'erreur
				System.out.println(result);
			}
			return;
		}

		if (matcherGeneralization.find()) {
			database.addGeneralization(matcherGeneralization.group(0));
			return;
		}

		if (matcherRelation.find()) {
			database.addAssociation(matcherRelation.group(0));
			return;
		}

		if (matcherAggregation.find()) {
			database.addAggregation(matcherAggregation.group(0));
			return;
		}
		return;
	}

	/** 
	 * Affiche les infos des classes dans le JPanel 
	 */
	public void afficherDansJPanel() {

		// recuperer les classes
		DefaultListModel<Classe> allClasse = database.getClasses();
		int nbClass = allClasse.size();

		jPanelClass.setLayout(new GridLayout(0, 1));

		for (int i = 0; i < nbClass; i++) {

			Classe classeActuel = allClasse.get(i);

			JLabel l = new JLabel(classeActuel.getName().toString(), SwingConstants.CENTER);

			Font font = new Font("Menlo", Font.PLAIN, 20);
			l.setFont(font);
			l.setForeground(Color.decode("#FFFFFF"));
			l.setOpaque(true);

			Color backcolor = Color.decode("#213F56");
			Color focuscolor = Color.decode("#894627");
			Color selectedcolor = Color.decode("#89462F");

			l.setBackground(backcolor);
			l.addMouseListener(new MouseListener() {
				public void mouseEntered(MouseEvent event) {
					if (l.getBackground() != selectedcolor) {
						l.setBackground(focuscolor);
					}
				}

				public void mouseExited(MouseEvent e) {
					if (l.getBackground() == focuscolor) {
						l.setBackground(backcolor);
						l.repaint();
					}
				}

				public void mousePressed(MouseEvent event) {}
				public void mouseReleased(MouseEvent event) {}

				public void mouseClicked(MouseEvent event) {
					//enlever les anciennes metrics
					metricsPane.setViewportView(null);
					// on reset la couleur des autres labels
					Component[] labels = jPanelClass.getComponents();
					for (int i = 0; i < nbClass; i++) {
						labels[i].setBackground(backcolor);
					}
					l.setBackground(selectedcolor);
					l.repaint();
					
					metricsButton.addMouseListener(new java.awt.event.MouseAdapter() {
						public void mouseReleased(java.awt.event.MouseEvent evt) {
							if(!metricsComputed) afficherMetrics(classeActuel); 
						}
					});
					
					// Classe selectionnee : on affiche ses details
					showClassInfo(classeActuel);
					selectedClass = classeActuel.getName().getValue();
					addListenerToShowDetails(attributePane);
					addListenerToShowDetails(methodPane);
					addListenerToShowDetails(subClassPane);
					addListenerToShowDetails(relationPane);
					addMetricListener(metricsPane);
				}
			});

			jPanelClass.add(l);
			jPanelClass.validate();
			jPanelClass.repaint();
		}
	}

	/**
	 * Charge les informations d'une Classe dans l'interface
	 * @param selectedClass classe dont on veut afficher les informations
	 */
	public void showClassInfo(Classe selectedClass) {

		DefaultListModel<StringDetail> listModel;
		JList<StringDetail> attributesJList;
		JList<StringDetail> methodsJList;
		JList<StringDetail> subClassesJList;
		JList<StringDetail> relationsJList;
		DefaultListModel<String> listModelMetrics;
		JList<String> metricsJList;

		Font font = new Font("Menlo", Font.PLAIN, 16);
		Color color = Color.decode("#894627");

		listModel = selectedClass.getAttributes(); // cree un modÃ¨le de liste d'objets StringDetail
		attributesJList = new JList<StringDetail>(listModel); // cree une JList contenant les objets StringDetail
																// (attributs) en utilisant le bon modele d'affichage
																// (listModel)
		listModel = selectedClass.getMethods();
		methodsJList = new JList<StringDetail>(listModel);
		listModel = selectedClass.getSubClasses();
		subClassesJList = new JList<StringDetail>(listModel);
		listModel = selectedClass.getRelations();
		relationsJList = new JList<StringDetail>(listModel);
		listModelMetrics = selectedClass.getListModelMetrics();
		metricsJList = new JList<String>(listModelMetrics);
		

		attributesJList.setFont(font);
		attributesJList.setForeground(color);
		methodsJList.setFont(font);
		methodsJList.setForeground(color);
		subClassesJList.setFont(font);
		subClassesJList.setForeground(color);
		relationsJList.setFont(font);
		relationsJList.setForeground(color);
		metricsJList.setFont(font);
		metricsJList.setForeground(color);
		
		// On affiche les métriques associées au classe uniquement si l'utilisateur ait préalablement demandé à les calculer
		if(metricsComputed) {
			metricsPane.setViewportView(metricsJList);
		}else {
			metricsPane.setViewportView(null);
		}

		// chargement des informations de la classe selectionne dans les panels
		attributePane.setViewportView(attributesJList); // ajout des attributs de la classe selectionnee
		methodPane.setViewportView(methodsJList); // ajout des methodes de la classe selectionnee
		subClassPane.setViewportView(subClassesJList); // ajout des sous-classes de la classe selectionnee
		relationPane.setViewportView(relationsJList); // ajout des relations de la classe selectionnee

		textDetails.setFont(font);
		textDetails.setEditable(false);
		textDetails.setText(selectedClass.getName().getDetail());
		textDetails.setForeground(color);
		detailPane.setViewportView(textDetails); // details BNF de Classe

	}
	
	/**
	 * Chaque clic sur un element affiche son detail BNF associe dans la partie Details
	 * @param panel  panel cible du lien d'ecoute
	 */
	@SuppressWarnings("unchecked")
	public void addListenerToShowDetails(JScrollPane panel) {
		JViewport viewport = panel.getViewport(); 
		JList<StringDetail> list = (JList<StringDetail>) viewport.getView(); 

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				textDetails.setText(list.getSelectedValue().getDetail());
			}
		});
	}
	
	/**
	 * Chaque clic sur une métrique affiche sa définition dans la partie Details
	 * @param panel  panel cible du lien d'ecoute
	 */
	@SuppressWarnings("unchecked")
	public void addMetricListener(JScrollPane panel) {
		JViewport viewport = panel.getViewport(); 
		JList<String> list = (JList<String>) viewport.getView(); 
		if(list == null) return; // Les métriques non pas encore été calculés, on ne mets pas de liens d'écoute

		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				String clickedData = list.getSelectedValue();
				String clickedMetric = clickedData.split("\\=")[0].trim();

				// On va chercher la définition a l'index qui correspond à la valeur de la metrique selectionnee
				for (MetricNames metricName : MetricNames.values()) {
					if(metricName.name().equals(clickedMetric)) {
						int indexEnum = metricName.ordinal();
						
						//On substitue le nom de la classe courante si nécessaire
						String definition = Metrics.metricsDefinition[indexEnum].replace("CLASSNAME", selectedClass); 
						textDetails.setText(definition);
						break;
					}
				}
				
			}
		});
	}

	public void closeMouseClicked(java.awt.event.MouseEvent evt) {
		System.exit(0);
	}

	public void produceCSV(){
		if(file != null){
			
			try {
				String csvFile = "./MetricsOfUML_Diagram.csv";
				FileWriter writer = new FileWriter(csvFile);
				database.generateCSVFileForMetrics(writer);
				
				String confirmAlert = "Un fichier \""+ csvFile.substring(2) +"\" contenant les métriques du \ndiagramme UML a été généré dans l'emplacement d'éxecution du programme.";
				JOptionPane.showMessageDialog(this, confirmAlert, "Création du fichier de métriques réussie", JOptionPane.INFORMATION_MESSAGE);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(this,
					    "Vous devez d'abord selectionner un fichier avant de pouvoir produire le fichier CSV correspondant.",
					    "Absence de fichier selectionne",
					    JOptionPane.WARNING_MESSAGE);
		}
	}
	
	public void afficherMetrics(Classe selectedClass){
		if (fileLoaded){
			database.computeAllMetrics();
			metricsComputed = true;
			// On rafraichit l'affichage des métriques de la classe en cours
			JList<String> metricsJList;
			DefaultListModel<String> listModelMetrics;
 			listModelMetrics = selectedClass.getListModelMetrics();
			metricsJList = new JList<String>(listModelMetrics);
 			Font font = new Font("Menlo", Font.PLAIN, 16);
			Color color = Color.decode("#894627");
 			metricsJList.setFont(font);
			metricsJList.setForeground(color);
 			metricsPane.setViewportView(metricsJList);
 			
 			
 		} else {
 			JOptionPane.showMessageDialog(this,
				    "Vous devez d'abord charger un fichier à l'aide du bouton 'Charger fichier' pour calculer des métriques.",
				    "Aucun fichier .ucd chargé",
				    JOptionPane.WARNING_MESSAGE);
 		}
	}

	/** 
	 * Initialise les elements de l'interface
	 */ 
	public void initComponents() {

		jPanelClassName = new JPanel();
		jLabel_Classe = new JLabel();
		jPanelClass = new JPanel();
		jPanelFile = new JPanel();
		loadFileButton = new JButton();
		filename = new JTextField();
		closeButton = new JLabel();
		cvsButton = new JButton();
		jPanelInfo = new JPanel();
		attributePane = new JScrollPane();
		methodPane = new JScrollPane();
		subClassPane = new JScrollPane();
		relationPane = new JScrollPane();
		detailPane = new JScrollPane();
		textDetails = new JTextArea(5, 8);
		metricsPane = new JScrollPane();
		metricsButton = new JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setUndecorated(true);

		// BOX CLASSES
		jPanelClassName.setBackground(new java.awt.Color(33, 63, 86));

		jLabel_Classe.setBackground(new java.awt.Color(255, 255, 255));
		jLabel_Classe.setFont(new java.awt.Font("Menlo", 0, 36)); // NOI18N
		jLabel_Classe.setForeground(new java.awt.Color(255, 255, 255));
		jLabel_Classe.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		jLabel_Classe.setText("Classes");

		jPanelClass.setBackground(new java.awt.Color(33, 63, 86));

		javax.swing.GroupLayout jPanelClassLayout = new javax.swing.GroupLayout(jPanelClass);
		jPanelClass.setLayout(jPanelClassLayout);
		jPanelClassLayout.setHorizontalGroup(jPanelClassLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 199, Short.MAX_VALUE));
		jPanelClassLayout.setVerticalGroup(jPanelClassLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 458, Short.MAX_VALUE));

		javax.swing.GroupLayout jPanelClassNameLayout = new javax.swing.GroupLayout(jPanelClassName);
		jPanelClassName.setLayout(jPanelClassNameLayout);
		jPanelClassNameLayout.setHorizontalGroup(
				jPanelClassNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								jPanelClassNameLayout.createSequentialGroup().addContainerGap()
										.addComponent(jLabel_Classe, javax.swing.GroupLayout.DEFAULT_SIZE, 242,
												Short.MAX_VALUE)
										.addContainerGap())
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelClassNameLayout
								.createSequentialGroup()
								.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanelClass, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(29, 29, 29)));
		jPanelClassNameLayout
				.setVerticalGroup(jPanelClassNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(jPanelClassNameLayout.createSequentialGroup().addGap(34, 34, 34)
								.addComponent(jLabel_Classe, javax.swing.GroupLayout.PREFERRED_SIZE, 63,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanelClass, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(44, Short.MAX_VALUE)));

		// BOX FILE
		jPanelFile.setBackground(new java.awt.Color(137, 70, 39));

		loadFileButton.setFont(new java.awt.Font("Menlo", 0, 13)); // NOI18N
		loadFileButton.setText("Charger fichier");

		filename.setBackground(new java.awt.Color(137, 70, 39));
		filename.setFont(new java.awt.Font("Menlo", 0, 14)); // NOI18N
		filename.setForeground(new java.awt.Color(255, 255, 255));
		filename.setHorizontalAlignment(javax.swing.JTextField.CENTER);
		filename.setBorder(javax.swing.BorderFactory.createCompoundBorder());

		closeButton.setBackground(new java.awt.Color(33, 63, 86));
		closeButton.setFont(new java.awt.Font("Menlo", 1, 24)); // NOI18N
		closeButton.setForeground(new java.awt.Color(33, 63, 86));
		closeButton.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		closeButton.setText("X");
		closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				closeMouseClicked(evt);
			}
		});

		cvsButton.setFont(new java.awt.Font("Menlo", 0, 13)); // NOI18N
        cvsButton.setText("Produire CVS");
        cvsButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				produceCSV();
			}
		});

		metricsButton.setFont(new java.awt.Font("Menlo", 0, 13)); // NOI18N
        metricsButton.setText("Calculer Métriques");
        metricsButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (file == null){
					JOptionPane.showMessageDialog(Parseur.this,
							    "Vous devez d'abord sélectionner un fichier avant de pouvoir calculer les métriques.",
							    "Absence de fichier sélectionné",
							    JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		javax.swing.GroupLayout jPanelFileLayout = new javax.swing.GroupLayout(jPanelFile);
		jPanelFile.setLayout(jPanelFileLayout);
		jPanelFileLayout.setHorizontalGroup(jPanelFileLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(jPanelFileLayout.createSequentialGroup()
                    .addGap(38, 38, 38)
					.addComponent(loadFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 175,javax.swing.GroupLayout.PREFERRED_SIZE)
					.addGap(68, 68, 68)
					.addComponent(filename, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
                    .addComponent(cvsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                	.addGap(18, 18, 18)
                	.addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
		jPanelFileLayout.setVerticalGroup(
            jPanelFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(closeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanelFileLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cvsButton)
                    .addGroup(jPanelFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelFileLayout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addComponent(filename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(loadFileButton)))
                .addGap(21, 21, 21))
        );

		// AUTRES BOX
		jPanelInfo.setBackground(new java.awt.Color(255, 255, 255));

		attributePane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Attributs",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP,
				new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

                metricsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Métriques", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

		methodPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Méthodes",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP,
				new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

		subClassPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sous-classes",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP,
				new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

		relationPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Associations/agrégations",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP,
				new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

		detailPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Details",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP,
				new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

		metricsPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Métriques", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N


		javax.swing.GroupLayout jPanelInfoLayout = new javax.swing.GroupLayout(jPanelInfo);
        jPanelInfo.setLayout(jPanelInfoLayout);

        jPanelInfoLayout.setHorizontalGroup(
            jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInfoLayout.createSequentialGroup()
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(detailPane, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelInfoLayout.createSequentialGroup()
                        .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(subClassPane)
                            .addComponent(attributePane, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(methodPane, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                            .addComponent(relationPane))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(metricsPane)
                    .addGroup(jPanelInfoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(metricsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        
		jPanelInfoLayout.setVerticalGroup(
            jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(attributePane, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                            .addComponent(methodPane, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(subClassPane, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(relationPane))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(detailPane, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelInfoLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(metricsButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(metricsPane)
                .addContainerGap())
        );
                
                
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jPanelClassName, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(jPanelFile, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jPanelInfo, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(jPanelClassName, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(jPanelFile, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jPanelInfo,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));

		pack();
	}  

	public static void main(String[] args) {
            new Parseur().setVisible(true);
	}
}