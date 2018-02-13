package logic;

import java.util.Objects;

public abstract class ConversionTripleAbstr implements ConversionTriple{

	@Override
	public String toSPARQL(Binding[] bindings) {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
		else {
			Binding b = bindings[this.getSubject().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValue();
		else {
			Binding b = bindings[this.getPredicate().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
		else {
			Binding b = bindings[this.getObject().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		return snippet;
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof ConversionTriple)) {
            return false;
        }
        ConversionTriple p = (ConversionTriple) o;
         
        return this.getSubject().equals(p.getSubject()) && this.getPredicate().equals(p.getPredicate())
        		&& this.getObject().equals(p.getObject());
    }
	
	public String toString() {
		return this.getSubject()+" "+this.getPredicate()+" "+this.getObject();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((this.getObject() == null) ? 0 : this.getObject().hashCode());
		result = prime * result + ((this.getPredicate() == null) ? 0 : this.getPredicate().hashCode());
		result = prime * result + ((this.getSubject() == null) ? 0 : this.getSubject().hashCode());
		return result;
	}
	
}
