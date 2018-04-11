package logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
	
	private String expandVariable(int var, Map<Integer,Integer> varsExpansion) {
		if(!varsExpansion.containsKey(new Integer(var))) {
			varsExpansion.put(new Integer(var), new Integer(0));
		}
		int varRepetitions = varsExpansion.get(new Integer(var));
		String expandedVar = ""+var;
		for(int i = 0; i <= varRepetitions ; i++) {
			expandedVar += "i";
		}
		varsExpansion.put(new Integer(var), new Integer(varRepetitions+1));
		return expandedVar;
	}
	
	@Override
	public String toSPARQL(Binding[] bindings, Map<Integer,Integer> varsExpansion) {
		boolean subjectIsConstant = this.getSubject().isConstant() || bindings[this.getSubject().getVar()].isConstant();
		boolean objectIsConstant = this.getObject().isConstant() || bindings[this.getObject().getVar()].isConstant();
		String snippet = toSPARQLhelper(bindings, varsExpansion, false, false);
		if(subjectIsConstant || objectIsConstant) {
			snippet = "{ {"+snippet+"}";
			if(subjectIsConstant) snippet += " UNION {"+toSPARQLhelper(bindings, varsExpansion, true, false)+"}";
			if(objectIsConstant) snippet += " UNION {"+toSPARQLhelper(bindings, varsExpansion, false, true)+"}";
			snippet += " }";
		} 
		return snippet;
		
	}
	
	private String toSPARQLhelper(Binding[] bindings, Map<Integer,Integer> varsExpansion, boolean subjectWildcard, boolean objectWildcard) {
		String snippet = "";
		if(subjectWildcard) {
			snippet += "<"+RDFUtil.bnodeProxy+">";
		} else {			
			if(this.getSubject().isConstant()) snippet += this.getSubject().getConstant().getLexicalValue();
			else {
				Binding b = bindings[this.getSubject().getVar()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
				else snippet += "?v"+expandVariable(b.getVar(), varsExpansion);
			}
		}
		snippet += " ";
		if(this.getPredicate().isConstant()) snippet += this.getPredicate().getConstant().getLexicalValue();
		else {
			Binding b = bindings[this.getPredicate().getVar()];
			if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
			else snippet += "?v"+expandVariable(b.getVar(), varsExpansion);
		}
		snippet += " ";
		if(objectWildcard) {
			snippet += "<"+RDFUtil.bnodeProxy+">";
		} else {			
			if(this.getObject().isConstant()) snippet += this.getObject().getConstant().getLexicalValue();
			else {
				Binding b = bindings[this.getObject().getVar()];
				if(b.isConstant()) snippet += b.getConstant().getLexicalValue();
				else snippet += "?v"+expandVariable(b.getVar(), varsExpansion);
			}
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
