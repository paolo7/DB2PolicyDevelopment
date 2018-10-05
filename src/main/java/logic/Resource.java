package logic;

import org.apache.jena.datatypes.RDFDatatype;

public interface Resource {

	public String getLexicalValue();
	
	public boolean isLiteral();
	
	public boolean isURI();
}
