package logic;

import java.util.List;
import java.util.Objects;

public abstract class PredicateAbstr implements Predicate{

	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof Predicate)) {
            return false;
        }
        Predicate p = (Predicate) o;
        
        if(this.getRDFtranslation().size() != p.getRDFtranslation().size())
        	return false;
        
        if(this.getTextLabel().size() != p.getTextLabel().size())
        	return false;
        
        if(! this.getRDFtranslation().equals(p.getRDFtranslation()))
        	return false;
        
        for(int i = 0; i < this.getTextLabel().size(); i++) {
        	if(! this.getTextLabel().get(i).equals(p.getTextLabel().get(i)))
        		return false;
        }
         
        return this.getName().equals(p.getName()) && this.getVarnum() == p.getVarnum();
    }
	
	@Override
	public String toString() {
		// signature
		String s = "PREDICATE: "+this.getName()+"(";
		boolean first = true;
		for(int i = 0; i < this.getVarnum(); i++) {
			if(first) first = false;
			else s += ", ";
			s += "?v"+i;
		}
		s += ")\n";
		// label
		s += "LABEL    : ";
		for (TextTemplate tt : this.getTextLabel()) {	
				s += tt+" ";
		}
		s += "\n";
		// rdf conversion
		s += "RDF      : ";
		for (ConversionTriple tt : this.getRDFtranslation()) {	
			s += tt+" . \n           ";
		}
		return s;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
		result = prime * result + ((this.getTextLabel() == null) ? 0 : this.getTextLabel().hashCode());
		result = prime * result + ((this.getRDFtranslation() == null) ? 0 : this.getRDFtranslation().hashCode());
		result = prime * result + this.getVarnum();
		return result;
	}
}
