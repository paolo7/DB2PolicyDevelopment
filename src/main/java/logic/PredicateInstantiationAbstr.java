package logic;

import java.util.Arrays;
import java.util.Objects;

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
		return s+")";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + Arrays.hashCode(this.getBindings());
		result = prime * result + ((this.getPredicate() == null) ? 0 : this.getPredicate().hashCode());
		return result;
	}
}
