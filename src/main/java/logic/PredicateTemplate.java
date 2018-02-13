package logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public interface PredicateTemplate {

	public List<TextTemplate> getName();

	public Binding[] getBindings();
	
	public PredicateInstantiation applyRule(Map<String, RDFNode> bindingsMap, Set<Predicate> predicates, List<TextTemplate> label, Set<PredicateInstantiation> antecedent);
	 
}
