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
	public String getAntecedentSPARQL();
	public String getExpandedAntecedentSPARQL();
	public Set<PredicateInstantiation> applyRule(Map<String,RDFNode> bindingsMap, Set<Predicate> predicates);
}
