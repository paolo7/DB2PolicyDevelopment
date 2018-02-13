package logic;

import java.util.Map;
import java.util.Set;

public interface PredicateExpansion {

	/**
	 * This method returns the predicates can be inferred from existingPredicates given a set of known
	 * predicates knownPredicates and the ruleset rules
	 * @param existingPredicates
	 * @param knownPredicates
	 * @param rules
	 * @return
	 */
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates, Set<Predicate> knownPredicates, Set<Rule> rules);

	/**
	 * This method returns the predicates that can be inferred from existingPredicates given the
	 * internal set of knownPredicates and rules that can be obtained, respectively, by getPredicates()
	 * and getRules(). 
	 * If new predicates are created, they will be added to the set of known predicates.
	 * @param existingPredicates
	 * @return
	 */
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates);
	
	public Set<Rule> getRules();
	
	public Set<Predicate> getPredicates();
	
	public void setRules(Set<Rule> rules);
	
	public void setPredicates(Set<Predicate> predicate);
	
	public void setPrefixes(Map<String,String> map);
}
