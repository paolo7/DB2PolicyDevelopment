package logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public interface Rule {

	public Set<PredicateInstantiation> getAntecedent();
	public Set<PredicateTemplate> getConsequent();
	public boolean createsNewPredicate();
	public List<TextTemplate> getLabel();
	//to remove?
	public String getAntecedentSPARQL();
	//to remove
	public String getExpandedAntecedentSPARQL();
	public String getGPPGAntecedentSPARQL();
	public Set<PredicateInstantiation> applyRule(Map<String,RDFNode> bindingsMap, Set<Predicate> predicates, Set<PredicateInstantiation> existingPredicates);
	public Set<Integer> getNoLitVariables();
}
