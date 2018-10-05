package predicates;

public class ConversionTriple {

	public String subject;
	public String predicate;
	public String object;
	
	public int subjectvar;
	public int predicatevar;
	public int objectvar;
	
	/**
	 * Creates a template of a triple generated with strings subject, predicate, object.
	 * If any of these strings is null, its value should be substituted with the variable from 
	 * a list of variables at index subjectvar, objectvar, or predicatevar, which should be greater
	 * than 0. If not, the corresponding variable should be set to -1.
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param subjectvar
	 * @param objectvar
	 * @param predicatevar
	 */
	public ConversionTriple(String subject, String predicate, String object,
			int subjectvar, int predicatevar, int objectvar) {
		this.subject = subject;
		this.object = object;
		this.predicate = predicate;
		this.subjectvar = subjectvar;
		this.objectvar = objectvar;
		this.predicatevar = predicatevar;
		
		String errorstring = "ERROR, a triple is being created with both a string value and a variable value as its ";
		String errorstring2 = "ERROR, a triple is being created with neither a string value nor a variable value as its ";
		if (this.subject != null && subjectvar != -1) throw new RuntimeException(errorstring+"subject");
		if (this.predicate != null && predicatevar != -1) throw new RuntimeException(errorstring+"predicate");
		if (this.object != null && objectvar != -1) throw new RuntimeException(errorstring+"object");
		if (this.subject == null && subjectvar < 0) throw new RuntimeException(errorstring2+"subject");
		if (this.predicate == null && predicatevar < 0) throw new RuntimeException(errorstring2+"predicate");
		if (this.object == null && objectvar < 0) throw new RuntimeException(errorstring2+"object");
	}
	
	public String toSPARQLsnippet(int[] vars) {
		String s = "";
		if(subject != null) s += subject+" ";
		else s += "?v"+vars[subjectvar]+" ";
		if(predicate != null) s += predicate+" ";
		else s += "?v"+vars[predicatevar]+" ";
		if(object != null) s += object;
		else s += "?v"+vars[objectvar];
		return s+"";
	}
	
	public String prettyToString() {
		String s = "";
		if(subject != null) s += subject+" ";
		else s += "?v"+subjectvar+" ";
		if(predicate != null) s += predicate+" ";
		else s += "?v"+predicatevar+" ";
		if(object != null) s += object+" ";
		else s += "?v"+objectvar+" ";
		return s+".";
	}
	
	public ConversionTriple semiInstantiate(String[] partialInstantiation){
		String subject = this.subject;
		String predicate = this.predicate;
		String object = this.object;
		int subjectvar = this.subjectvar;
		int predicatevar = this.predicatevar;
		int objectvar = this.objectvar;
		if(subjectvar >= 0 && subjectvar < partialInstantiation.length && partialInstantiation[subjectvar] != null){
			subject = partialInstantiation[subjectvar];
			subjectvar = -1;
		}
		if(objectvar >= 0 && objectvar < partialInstantiation.length && partialInstantiation[objectvar] != null){
			object = partialInstantiation[objectvar];
			objectvar = -1;
		}
		if(predicatevar >= 0 && predicatevar < partialInstantiation.length && partialInstantiation[predicatevar] != null){
			predicate = partialInstantiation[predicatevar];
			predicatevar = -1;
		}
		return new ConversionTriple(subject, predicate, object,	subjectvar, predicatevar, objectvar);
	}
	
	@Override
    public boolean equals(Object o) {
  
        if (o == this) {
            return true;
        }
        if (!(o instanceof ConversionTriple)) {
            return false;
        }
        ConversionTriple t = (ConversionTriple) o;
         
        // Compare the data members and return accordingly 
        return subject == t.subject && object == t.object && predicate == t.predicate &&
        		subjectvar == t.subjectvar && objectvar == t.objectvar && predicatevar == t.predicatevar ;
    }
}
