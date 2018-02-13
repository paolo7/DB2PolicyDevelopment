package logic;

import java.util.Set;

public class PredicateUtil {

	/**
	 * If predicates contain a Predicate with signature predicateName and varNum variables, return it. Otherwise throw a runtime exception.
	 * @param predicateName
	 * @param varNum
	 * @param predicates
	 * @return
	 */
	public static Predicate get(String predicateName, int varNum, Set<Predicate> predicates) {
		Predicate predicate = null;
		for(Predicate p : predicates) {
			if(p.getName().equals(predicateName) && p.getVarnum() == varNum) {
				if(predicate == null)
					predicate = p;
				else
					throw new RuntimeException("ERROR: the set of predicates contain more than one entry with predicate name "+predicateName+" and "+varNum+" variables");
			}
		}
		if(predicate != null)
			return predicate;
		else
			throw new RuntimeException("ERROR: the set of predicates does not contain an entry with predicate name "+predicateName+" and "+varNum+" variables");
	}
	
	/**
	 * 
	 * @param predicateName
	 * @param varNum
	 * @param predicates
	 * @return true if predicates contain a single Predicate with signature predicateName and varNum variables. Else return false.
	 */
	public static boolean containsOne(String predicateName, int varNum, Set<Predicate> predicates) {
		Predicate predicate = null;
		for(Predicate p : predicates) {
			if(p.getName().equals(predicateName) && p.getVarnum() == varNum) {
				if(predicate == null)
					predicate = p;
				else
					return false;
			}
		}
		if(predicate != null)
			return true;
		else
			return false;
	}
}
