package logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public abstract class ConversionTripleAbstr implements ConversionTriple{

	
	@Override
	public Set<Integer> getNoLitVariables(Binding[] bindings){
		Set<Integer> noLitVars = new HashSet<Integer>();
		if(this.getSubject().isVar()) {
			Binding b = bindings[this.getSubject().getVar()];
			if(b.isVar()) noLitVars.add(new Integer(b.getVar()));
		}
		if(this.getPredicate().isVar()) {
			Binding b = bindings[this.getPredicate().getVar()];
			if(b.isVar()) noLitVars.add(new Integer(b.getVar()));
		}
		return noLitVars;
	}
	
	@Override
	public String toGPPGSPARQL(Binding[] bindings) {
		String snippet = " {";
		snippet += toGPPGSPARQL(bindings,false,false,false);
		snippet += " UNION "+toGPPGSPARQL(bindings,true,false,false);
		snippet += " UNION "+toGPPGSPARQL(bindings,false,true,false);
		snippet += " UNION "+toGPPGSPARQL(bindings,false,false,true);
		snippet += " UNION "+toGPPGSPARQL(bindings,true,true,false);
		snippet += " UNION "+toGPPGSPARQL(bindings,true,false,true);
		snippet += " UNION "+toGPPGSPARQL(bindings,false,true,true);
		snippet += " UNION "+toGPPGSPARQL(bindings,true,true,true);
		return snippet+"}\n";
	}
	
	public String toGPPGSPARQL(Binding[] bindings, boolean lambdaS, boolean lambdaP, boolean lambdaO) {
		String snippet = " {";
		if (lambdaS) snippet += "<"+RDFUtil.LAMBDAURI+">";
		else {
			if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
			else {
				Binding b = bindings[this.getSubject().getVar()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
				else snippet += "?v"+b.getVar();
			}
		}
		snippet += " ";
		if (lambdaP) snippet += "<"+RDFUtil.LAMBDAURI+">";
		else {
			if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValue();
			else {
				Binding b = bindings[this.getPredicate().getVar()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
				else snippet += "?v"+b.getVar();
			}
		}
		snippet += " ";
		if (lambdaO) snippet += "<"+RDFUtil.LAMBDAURI+">";
		else {
			if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
			else {
				Binding b = bindings[this.getObject().getVar()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
				else snippet += "?v"+b.getVar();
			}
		}
		return snippet+"}\n";
	}
	
	
	
	public ConversionTriple applyBinding(Binding[] bindings) {
		return new ConversionTripleImpl(applyBindingHelper(bindings, getSubject()), 
				applyBindingHelper(bindings, getPredicate()), 
				applyBindingHelper(bindings, getObject()));
	}
	public Binding applyBindingHelper(Binding[] bindings, Binding binding) {
		if(binding.isConstant()) return binding;
		if(binding.getVar() < bindings.length) return bindings[binding.getVar()];
		return binding;
	}
	
	
	@Override
	public String toSPARQL(Binding[] bindings, String freshVarPrefix) {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
		else if (this.getSubject().getVar() >= bindings.length) {
			snippet += "?v"+this.getSubject().getVar()+freshVarPrefix;
		} else {
			Binding b = bindings[this.getSubject().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValue();
		else if (this.getPredicate().getVar() >= bindings.length) {
			snippet += "?v"+this.getPredicate().getVar()+freshVarPrefix;
		} else {
			Binding b = bindings[this.getPredicate().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+b.getVar();
		}
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
		else if (this.getObject().getVar() >= bindings.length) {
			snippet += "?v"+this.getObject().getVar()+freshVarPrefix;
		} else {
			Binding b = bindings[this.getObject().getVar()];
			if(b.isConstant()) {
				if(b.getConstant().isLiteral() && RDFUtil.isNumericDatatypeIRI(((ResourceLiteral) b.getConstant()).getLiteralTypeIRI()))
					snippet += b.getConstant().getLexicalValue();
				else if(b.getConstant().isLiteral()) snippet += "\""+b.getConstant().getLexicalValue()+"\"";
				else snippet += b.getConstant().getLexicalValueExpanded();
			}
			else snippet += "?v"+b.getVar();
		}
		return snippet;
	}
	
	@Override
	public String toSPARQL() {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
		else {
			snippet += "?v"+this.getSubject().getVar();
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValue();
		else {
			snippet += "?v"+this.getPredicate().getVar();
		} 
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
		else {
			snippet += "?v"+this.getObject().getVar();
		} 
		return snippet;
	}
	

	
	@Override
	public String toSPARQL_INSERT(Binding[] bindings, String baseNew) {
		String snippet = "";
		if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValueExpanded();
		else if (this.getSubject().getVar() >= bindings.length) {
			snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,this.getSubject().getVar());
		} else {
			Binding b = bindings[this.getSubject().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValueExpanded();
			else snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar());
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValueExpanded();
		else if (this.getPredicate().getVar() >= bindings.length) {
			snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,this.getPredicate().getVar());
		} else {
			Binding b = bindings[this.getPredicate().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValueExpanded();
			else snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar());
		}
		snippet += " ";
		if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValueExpanded();
		else if (this.getObject().getVar() >= bindings.length) {
			snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,this.getObject().getVar());
		} else {
			Binding b = bindings[this.getObject().getVar()];
			if(b.isConstant()) {
				if(b.getConstant().isLiteral() && RDFUtil.isNumericDatatypeIRI(((ResourceLiteral) b.getConstant()).getLiteralTypeIRI()))
					snippet += b.getConstant().getLexicalValue();
				else snippet += b.getConstant().getLexicalValueExpanded();
			}
			else snippet += RDFUtil.getBlankNodeOrNewVarString(baseNew,b.getVar());
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
	
	@Override
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
