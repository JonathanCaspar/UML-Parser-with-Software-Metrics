package com.parseur.main;

import javax.swing.DefaultListModel;

public class Classe {
	private StringDetail nom;
	private StringDetail attributes;
	private StringDetail methods;
	private StringDetail subClass;
	private StringDetail relations;
	
	public Classe() {
		nom = new StringDetail();
		attributes = new StringDetail();
		methods = new StringDetail();
		subClass = new StringDetail();
		relations = new StringDetail();
	}
	
	public void setName(String value, String detail) {
		nom.set(value, detail);
	}
	
	public void setAttributes(String value, String detail) {
		attributes.set(value, detail);
	}
	
	public void setMethods(String value, String detail) {
		methods.set(value, detail);
	}
	
	public void setSubClasses(String value, String detail) {
		subClass.set(value, detail);
	}
	
	public void addRelation(String value, String detail) {
		String oldValue = relations.getValue();
		String oldDetail = relations.getDetail();
		
		if (!oldValue.isEmpty()) {
			 oldValue += ";";
			 oldDetail += ";";
		}
		relations.set(oldValue + value, oldDetail + detail);
	}
	
	public StringDetail getName() { return nom; }
	
	public DefaultListModel<StringDetail> getAttributes() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel();
		String[] splitAttributes = attributes.getValue().split("\\;");
		
		for(int i = 0; i < splitAttributes.length; i++) {
			listModel.addElement(new StringDetail(splitAttributes[i], attributes.getDetail()));
		}
		return listModel; 
	}
	
	public DefaultListModel<StringDetail> getMethods() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel();
		String[] splitMethods = methods.getValue().split("\\;");
		
		for(int i = 0; i < splitMethods.length; i++) {
			listModel.addElement(new StringDetail(splitMethods[i], methods.getDetail()));
		}
		return listModel; 
	}
	
	public DefaultListModel<StringDetail> getSubClasses() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel();
		String[] splitSubClasses = subClass.getValue().split("\\;");
		
		for(int i = 0; i < splitSubClasses.length; i++) {
			listModel.addElement(new StringDetail(splitSubClasses[i], subClass.getDetail()));
		}
		return listModel; 
	}
	
	public DefaultListModel<StringDetail> getRelations() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel();
		String[] splitRelationsValue = relations.getValue().split("\\;");
		String[] splitRelationsDetail = relations.getDetail().split("\\;");
		
		for(int i = 0; i < splitRelationsValue.length; i++) {
			listModel.addElement(new StringDetail(splitRelationsValue[i], splitRelationsDetail[i]));
		}
		return listModel; 
	}
	
	
	public String toString() {
		return this.nom.getValue();
	}
}


// Permet de créer un objet regroupant une valeur (nom, attributs..) avec sa description BNF
class StringDetail {
	private String value;
	private String detail;
	
	public StringDetail(String value, String detail) {
		this.value = value;
		this.detail = detail;
	}
	
	public StringDetail() {
		this.value = "";
		this.detail = "";
	}
	
	public String getValue() {return this.value;}
	public String getDetail() {return this.detail;}
	
	public void set(String value, String detail) {
		this.value = value;
		this.detail = detail;
	}
	
	public String toString() {
		return value;
	}
	
}
