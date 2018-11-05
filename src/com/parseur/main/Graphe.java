package com.parseur.main;

import java.util.ArrayList;

public class Graphe {
	Node racine;
	ArrayList<Classe> classes;
	
	public Graphe() {
		this.racine = new Node(null);
	}
	
	public void generateGraph(ArrayList<Classe> classes) {
		for (Classe c : classes) {
			//?
		}
	}
}

class Node {
	Classe classe;
	ArrayList<Classe> childrens;
	
	public Node(Classe c) {
		this.classe = c;
	}
	
	public void addChild(Classe c) {
		if(!childrens.contains(c) && (c != classe)) {
			this.childrens.add(c);
		}
	}
}
