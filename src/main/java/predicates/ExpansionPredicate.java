package predicates;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.query.QuerySolution;

public class ExpansionPredicate {

	public List<TextTemplate> name;
	public int[] variables;
	
	/** 
	 * A predicate with variables is a predicate that has a variable ID (identified with an integer) in
	 * each of its variable slots.
	 * @param predicate
	 * @param variables array of variable IDs, must contain the exact number of variables that the predicates uses
	 */
	public ExpansionPredicate(List<TextTemplate> name, int[] variables) {
		for(int i = 0; i < variables.length; i++)
			if(variables[i] < 0)
				throw new RuntimeException("ERROR, trying to set a variable as a negative number");
		this.name = name;
		this.variables = variables;
	}
	
	
	public PredicateSemiInstantiation applyBinding(QuerySolution binding){
		String predicateName = "";
		for(TextTemplate tt : name) {
			if(tt.text != null) predicateName += tt.text;
			else predicateName += binding.get("v"+tt.var);  
		}
		Predicate basePredicate = new Predicate(predicateName,variables.length);
		String[] partialInstantiation = new String[variables.length];
		for(int i = 0; i < variables.length; i++) {
			String varname = "v"+variables[i];
			if(binding.contains(varname) && ! binding.get(varname).isAnon()) {
				if(binding.get(varname).isLiteral()) {					
					partialInstantiation[i] = binding.get(varname).asLiteral().getLexicalForm();
				} else {
					partialInstantiation[i] = binding.get(varname).asResource().getLocalName();
				}
			}
		}

		return new PredicateSemiInstantiation(basePredicate,partialInstantiation);
	}
	
	//Pair<Set<String>,Set<Pair<Integer,String>>> 
	public Set<Triple<String,Integer,String>> toSPARQLquerySnippet(Set<Predicate> predicates) {
		Set<Pair<Integer,String>> matchedVariables = new HashSet<Pair<Integer,String>>();
		Set<Triple<String,Integer,String>> possibilities = new HashSet<Triple<String,Integer,String>>();
		for(Predicate p: predicates) {
			Pair<Integer,String> variableForPredicateMatch = PredicateExpansion.matches(this, p);
			if(variableForPredicateMatch != null) {
				//matchedVariables.add(variableForPredicateMatch);
				possibilities.add(
						new ImmutableTriple<String,Integer,String>(
								p.toSPARQLquerySnippet(variables),
								variableForPredicateMatch.getLeft(),
								variableForPredicateMatch.getRight()
								)
						);
			}
		}
		return possibilities;
	}
	
	
	public String prettyToString() {
		String s = "";
		for(TextTemplate tt : name) {
			s += tt.prettyToString();
		}
		s += "(";
		for(int i = 0; i < variables.length; i++) {
			s += "?v"+variables[i]+"";
			if(i < variables.length -1)
				s += ",";
		}
		return s+")";
	}
	
	/**
	 * 
	 * @return the set of variable IDs used in this predicate
	 */
	public Set<Integer> getVariableSet(){
		Set<Integer> vars = new HashSet<Integer>();
		for(int i = 0; i < variables.length; i++) {
			vars.add(new Integer(variables[i]));
		}
		for(TextTemplate tt: name) {
			if(tt.text != null) vars.add(new Integer(tt.var));
		}
		return vars;
	}
}
