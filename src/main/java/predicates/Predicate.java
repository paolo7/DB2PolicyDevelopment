package predicates;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Predicate {

	public String name;
	public int varnum;
	
	public Set<ConversionTriple> equivalentRDF;
	public Set<List<TextTemplate>> labels;
	
	public Predicate(String name, int varnum) {
		this.name = name;
		this.varnum = varnum;
		labels = new HashSet<List<TextTemplate>>();
		equivalentRDF = new HashSet<ConversionTriple>();
	}
	
	public void setEquivalentRDF(Set<ConversionTriple> equivalentRDF) {		
		this.equivalentRDF = equivalentRDF;
	}
	
	public void setLabels(Set<List<TextTemplate>> labels) {		
		this.labels = labels;
	}
	
	public void addLabel(List<TextTemplate> label) {		
		labels.add(label);
	}
	
	public PredicateSemiInstantiation semiInstantiate(String[] partialInstantiation) {
		PredicateSemiInstantiation instantiation = new PredicateSemiInstantiation(this,partialInstantiation);
		return instantiation;
	}
	
	public Set<ConversionTriple> semiInstantiateEquivalentRDF(String[] partialInstantiation){
		Set<ConversionTriple> newConversionTriples = new HashSet<ConversionTriple>();
		for(ConversionTriple ct: equivalentRDF) {
			newConversionTriples.add(ct.semiInstantiate(partialInstantiation));
		}
		return newConversionTriples;
	}
	
	public String prettyToString() {
		return prettyToString(equivalentRDF,null);
	}
	
	public String toSPARQLquerySnippet(int[] variables) {
		String snippet = "";
		for(ConversionTriple t : equivalentRDF) {
			snippet += t.toSPARQLsnippet(variables)+" . ";
		}
		return snippet;
	}
	
	public String prettyToString(Set<ConversionTriple> equivalentRDF,String[] partialInstantiation) {
		String s = name+"(";
		boolean first = true;
		for(int i = 0; i < varnum; i++) {
			if(first) first = false;
			else s += ",";
			
			if(partialInstantiation != null && partialInstantiation[i] != null) {
				s += ""+partialInstantiation[i]+"";
			} else s += "?v"+i;
		}
		s += ")\n";
		
		s += "RDF EQUIVALNCE:\n";
		if (equivalentRDF != null) {			
			for (ConversionTriple t : equivalentRDF) {			
				s += "  "+t.prettyToString()+"\n";
			}
		}
		int li = 0;
		for (List<TextTemplate> l : labels) {	
			s += ">Label n. "+li+":\n";
			s += "  ";
			for (TextTemplate tt : l) {	
				s += tt.prettyToString()+" ";
			}
			s += "\n";
			li++;
		}
		return s;
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Predicate)) {
            return false;
        }
        Predicate p = (Predicate) o;
         
        // Compare the data members and return accordingly 
        return name == p.name && varnum == p.varnum && equivalentRDF == p.equivalentRDF;
    }
}
