package logic;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public class JSONoutput {

	public static void outputAsJSON(String filename, Set<PredicateInstantiation> availablePredicates) {
		JsonArray ja = new JsonArray();
		for(PredicateInstantiation pi : availablePredicates) {
			JsonObject jo = new JsonObject();
			Predicate p = pi.getPredicate();
			jo.add("propertyName", new JsonPrimitive(p.getName()));
			JsonArray jVars = new JsonArray();
			for(int i = 0; i < pi.getBindings().length; i++) {
				Binding b = pi.getBinding(i);
				if(b.isConstant() && b.getConstant().isURI()) {
					JsonObject URI = new JsonObject();
					URI.add("URI", new JsonPrimitive(b.getConstant().getLexicalValue()));
					jVars.add(URI);
				} else if(b.isConstant() && b.getConstant().isLiteral()) {
					JsonObject literal = new JsonObject();
					literal.add("lexicalValue", new JsonPrimitive(b.getConstant().getLexicalValue()));
					jVars.add(literal);
				} else if (b.isVar()){
					jVars.add(b.getVar());
				}
			}
			JsonArray label = new JsonArray();
			for (TextTemplate tt : p.getTextLabel()) {
				if(tt.isVar()) label.add(tt.getVar());
				else label.add(tt.getText());
			}
			jo.add("propertyVariables", jVars);
			jo.add("label", label);
			ja.add(jo);
		}
		
		try (Writer writer = new FileWriter(filename)) {
		    Gson gson = new GsonBuilder().setPrettyPrinting().create();
		    gson.toJson(ja, writer);
		    System.out.println("Available predicates have been saved to the JSON file "+filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR while saving this JSON to file "+filename);
			System.out.println(ja);
			e.printStackTrace();
		}
	}
}
