package com.parseur.main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.awt.event.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import java.awt.Dimension;

import javax.naming.spi.DirStateFactory.Result;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class Parseur extends JPanel {
	static JFrame frame;
	
	public JButton loadFileButton;
	public JTextArea fileName;
	public JScrollPane classBox;
	public JScrollPane attributeBox;
	public JScrollPane methodBox;
	public JScrollPane subClassBox;
	public JScrollPane relationsBox;
	public JTextArea detailsBox;
	
	public Database database;
	public File file;
	
	public String fileStrings;
	
	public Parseur() {
		loadFileButton = createButton("Charger fichier");
		fileName = createTextArea("");
		classBox = createBox("Classes");
		attributeBox = createBox("Attributs");
		methodBox = createBox("Méthodes");
		subClassBox = createBox("Sous-classes");
		relationsBox = createBox("Relations/Agrégations");
		detailsBox = createTextArea("Détails");
		
		database = new Database();
		
		loadFileButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				file = searchFile();
				if(file != null){
					fileName.setText(file.getName());

					try{
						database.resetDB();
				    	fileStrings = readFile(file.toPath().toString(),Charset.forName("UTF-8"));

				    	//decouper avec ; (classe, generalisation, relations)
				    	String[] tab = fileStrings.split("\\;");

				    	
				    	for(int j=0; j < tab.length; j++){
				    		findAndTreatType(tab[j]);
				    	}
				    	database.showDBcontent();
				    	updateClasses(database);
				    	enableClassListener();

				    } catch(IOException ex){
				    	System.out.println("Could not read file");
				    }
				} else {
					System.out.println("File access cancelled by user.");
				}
			}
		});
	}
	
	public void updateClasses(Database database) {
		DefaultListModel<Classe> listModel = database.getClasses();
		JList<Classe> classesJList = new JList<Classe>(listModel);
		classBox.setViewportView(classesJList);
	}
	
	public void enableClassListener() {
		JViewport viewport = classBox.getViewport(); 
		JList classList = (JList) viewport.getView(); 
		classList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList list = (JList) e.getSource();
				Classe selectedClass = (Classe) list.getSelectedValue();	
				DefaultListModel<StringDetail> listModel;
				JList<StringDetail> attributesJList;
				JList<StringDetail> methodsJList;
				JList<StringDetail> subClassesJList;
				JList<StringDetail> relationsJList;
				
				listModel = selectedClass.getAttributes(); // crée un modèle de liste d'objets StringDetail
				attributesJList = new JList<StringDetail>(listModel); // crée une JList contenant les objets StringDetail (attributs) en utilisant le bon modele d'affichage (listModel)
				listModel = selectedClass.getMethods();
				methodsJList = new JList<StringDetail>(listModel);
				listModel = selectedClass.getSubClasses();
				subClassesJList = new JList<StringDetail>(listModel);
				listModel = selectedClass.getRelations();
				relationsJList = new JList<StringDetail>(listModel);
				
				detailsBox.setText(selectedClass.getName().getDetail()); // détails BNF de Classe
				attributeBox.setViewportView(attributesJList); // ajout des attributs de la classe selectionnée
				methodBox.setViewportView(methodsJList); // ajout des méthodes de la classe selectionnée
				subClassBox.setViewportView(subClassesJList); // ajout des sous-classes de la classe selectionnée
				relationsBox.setViewportView(relationsJList); // ajout des relations de la classe selectionnée
				
			}
		});
	}
	
	//met le fichier dans un String
	public String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	//cherche un fichier dans l'odinateur
	public File searchFile() {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

		int returnVal = jfc.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {return jfc.getSelectedFile();} 
		else {return null;}
	}
	
	
	public void findAndTreatType(String instruction){
		Pattern patternClass          = Pattern.compile("CLASS\\s\\w+\\s+ATTRIBUTES\\s+(\\w|\\:|\\,|\\(|\\)|\\s)*OPERATIONS(\\w|\\:|\\,|\\(|\\)|\\s)*");
		Pattern patternGeneralization = Pattern.compile("GENERALIZATION[\\s]+[\\w]+[\\s]+SUBCLASSES[\\w|\\,|\\s]+");
		Pattern patternRelation       = Pattern.compile("RELATION\\s\\w+\\s+ROLES\\s+(CLASS \\w+\\s\\w+\\,*\\s+){2}");
		Pattern patternAggregation    = Pattern.compile("AGGREGATION\\s+CONTAINER\\s+CLASS\\s\\w+\\s\\w+\\s+PARTS\\s+CLASS\\s\\w+\\s\\w+\\s+");
		
		
		Matcher matcherClass = patternClass.matcher(instruction);
		Matcher matcherGeneralization = patternGeneralization.matcher(instruction);
		Matcher matcherRelation = patternRelation.matcher(instruction);
		Matcher matcherAggregation = patternAggregation.matcher(instruction);
		
		if (matcherClass.find()) {
			String result = database.addClass(matcherClass.group(0));
			if(!result.isEmpty()) {
				// result a retourné un string non vide donc contient un message d'erreur
				JOptionPane.showMessageDialog(frame, result , "Erreur - Doublon détecté !",  JOptionPane.OK_CANCEL_OPTION);
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
	
	// Ajoute une fenetre interne
	public JScrollPane createBox(String title) {
		String[] data = new String[]{}; //si aucune donnÃ©e : fenÃªtre vide
		
		JList<String> list = new JList<String>(data);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        
		// Ajout Ã  la fenÃªtre principale
		add(new JLabel(title));
		add(listScroller);
		
		return listScroller;
	}
	
	// Ajoute un bouton interne
	public JButton createButton(String text) {
		JButton button = new JButton(text);
		add(button);
		return button;
	}
	
	// Ajoute une zone de texte interne non-sÃ©lectionnable
	public JTextArea createTextArea(String text) {
		JTextArea area = new JTextArea(text);
		area.setEnabled(false);
		add(area);
		return area;
	}
	
	// Modifie le contenu d'une liste
	public void changeBoxData(JScrollPane pane, ArrayList<StringDetail> newData) {
		if (newData == null) return;
		String[] value = new String[newData.size()];
		String[] details = new String[newData.size()];
		
		for (int i = 0; i < newData.size(); i++) {
			value[i]   = newData.get(i).getValue();
			details[i] = newData.get(i).getDetail();
		}
		JList<String> newList = new JList<String>(value);
		pane.setViewportView(newList);
	}
	
	public void runAndShowGUI() {
		//Create and set up the window.
        frame = new JFrame("Extracteur UML");
        frame.setSize(400, 400);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		Parseur test = new Parseur();
		test.runAndShowGUI();
	}

}
