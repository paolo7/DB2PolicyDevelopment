package logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public abstract class PredicateInstantiationAbstr implements PredicateInstantiation {

	@Override
	public String toSPARQL() {
		String snippet = "";
		for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			snippet += ct.toSPARQL(this.getBindings())+" .\n";
		}
		return snippet;
	}
	
	@Override
	public String toSPARQL(Map<Integer,Integer> varsExpansion) {
		String snippet = "";
		for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			snippet += ct.toSPARQL(this.getBindings(), varsExpansion)+" .\n";
		}
		return snippet;
	}
	
	@Override
	public Set<Integer> getNoLitVariables(){
		Set<Integer> noLitVars = new HashSet<Integer>();
		for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			noLitVars.addAll(ct.getNoLitVariables(this.getBindings()));
		}
		return noLitVars;
	}
	
	@Override
	public String toGPPGSPARQL() {
		String snippet = "";
		for(ConversionTriple ct: this.getPredicate().getRDFtranslation()) {
			snippet += ct.toGPPGSPARQL(this.getBindings())+" \n";
		}
		return snippet;
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof PredicateInstantiation)) {
            return false;
        }
        PredicateInstantiation p = (PredicateInstantiation) o;
        
        if(! this.getPredicate().equals(p.getPredicate())) return false;
        for(int i = 0; i < this.getBindings().length; i++) {
        	if(! this.getBinding(i).equals(p.getBinding(i))) return false;
        }
        if(this.getAdditionalConstraints().size() != p.getAdditionalConstraints().size())
        	return false;
        if(!this.getAdditionalConstraints().equals(p.getAdditionalConstraints()))
        	return false;
        return true;
    }
	
	@Override
	public String toString() {
		String s = this.getPredicate().getName()+"(";
		boolean first = true;
		for(int i = 0; i < this.getPredicate().getVarnum(); i++) {
			if(first) first = false;
			else s += ", ";
			s += this.getBindings()[i];
		}
		s += ")";
		if(!getAdditionalConstraints().isEmpty()) {
			s += " {";
			for(ConversionTriple ct : getAdditionalConstraints())
				s += "["+ct+"]";
			s += "}";
		}
		return s;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + Arrays.hashCode(this.getBindings());
		result = prime * result + ((this.getPredicate() == null) ? 0 : this.getPredicate().hashCode());
		return result;
	}
	
	@Override
	public boolean compatible(PredicateInstantiation other, Map<String, RDFNode> bindingsMap) {
		if(!getPredicate().equals(other.getPredicate())) return false;
		for(int i = 0; i < getBindings().length; i++) {
			Binding boundValue = getBindings()[i];
			if(boundValue.isVar() && bindingsMap.get("v"+boundValue.getVar()) != null && bindingsMap.get("v"+boundValue.getVar()).isURIResource()) 
				boundValue = new BindingImpl(new ResourceURI(bindingsMap.get("v"+boundValue.getVar()).asResource().getURI()));
			else if(boundValue.isVar() && bindingsMap.get("v"+boundValue.getVar()) != null && bindingsMap.get("v"+boundValue.getVar()).isLiteral()) 
				boundValue = new BindingImpl(new ResourceLiteral(bindingsMap.get("v"+boundValue.getVar()).asLiteral().getLexicalForm()));
			if(boundValue.isConstant() && other.getBindings()[i].isConstant() && ! boundValue.equals(other.getBindings()[i]))
				return false;
		}
		return true;
	}
}
