package logic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public class RuleImpl extends RuleAbstr{
	
	Set<PredicateInstantiation> antecedent;
	Set<PredicateTemplate> consequent;
	boolean createsNewPredicate;
	List<TextTemplate> label;
	
	public RuleImpl(Set<PredicateInstantiation> antecedent, Set<PredicateTemplate> consequent) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.label = null;
		createsNewPredicate = false;
	}
	
	public RuleImpl(Set<PredicateInstantiation> antecedent, Set<PredicateTemplate> consequent, List<TextTemplate> label) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.label = label;
		createsNewPredicate = true;
	}

	@Override
	public Set<PredicateInstantiation> getAntecedent() {
		return antecedent;
	}

	@Override
	public Set<PredicateTemplate> getConsequent() {
		return consequent;
	}

	@Override
	public boolean createsNewPredicate() {
		return createsNewPredicate;
	}

	@Override
	public List<TextTemplate> getLabel() {
		return label;
	}

	@Override
	public Set<PredicateInstantiation> applyRule(Map<String, RDFNode> bindingsMap, Set<Predicate> predicates) {
		Set<PredicateInstantiation> newpredicates = new HashSet<PredicateInstantiation>();
		for(PredicateTemplate pt: consequent) {
			newpredicates.add(pt.applyRule(bindingsMap, predicates, label, antecedent));
		}
		return newpredicates;
	}

	
}
