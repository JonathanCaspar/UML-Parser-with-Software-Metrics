package com.parseur.main;

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
	
	public Parseur() {
		loadFileButton = createButton("Charger fichier");
		fileName = createTextArea("");
		classBox = createBox("Classes");
		attributeBox = createBox("Attributs");
		methodBox = createBox("Méthodes");
		subClassBox = createBox("Sous-classes");
		relationsBox = createBox("Relations/Agrégations");
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
