package predicates;

import java.util.Set;

public class PredicateSemiInstantiation {
	
	public Predicate basePredicate;
	public String[] partialInstantiation;
	//public Set<ConversionTriple> equivalentRDF;
	
	/**
	 * For each variable with n. x, partialInstantiation[x] gives the specific value for that variable, if set, or null if open.
	 * @param basePredicate
	 * @param partialInstantiation
	 */
	public PredicateSemiInstantiation(Predicate basePredicate, String[] partialInstantiation) {
		this.basePredicate = basePredicate;
		this.partialInstantiation = partialInstantiation;
		//equivalentRDF = basePredicate.semiInstantiateEquivalentRDF(partialInstantiation);
	}
	
	public Set<ConversionTriple> getEquivalentRDF() {
		return basePredicate.semiInstantiateEquivalentRDF(partialInstantiation);
	}
	
	/**
	 * Generates a basic version of the predicate where all variables can are open.
	 * @param basePredicate
	 */
	public PredicateSemiInstantiation(Predicate basePredicate) {
		this.basePredicate = basePredicate;
		this.partialInstantiation = new String[basePredicate.varnum];
	}
	
	public String prettyToString() {
		return basePredicate.prettyToString(getEquivalentRDF(),partialInstantiation);
	}
}
