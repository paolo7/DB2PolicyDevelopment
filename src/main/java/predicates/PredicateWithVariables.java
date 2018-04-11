package predicates;

public class PredicateWithVariables {

	public Predicate inferredPredicate;
	public int[] variables;
	
	/** 
	 * A predicate with variables is a predicate that has a variable ID (identified with an integer) in
	 * each of its variable slots.
	 * @param predicate
	 * @param variables array of variable IDs, must contain the exact number of variables that the predicates uses
	 */
	public PredicateWithVariables(Predicate predicate, int[] variables) {
		if(predicate.varnum != variables.length) 
			throw new RuntimeException("ERROR, trying to set a predicate with a different number of variables than its correct number");
		for(int i = 0; i < variables.length; i++)
			if(variables[i] < 0)
				throw new RuntimeException("ERROR, trying to set a variable as a negative number");
		this.inferredPredicate = predicate;
		this.variables = variables;
	}
}
