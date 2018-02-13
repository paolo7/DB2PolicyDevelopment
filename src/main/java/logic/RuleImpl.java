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



/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RuleImpl other = (RuleImpl) obj;
		if (antecedent == null) {
			if (other.antecedent != null)
				return false;
		} else if (!antecedent.equals(other.antecedent))
			return false;
		if (consequent == null) {
			if (other.consequent != null)
				return false;
		} else if (!consequent.equals(other.consequent))
			return false;
		if (createsNewPredicate != other.createsNewPredicate)
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}*/

	
}
