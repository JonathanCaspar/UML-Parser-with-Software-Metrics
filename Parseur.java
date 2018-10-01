package com.parseur.main;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class Parseur extends javax.swing.JFrame {
	
	public Database database;
	public File file;
	public String fileStrings;

	private JLabel close;
    private JButton jButton_file;
    private JLabel jLabel_Classe;
	private JPanel jPanelInfo;
    private JPanel jPanelClass;
    private JPanel jPanelClassName;
    private JPanel jPanelFile;
    private JScrollPane jPanelAtt;
    private JScrollPane jPanelRel;
    private JScrollPane jPanelSous;
    private JScrollPane jPanelAss;
    private JScrollPane jPanelDetail;
    private JTextField jTextField_fileName;
	
	public Parseur() {

		initComponents();
		
		database = new Database();
		
		jButton_file.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                    file = searchFile();
                    if(file != null){
                        jTextField_fileName.setText(file.getName());
                        jTextField_fileName.setEditable(false);

                        try{
                        	database.resetDB();
					    	fileStrings = readFile(file.toPath().toString(),Charset.forName("UTF-8"));

					    	//decouper avec ; (classe, generalisation, relations)
					    	String[] tab = fileStrings.split("\\;");

				    	
					    	for(int j=0; j < tab.length; j++){
					    		findAndTreatType(tab[j]);
					    	}
					    	database.showDBcontent();

					    	afficherDansJPanel();

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
				//JOptionPane.showMessageDialog(frame, result , "Erreur - Doublon détecté !",  JOptionPane.OK_CANCEL_OPTION);
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


	//affiche les infos des classes dans le JPanel
    public void afficherDansJPanel(){

    	//recuperer les classes
    	DefaultListModel<Classe> allClasse = database.getClasses();

    	int nbClass = allClasse.size();

    	jPanelClass.setLayout(new GridLayout(0, 1));


        for(int i=0; i<nbClass;i++){ 

        	Classe classeActuel = allClasse.get(i);

            JLabel l = new JLabel(classeActuel.getName().toString(),SwingConstants.CENTER);
            
            Font font = new Font("Menlo", Font.PLAIN, 20);
            l.setFont(font);
            l.setForeground(Color.decode("#FFFFFF"));
            l.setOpaque(true);
            
            Color backcolor = Color.decode("#213F56");
            Color focuscolor = Color.decode("#894627");
            
            l.setBackground(backcolor);
            l.addMouseListener(new MouseListener(){
                public void mouseEntered(MouseEvent event) {
                    l.setBackground(focuscolor);
                }
                public void mouseExited(MouseEvent e){
                    if (l.getBackground() == focuscolor) {
                        l.setBackground(backcolor);
                        l.repaint();
                    }
                }

                public void mousePressed(MouseEvent event) {}
                public void mouseReleased(MouseEvent event) {}
                public void mouseClicked(MouseEvent event) {
                	changeValue(classeActuel);
                }
                
            });
            
            jPanelClass.add(l);
            jPanelClass.validate();
            jPanelClass.repaint();
        }
    }

    public void changeValue(Classe selectedClass){

		DefaultListModel<StringDetail> listModel;
		JList<StringDetail> attributesJList;
		JList<StringDetail> methodsJList;
		JList<StringDetail> subClassesJList;
		JList<StringDetail> relationsJList;

		Font font = new Font("Menlo", Font.PLAIN, 16);
		Color color = Color.decode("#894627");
		
		listModel = selectedClass.getAttributes(); // crée un modèle de liste d'objets StringDetail
		attributesJList = new JList<StringDetail>(listModel); // crée une JList contenant les objets StringDetail (attributs) en utilisant le bon modele d'affichage (listModel)
		listModel = selectedClass.getMethods();
		methodsJList = new JList<StringDetail>(listModel);
		listModel = selectedClass.getSubClasses();
		subClassesJList = new JList<StringDetail>(listModel);
		listModel = selectedClass.getRelations();
		relationsJList = new JList<StringDetail>(listModel);

		attributesJList.setFont(font);
		attributesJList.setForeground(color);
		methodsJList.setFont(font);
		methodsJList.setForeground(color);
		subClassesJList.setFont(font);
		subClassesJList.setForeground(color);
		relationsJList.setFont(font);
		relationsJList.setForeground(color);
		
		jPanelAtt.setViewportView(attributesJList); // ajout des attributs de la classe selectionnée
		jPanelRel.setViewportView(methodsJList); // ajout des méthodes de la classe selectionnée
		jPanelSous.setViewportView(subClassesJList); // ajout des sous-classes de la classe selectionnée
		jPanelAss.setViewportView(relationsJList); // ajout des relations de la classe selectionnée

		JTextArea textDetails = new JTextArea(5, 8);
		textDetails.setFont(font);
		textDetails.setEditable(false);
		textDetails.setText(selectedClass.getName().getDetail());
		textDetails.setForeground(color);
		jPanelDetail.setViewportView(textDetails); // détails BNF de Classe
		
    }
	
	

    public void closeMouseClicked(java.awt.event.MouseEvent evt) {                                   
        System.exit(0);
    }


    
   	//initialise les elements du JFrame
    public void initComponents() {

        jPanelClassName = new JPanel();
        jLabel_Classe = new JLabel();
        jPanelClass = new JPanel();
        jPanelFile = new JPanel();
        jButton_file = new JButton();
        jTextField_fileName = new JTextField();
        close = new JLabel();
        jPanelInfo = new JPanel();
        jPanelAtt = new JScrollPane();
        jPanelRel = new JScrollPane();
        jPanelSous = new JScrollPane();
        jPanelAss = new JScrollPane();
        jPanelDetail = new JScrollPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        //BOX CLASSES
        jPanelClassName.setBackground(new java.awt.Color(33, 63, 86));

        jLabel_Classe.setBackground(new java.awt.Color(255, 255, 255));
        jLabel_Classe.setFont(new java.awt.Font("Menlo", 0, 36)); // NOI18N
        jLabel_Classe.setForeground(new java.awt.Color(255, 255, 255));
        jLabel_Classe.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Classe.setText("Classes");

        jPanelClass.setBackground(new java.awt.Color(33, 63, 86));

        javax.swing.GroupLayout jPanelClassLayout = new javax.swing.GroupLayout(jPanelClass);
        jPanelClass.setLayout(jPanelClassLayout);
        jPanelClassLayout.setHorizontalGroup(
            jPanelClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 199, Short.MAX_VALUE)
        );
        jPanelClassLayout.setVerticalGroup(
            jPanelClassLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanelClassNameLayout = new javax.swing.GroupLayout(jPanelClassName);
        jPanelClassName.setLayout(jPanelClassNameLayout);
        jPanelClassNameLayout.setHorizontalGroup(
            jPanelClassNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelClassNameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel_Classe, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelClassNameLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanelClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29))
        );
        jPanelClassNameLayout.setVerticalGroup(
            jPanelClassNameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelClassNameLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(jLabel_Classe, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelClass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(44, Short.MAX_VALUE))
        );


        //BOX FILE
        jPanelFile.setBackground(new java.awt.Color(137, 70, 39));

        jButton_file.setFont(new java.awt.Font("Menlo", 0, 13)); // NOI18N
        jButton_file.setText("Charger fichier");

        jTextField_fileName.setBackground(new java.awt.Color(137, 70, 39));
        jTextField_fileName.setFont(new java.awt.Font("Menlo", 0, 14)); // NOI18N
        jTextField_fileName.setForeground(new java.awt.Color(255, 255, 255));
        jTextField_fileName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField_fileName.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        close.setBackground(new java.awt.Color(33, 63, 86));
        close.setFont(new java.awt.Font("Menlo", 1, 24)); // NOI18N
        close.setForeground(new java.awt.Color(33, 63, 86));
        close.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        close.setText("X");
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanelFileLayout = new javax.swing.GroupLayout(jPanelFile);
        jPanelFile.setLayout(jPanelFileLayout);
        jPanelFileLayout.setHorizontalGroup(
            jPanelFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jButton_file, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField_fileName, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(close, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelFileLayout.setVerticalGroup(
            jPanelFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelFileLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanelFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_file)
                    .addComponent(jTextField_fileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(jPanelFileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(close, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );


        //AUTRES BOX
        jPanelInfo.setBackground(new java.awt.Color(255, 255, 255));

        jPanelAtt.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Attributs", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

        jPanelRel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Méthodes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

        jPanelSous.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Sous-classes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

        jPanelAss.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Associations/agrégations", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

        jPanelDetail.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Menlo", 1, 18), new java.awt.Color(33, 63, 86))); // NOI18N

        
        javax.swing.GroupLayout jPanelInfoLayout = new javax.swing.GroupLayout(jPanelInfo);
        jPanelInfo.setLayout(jPanelInfoLayout);
        jPanelInfoLayout.setHorizontalGroup(
            jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInfoLayout.createSequentialGroup()
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelInfoLayout.createSequentialGroup()
                        .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanelSous)
                            .addComponent(jPanelAtt, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanelRel)
                            .addComponent(jPanelAss)))
                    .addComponent(jPanelDetail))
                .addContainerGap())
        );
        jPanelInfoLayout.setVerticalGroup(
            jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelAtt, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                    .addComponent(jPanelRel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanelSous, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelAss))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelDetail, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelClassName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelClassName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanelFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
	
	public void runAndShowGUI() {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Parseur().setVisible(true);
            }
        });
	}
	
	public static void main(String[] args) {
		Parseur test = new Parseur();
		test.runAndShowGUI();
	}

}
