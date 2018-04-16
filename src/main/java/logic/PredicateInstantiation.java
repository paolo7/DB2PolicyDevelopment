package logic;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

/**
 * Each implementation should reuse the implementation of equals specified by PredicateInstantiationAbstr.
 * @author paolo
 *
 */
public interface PredicateInstantiation {

	public Predicate getPredicate();
	public Binding[] getBindings();
	public Binding getBinding(int index);
	public String toSPARQL();
	public String toSPARQL(Map<Integer,Integer> varsExpansion);
	public String toGPPGSPARQL();
	public Set<ConversionTriple> applyBinding(Map<String, RDFNode> bindingsMap, Binding[] newSignatureBindings);
	public Set<ConversionTriple> getAdditionalConstraints();
	public Set<Integer> getNoLitVariables();
}
