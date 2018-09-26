package com.parseur.main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.awt.event.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;


public class Parseur extends JPanel {
	static JFrame frame;
	
	public JButton loadFileButton;
	public JTextArea fileName;
	public JScrollPane classBox;
	public JScrollPane attributeBox;
	public JScrollPane methodBox;
	public JScrollPane subClassBox;
	public JScrollPane relationsBox;
	public JScrollPane detailsBox;
	
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
		
		loadFileButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				file = searchFile();
				if(file != null){
					fileName.setText(file.getName());

					try{
				    	fileStrings = readFile(file.toPath().toString(),Charset.forName("UTF-8"));

				    	//decouper avec ; (classe, generalisation, relations)
				    	String[] tab = fileStrings.split(";");

				    	int j=0;
				    	do{
						decoupeClass(fileStrings, tab,j);//decortiquer les classes
						j++;
				    	}
				    	while(tab[j].contains("ATTRIBUTES"));//jusqu'à avoir passé toutes les classes


				    } catch(IOException ex){
				    	System.out.println("Could not read file");
				    }
				} else {
					System.out.println("File access cancelled by user.");
				}
			}
		});
	}
	
	
	//decoupe les classes
	public void decoupeClass(String fileStrings, String[] tab, int j){
		//CLASS
		int i1Class = tab[j].indexOf("CLASS") + 6;
		int i2Class = tab[j].indexOf("ATTRIBUTES") - 1;
		String className = tab[j].substring(i1Class,i2Class);


		//ATTRIBUTES
		int i1Att = tab[j].indexOf("ATTRIBUTES") + 10;
		int i2Att = tab[j].indexOf("OPERATIONS") - 1;
		String allAttributes = tab[j].substring(i1Att,i2Att);

		//nb de virgule
		int nbA = count(allAttributes, "," );
		
		//tableau de tout les attributs de la class
		String[] tabAttributs = new String[nbA+1];

		//si plusieurs attributs
		if(nbA>0){
			String[] t = allAttributes.split(",");
			for (int i=0; i<t.length; i++) {
				tabAttributs[i] = t[i].trim();
			}
		} else if(nbA == 0){//soit 0 ou 1 attribut
			tabAttributs[0] = allAttributes.trim();
		}


		//OPERATIONS
		int i1Ope = tab[j].indexOf("OPERATIONS") + 10;
		int i2Ope = tab[j].length();
		String allOperations = tab[j].substring(i1Ope,i2Ope);

		//nb d'opeartions
		int nbO = count(allOperations, "," ); //compte le nb de virgule

		//tableau de toutes les operations de la class
		String[] tabOperations = new String[nbO+1];

		//si plusieurs opérations
		if(nbO>0){
			String[] t2 = allOperations.split(",");
			for (int i=0; i<t2.length; i++) {
				tabOperations[i] = t2[i].trim();
			}
		} else if(nbO == 0){//soit 0 ou 1 attribut
			tabOperations[0] = allOperations.trim();
		}


		//creer une nouvelle classe avec la liste d'attributs correspondants
		Classe classe = new Classe(className, tabAttributs, tabOperations);

		//supprimer chaines deja faites
		fileStrings = fileStrings.substring(tab[0].length(),fileStrings.length());
	}

	//compte le nombre de l dans str
	public int count(String str, String l){
		if (str.isEmpty() || l.isEmpty()) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(l, idx)) != -1) {
			count++;
			idx += l.length();
		}
		return count;
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
	
	
	// Ajoute une fenêtre interne
	public JScrollPane createBox(String title) {
		String[] data = new String[]{}; //si aucune donnée : fenêtre vide
		
		JList<String> list = new JList<String>(data);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        
		// Ajout à la fenêtre principale
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
	
	// Ajoute une zone de texte interne non-sélectionnable
	public JTextArea createTextArea(String text) {
		JTextArea area = new JTextArea(text);
		area.setEnabled(false);
		add(area);
		return area;
	}
	
	// Modifie le contenu d'une liste
	public void changeBoxData(JScrollPane list, String[] newData) {
		if (newData == null) return;
		JList<String> newList = new JList<String>(newData);
		list.setViewportView(newList);
	}
	
	public void runAndShowGUI() {
		//Create and set up the window.
        frame = new JFrame("Extracteur UML");
        frame.setSize(400, 400);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        changeBoxData(methodBox, new String[] {"tesr","ujbgyfy"});
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(this);
        frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		Parseur test = new Parseur();
		test.runAndShowGUI();
	}

}
