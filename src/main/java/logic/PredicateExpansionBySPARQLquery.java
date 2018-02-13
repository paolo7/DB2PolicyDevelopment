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
		if(debugPrint) System.out.println("*************** Expansion Iteration");
		if(debugPrint) System.out.println("***************   Num. of predicates "+knownPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of existing predicates "+existingPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of rules "+rules.size()+"\n");
		
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		Model m = RDFUtil.generateModel(existingPredicates,RDFprefixes);
		for(Rule r: rules) {
			String SPARQLquery = RDFUtil.getSPARQLprefixes(m)+r.getAntecedentSPARQL();
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, m);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	//if(debugPrint) System.out.println("APPLYING RULE "+r);
				QuerySolution binding = rs.nextSolution();
				Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
				for(Iterator<String> i = binding.varNames(); i.hasNext();) {
					String var =  i.next();
					RDFNode value = binding.get(var);
					if(value.isResource() && value.isAnon()) value = null;
					if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.bnodeProxy))
						value = null;
					bindingsMap.put(var, value);
				}
				newPredicates.addAll(r.applyRule(bindingsMap, knownPredicates));
				for(PredicateInstantiation pi: newPredicates) {
					for(PredicateInstantiation pi2: newPredicates) {
						boolean thesame = pi.equals(pi2);
						int hash = pi.hashCode();
						int hash2 = pi2.hashCode();
						boolean sameHash = hash == hash2;
						thesame = pi.equals(pi2);
					}
				}
				//Resource subj = (Resource) binding.get("Subject");
			    //System.out.println("Subject: "+subj.getURI());
			}
			//System.out.println(r);
			//System.out.println(SPARQLquery);
			}
		boolean oneNewPredicate = false;
		// update the list of known predicates
		for(PredicateInstantiation pi: newPredicates) {
			oneNewPredicate = oneNewPredicate || knownPredicates.add(pi.getPredicate());
			oneNewPredicate = oneNewPredicate || !existingPredicates.contains(pi);
		}
		if(oneNewPredicate) {
			Set<PredicateInstantiation> newKnownPredicates = new HashSet<PredicateInstantiation>();
			newKnownPredicates.addAll(existingPredicates);
			newKnownPredicates.addAll(newPredicates);
			newPredicates.addAll(expand(newKnownPredicates));
		} 
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
