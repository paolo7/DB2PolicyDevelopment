package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public class PredicateExpansionBySPARQLquery implements PredicateExpansion{
	
	private Set<Predicate> knownPredicates;
	private Set<Rule> rules;
	
	private Map<String,String> RDFprefixes;
	
	private boolean debugPrint = true;
	
	public PredicateExpansionBySPARQLquery(Set<Predicate> knownPredicates, Set<Rule> rules) {
		this.knownPredicates = knownPredicates;
		this.rules = rules;
	}
	
	public PredicateExpansionBySPARQLquery() {
		knownPredicates = new HashSet<Predicate>();
		rules = new HashSet<Rule>();
	}

	@Override
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates,
			Set<Predicate> knownPredicates, Set<Rule> rules) {
		PredicateExpansionBySPARQLquery expansion = new PredicateExpansionBySPARQLquery(knownPredicates,rules);
		Set<PredicateInstantiation> expandedPredicates = expansion.expand(existingPredicates);
		return expandedPredicates;
	}

	@Override
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates) {
		return expand(existingPredicates,true);
	}
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates, boolean varExpansion) {
		if(debugPrint) System.out.println("*************** Expansion Iteration");
		if(debugPrint) System.out.println("***************   Num. of predicates "+knownPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of existing predicates "+existingPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of rules "+rules.size()+"\n");
		
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		Model m = RDFUtil.generateModel(existingPredicates,RDFprefixes);
		for(Rule r: rules) {
			String SPARQLquery;
			if(!varExpansion) SPARQLquery = RDFUtil.getSPARQLprefixes(m)+r.getAntecedentSPARQL();
			else SPARQLquery = RDFUtil.getSPARQLprefixes(m)+r.getExpandedAntecedentSPARQL();
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, m);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	//if(debugPrint) System.out.println("APPLYING RULE "+r);
				QuerySolution binding = rs.nextSolution();
				Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
				boolean validBinding = true;
				if(varExpansion) {
					for(Iterator<String> i = binding.varNames(); i.hasNext();) {
						// First get the value of the expanded variable
						String var = i.next();
						RDFNode value = binding.get(var);
						// Then de-expand the variable to get the actual variable name
						var =  var.replace("i", "");
						if(value.isResource() && value.isAnon()) value = null;
						else if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.bnodeProxy))
							value = null;
						if(bindingsMap.containsKey(var)) {
							// if the same binding is mapped to more than one resource, 
							// we need to check if they are compatible, and if they are
							// we need to take the intersection of their bindings
							RDFNode previousValue = bindingsMap.get(var);
							
							if(previousValue == null && value == null) {
								// if they are both bound to any entity, then they are the
								// same and there is nothing to do here
							} else if(previousValue == null && value != null) {
								// if the previous value was any entity, and the new value
								// to a constant, then we must restrict the binding to this
								// constant only 
								bindingsMap.put(var, value);
							} else if(previousValue != null && value == null) {
								// if the previous value was a constrant, and the new value
								// can be any entity, then we keep the previous stricter
								// binding to the constant
							} else if(previousValue.equals(value) ) {
								// if they are both bound to the same constant, then they
								// are the same and there is nothing to do here
							} else if(!previousValue.equals(value) ) {
								// if they are both bound to a constant, but not the same
								// one, then this is not a legal binding as we can't 
								// force these two constants to being equal
								validBinding = false;
							} else {
								throw new RuntimeException("ERROR: internal problem during var epansion, this line should never be reached");
							}
							
						} else {							
							bindingsMap.put(var, value);
						}
					}
				}
				else for(Iterator<String> i = binding.varNames(); i.hasNext();) {
					String var =  i.next();
					RDFNode value = binding.get(var);
					if(value.isResource() && value.isAnon()) value = null;
					else if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.bnodeProxy))
						value = null;
					bindingsMap.put(var, value);
				}
				if(validBinding)
					newPredicates.addAll(r.applyRule(bindingsMap, knownPredicates));
				/*for(PredicateInstantiation pi: newPredicates) {
					for(PredicateInstantiation pi2: newPredicates) {
						boolean thesame = pi.equals(pi2);
						int hash = pi.hashCode();
						int hash2 = pi2.hashCode();
						boolean sameHash = hash == hash2;
						thesame = pi.equals(pi2);
					}
				}*/
			}

			}
		newPredicates.removeAll(existingPredicates);
		if(newPredicates.size() == 0) return newPredicates;
		Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
		newKnownPredicates.addAll(existingPredicates);
		newKnownPredicates.addAll(newPredicates);
		newPredicates.addAll(expand(newKnownPredicates));
		return newPredicates;
	}

	@Override
	public Set<Rule> getRules() {
		return rules;
	}

	@Override
	public Set<Predicate> getPredicates() {
		return knownPredicates;
	}

	@Override
	public void setRules(Set<Rule> rules) {
		this.rules = rules;
	}

	@Override
	public void setPredicates(Set<Predicate> predicates) {
		this.knownPredicates = predicates;
	}

	@Override
	public void setPrefixes(Map<String, String> map) {
		this.RDFprefixes = map;
		
	}

}
