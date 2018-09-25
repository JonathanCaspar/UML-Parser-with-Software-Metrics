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
				    	String fileStrings = readFile(file.toPath().toString(),Charset.forName("UTF-8"));

				    	//System.out.println(fileStrings);

				    	//decouper avec ; (classe, generalisation, relations)
				    	String[] tab = fileStrings.split(";");

				    	for (int i=0; i<tab.length; i++) {
				    		System.out.println(tab[i]);
				    	}

				    } catch(IOException ex){
				    	System.out.println("Could not read file");
				    }
				} else {
					System.out.println("File access cancelled by user.");
				}
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
