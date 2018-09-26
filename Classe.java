public class Classe {
	
	public String  className;
	public String[] liste_attributs;
	public String[] liste_operations;

	public Classe(String className, String[] liste_attributs, String[] liste_operations){
		this.className = className;
		this.liste_attributs = liste_attributs;
		this.liste_operations = liste_operations;

		for (int i=0; i<liste_attributs.length; i++) {
			System.out.println(liste_attributs[i]);
			System.out.println("*");
		}

		for (int i=0; i<liste_operations.length; i++) {
			System.out.println(liste_operations[i]);
			System.out.println("%");
			System.out.println("  ");
		}
	}
}