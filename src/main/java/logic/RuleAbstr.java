package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

public abstract class RuleAbstr implements Rule{

	@Override
	public String getAntecedentSPARQL() {
		String SPARQL = "SELECT ";
		
		// get the SELECT variables
		
		Set<Integer> selectVars = new HashSet<Integer>();
		for(PredicateTemplate ep : this.getConsequent()) {
			for(int i = 0; i < ep.getBindings().length; i++) {
				if(ep.getBindings()[i].isVar())
					selectVars.add(new Integer(ep.getBindings()[i].getVar()));
			}
			for(TextTemplate tt : ep.getName()) {
				if(tt.isVar()) selectVars.add(new Integer(tt.getVar()));
			}
		}
		boolean first = true;
		for(Integer i: selectVars) {
			if (first) first = false;
			else SPARQL += " ";
			SPARQL += "?v"+i;
		}
		SPARQL += "\nWHERE {\n";
		//  compute the WHERE clause
		for(PredicateInstantiation ep : this.getAntecedent()) {
			SPARQL += ep.toSPARQL();
		}
		return SPARQL+"}";
	}
	
	@Override
	public Set<Integer> getNoLitVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		for(PredicateInstantiation ep : this.getAntecedent()) {
			noLitVars.addAll(ep.getNoLitVariables());
		}
		return noLitVars;
	}
	
	@Override
	public String getGPPGAntecedentSPARQL() {
		String SPARQL = "SELECT * WHERE {\n";
		for(PredicateInstantiation ep : this.getAntecedent()) {
			SPARQL += ep.toGPPGSPARQL();
		}
		return SPARQL + "}";
	}
	
	@Override
	public String getExpandedAntecedentSPARQL() {
		
		
		// get the SELECT variables
		Set<Integer> selectVars = new HashSet<Integer>();
		Map<Integer,Integer> varsExpansion = new HashMap<Integer,Integer>();
		for(PredicateTemplate ep : this.getConsequent()) {
			for(int i = 0; i < ep.getBindings().length; i++) {
				if(ep.getBindings()[i].isVar())
					selectVars.add(new Integer(ep.getBindings()[i].getVar()));
					//varsExpansion.put(new Integer(ep.getBindings()[i].getVar()), new Integer(1));
			}
			for(TextTemplate tt : ep.getName()) {
				if(tt.isVar()) {
					selectVars.add(new Integer(tt.getVar()));
					//varsExpansion.put(new Integer(tt.getVar()), new Integer(1));
				}
			}
		}
		String SPARQL = "\nWHERE {\n";
		//  compute the WHERE clause
		for(PredicateInstantiation ep : this.getAntecedent()) {
			SPARQL += ep.toSPARQL(varsExpansion);
		}
		
		// Expand the SELECT variables
		String SPARQLvariables = "SELECT ";
		boolean first = true;
		for(Integer i: varsExpansion.keySet()) {
			for(int j = 1; j <= varsExpansion.get(new Integer(i)); j++) {				
				if (first) first = false;
				else SPARQLvariables += " ";
				SPARQLvariables += "?v"+i;
				for(int k = 1; k <= j; k++) {
					SPARQLvariables += "i";
				}
			}
		}
		return SPARQLvariables+SPARQL+"}";
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Rule)) {
            return false;
        }
        Rule p = (Rule) o;
        return 
        		this.getAntecedent().equals(p.getAntecedent()) && 
        		this.getConsequent().equals(p.getConsequent()) &&
        		this.createsNewPredicate() == p.createsNewPredicate() && 
        		(!this.createsNewPredicate() || this.getLabel().equals(p.getLabel()))
        	;
    }
	
	@Override
	public String toString() {
		String s = "RULE   : ";
		boolean first = true;
		for(PredicateTemplate p : this.getConsequent()) {
			if(first) first = false;
			else s += " AND ";
			s += p;
		}
		s += " <== ";
		first = true;
		for(PredicateInstantiation p : this.getAntecedent()) {
			if(first) first = false;
			else s += " AND ";
			s += p;
		}
		first = true;
		if(this.createsNewPredicate()) {
			s += "\nLABEL  : ";
			for(TextTemplate t : this.getLabel()) {
				if(first) first = false;
				else s += " ";
				s += t;
			}
		}
		
		return s+"\n";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((this.getAntecedent() == null) ? 0 : this.getAntecedent().hashCode());
		result = prime * result + ((this.getConsequent() == null) ? 0 : this.getConsequent().hashCode());
		result = prime * result + (this.createsNewPredicate() ? 1231 : 1237);
		result = prime * result + ((this.getLabel() == null) ? 0 : this.getLabel().hashCode());
		return result;
	}
	
}
