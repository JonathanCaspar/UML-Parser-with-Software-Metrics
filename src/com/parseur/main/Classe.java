package com.parseur.main;

import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;

public class Classe {
	private StringDetail nom;
	private StringDetail attributes;
	private StringDetail methods;
	private StringDetail subClass;
	private StringDetail relations;
	
	private MetricsData metrics;
	
	public Classe() {
		nom = new StringDetail();
		attributes = new StringDetail();
		methods = new StringDetail();
		subClass = new StringDetail();
		relations = new StringDetail();
		metrics = new MetricsData();
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
	
	public void setMetrics(String[] data) {
		metrics.updateMetrics(data);
	}
	
	/**Concatene les relations de la Classe dans une seule chaine de caracteres
	 * @param	value   informations liees a la relation
	 * @param	detail  grammaire BNF qui decrit la relation
	 */
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
		DefaultListModel<StringDetail> listModel = new DefaultListModel<StringDetail>();
		String[] splitAttributes = attributes.getValue().split("\\;");
		
		for(int i = 0; i < splitAttributes.length; i++) {
			listModel.addElement(new StringDetail(splitAttributes[i], attributes.getDetail()));
		}
		return listModel; 
	}
	
	public DefaultListModel<StringDetail> getMethods() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel<StringDetail>();
		String[] splitMethods = methods.getValue().split("\\;");
		
		for(int i = 0; i < splitMethods.length; i++) {
			listModel.addElement(new StringDetail(splitMethods[i], methods.getDetail()));
		}
		return listModel; 
	}
	
	public DefaultListModel<StringDetail> getSubClasses() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel<StringDetail>();
		String[] splitSubClasses = subClass.getValue().split("\\;");
		
		for(int i = 0; i < splitSubClasses.length; i++) {
			listModel.addElement(new StringDetail(splitSubClasses[i], subClass.getDetail()));
		}
		return listModel; 
	}
	
	public DefaultListModel<StringDetail> getRelations() { 
		DefaultListModel<StringDetail> listModel = new DefaultListModel<StringDetail>();
		String[] splitRelationsValue = relations.getValue().split("\\;");
		String[] splitRelationsDetail = relations.getDetail().split("\\;");
		
		for(int i = 0; i < splitRelationsValue.length; i++) {
			listModel.addElement(new StringDetail(splitRelationsValue[i], splitRelationsDetail[i]));
		}
		return listModel; 
	}
	
	/**
	 * @return le MetricData de l'objet Classe
	 */
	public MetricsData getMetrics() {
		return this.metrics;
	}
	
	public DefaultListModel<String> getListModelMetrics() { 
		DefaultListModel<String> listModel = new DefaultListModel<String>();	
		Map<String, String> metricsDict = metrics.getDict();
		
		Set<String> keys = metricsDict.keySet();
        for(String key: keys){
        	listModel.addElement(key + " = " + metricsDict.get(key));
        }
		
		return listModel; 
	}

	/**
	 * Calcule les metriques d'une classe donnee
	 * @param classes
	 */
	
	public String toString() {
		return this.nom.getValue();
	}
}

/**
 * Permet de creer un objet regroupant une valeur (nom, attributs..) avec sa description BNF
 */
class StringDetail {
	private String value;
	private String detail;
	
	/**
	 * Regroupe deux valeurs de type String
	 * @param	value   information
	 * @param	detail  grammaire BNF qui decrit l'information a extraire
	 */
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
