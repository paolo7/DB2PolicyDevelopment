package predicates;

public class TextTemplate {

	public String text;
	public int var;
	
	public TextTemplate(String text) {
		this.text = text;
		var = -1;
	}
	
	public TextTemplate(int var) {
		this.var = var;
	}
	
	public String prettyToString() {
		if(text != null) return text;
		else return "[?v"+var+"]";
	}
}
