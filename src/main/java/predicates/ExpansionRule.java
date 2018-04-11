package predicates;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.query.QuerySolution;

public class ExpansionRule {

	public Set<ExpansionPredicate> antecedent;
	public Set<ExpansionPredicate> consequent;
	
	public Set<Predicate> predicates;
	
	public ExpansionRule(Set<ExpansionPredicate> antecedent, Set<ExpansionPredicate> consequent, Set<Predicate> newpredicates) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.predicates = newpredicates;
		if (! validateRule()) {
			throw new RuntimeException("ERROR, trying to define an invalid rule. A possible explanation is that not all the variables of the consequent are defined in the antecedent.");
		}
	}
	
	public String getAntecedentSPARQL() {
		String SPARQL = "SELECT ";
		// get the SELECT variables
		Set<Integer> selectVars = new HashSet<Integer>();
		for(ExpansionPredicate ep : consequent) {
			for(int i = 0; i < ep.variables.length; i++) {
				selectVars.add(new Integer(ep.variables[i]));
			}
			for(TextTemplate tt : ep.name) {
				if(tt.var >= 0) selectVars.add(new Integer(tt.var));
			}
		}
		boolean first = true;
		for(Integer i: selectVars) {
			if (first) first = false;
			else SPARQL += " ";
			SPARQL += "?v"+i;
		}
		SPARQL += "\nWHERE {\n";
		// 
		for(ExpansionPredicate ep : antecedent) {
			Set<Triple<String,Integer,String>> result = ep.toSPARQLquerySnippet(predicates);
			//Set<String> alternatives = result.getLeft();
			/*for(Pair<Integer,String> mapping : result.getRight()) {		
				binding += "BIND (\""+mapping.getRight()+"\" AS ?v"+mapping.getLeft()+")";
				
			}*/
			if(result.size() == 1) {
				String binding = "";
				SPARQL += result.iterator().next().getLeft()+" \n";
				if(result.iterator().next().getMiddle() != null)
					binding = "BIND (\""+result.iterator().next().getRight()+"\" AS ?v"+result.iterator().next().getMiddle()+")";
			} else {
				String binding = "";
				SPARQL += "{";
				boolean first2 = true;
				for(Triple<String,Integer,String> s : result) {
					if(s.getMiddle() != null)
						binding = "BIND (\""+s.getRight()+"\" AS ?v"+s.getMiddle()+")";
					if (first2) first2 = false;
					else SPARQL += " UNION ";
					SPARQL += "{"+s.getLeft()+" "+binding+" }";
				}
				SPARQL += " }\n";
			}
		}
		return SPARQL+"}";
	}
	
	/**
	 * The rule is invalid if there is a variable in the consequent not defined in the antecedent
	 * @return true if the rule is valid. False otherwise.
	 */
	public boolean validateRule() {
		Set<Integer> varInAntecedent = new HashSet<Integer>();
		Set<Integer> varInConsequent = new HashSet<Integer>();
		for(ExpansionPredicate ep: antecedent) {
			varInAntecedent.addAll(ep.getVariableSet());
		}
		for(ExpansionPredicate ep: consequent) {
			varInConsequent.addAll(ep.getVariableSet());
		}
		varInConsequent.removeAll(varInAntecedent);
		return varInConsequent.size() == 0;
	}
	
	public String prettyToString() {
		String s = "RULE:\n";
		for(ExpansionPredicate ep : consequent) {
			s += ep.prettyToString()+" ";
		}
		s += "<== ";
		boolean first = true;
		for(ExpansionPredicate ep : antecedent) {
			if (first) first = false;
			else s += ", ";
			s += ep.prettyToString();
		}
		return s;
	}
	
	
	public Set<PredicateSemiInstantiation> applyRule(QuerySolution binding){
		Set<PredicateSemiInstantiation> semiInst = new HashSet<PredicateSemiInstantiation>();
		for(ExpansionPredicate ep: consequent) {
			semiInst.add(ep.applyBinding(binding));
		}
		
		
		//if(newpredicates != null) return newpredicates;
		//else return null;
		return semiInst;
	}
	
	public Set<PredicateSemiInstantiation> applyRule2(){
		Set<PredicateSemiInstantiation> semiInst = new HashSet<PredicateSemiInstantiation>();
		//if(newpredicates != null) return newpredicates;
		//else return null;
		return semiInst;
	}

}
