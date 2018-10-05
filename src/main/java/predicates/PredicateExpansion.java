package predicates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import logic.RDFUtil;

public class PredicateExpansion {

	public Set<ExpansionRule> rules;
	
	public PredicateExpansion(Set<ExpansionRule> rules) {
		this.rules = rules;
	}
	
	public Set<PredicateSemiInstantiation> expand(Set<PredicateSemiInstantiation> predicates){
		Set<PredicateSemiInstantiation> newPredicates = new HashSet<PredicateSemiInstantiation> ();
		Model m = RDFinstantiation.generateModel(predicates,true);
		for(ExpansionRule r: rules) {
			String SPARQLquery = RDFUtil.getSPARQLprefixes(m)+r.getAntecedentSPARQL();
			System.out.println(SPARQLquery);
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, m);
		    ResultSet rs = qe.execSelect();
		    //System.out.println(ResultSetFormatter.asText(rs));
		    while (rs.hasNext())
			{
		    	System.out.println("\nAPPLYING RULE "+r.prettyToString()+"\n");
				QuerySolution binding = rs.nextSolution();
				newPredicates.addAll(r.applyRule(binding));
				//Resource subj = (Resource) binding.get("Subject");
			    //System.out.println("Subject: "+subj.getURI());
			}
		}
		return newPredicates;
	}
	
	public Set<PredicateSemiInstantiation> expand2(Set<PredicateSemiInstantiation> predicates){
		Set<PredicateSemiInstantiation> newPredicates = new HashSet<PredicateSemiInstantiation> ();
		for(PredicateSemiInstantiation psi: predicates) {			
			newPredicates.add(psi);
		}
		
		for(ExpansionRule r: rules) {
			Set<ExpansionPredicate> antecendsToCheck = new HashSet<ExpansionPredicate>();
			Set<ExpansionPredicate> consequentsToCheck = new HashSet<ExpansionPredicate>();
			antecendsToCheck.addAll(r.antecedent);
			consequentsToCheck.addAll(r.consequent);
			for(PredicateSemiInstantiation p: predicates) {
				Map<Integer,String> varBinding = new HashMap<Integer,String>();
				boolean found = false;
				Set<ExpansionPredicate> antecendsToRemove = new HashSet<ExpansionPredicate>();
				for(ExpansionPredicate ep: antecendsToCheck) {
					if(!found) {
						if(matches(ep,p)) {
							found = true;
							antecendsToRemove.add(ep);
							//System.out.println("      **** MATCH BETWEEN {"+ep.prettyToString()+"} AND {"+p.prettyToString()+"}");
						}
					}
				}
				if(found) antecendsToCheck.removeAll(antecendsToRemove);
				Set<ExpansionPredicate> consequentsToRemove = new HashSet<ExpansionPredicate>();
				for(ExpansionPredicate ep: consequentsToCheck) {
					if(!found) {
						if(matches(ep,p)) {
							found = true;
							consequentsToRemove.add(ep);
							//System.out.println("      **** MATCH BETWEEN {"+ep.prettyToString()+"} AND {"+p.prettyToString()+"}");
						}
					}
				}
				if(found) consequentsToCheck.removeAll(antecendsToRemove);
			}
			// check whether all the antecedents have been matched, and at least one consequent is not already available
			if(antecendsToCheck.size() == 0 && consequentsToCheck.size() > 0) {
				System.out.println("      **** APPLYING RULE "+r.prettyToString());
				newPredicates.addAll(r.applyRule2());
			}
		}
		
		
		
		// if any new predicates were created, then re-evaluate the expansion
		if(newPredicates.size() > predicates.size())
			newPredicates = expand(newPredicates);
		return newPredicates;
	}
	
	/**
	 * check whether predicate p would match expantion predicate ep
	 * @param ep
	 * @param p
	 * @return
	 */
	public static boolean matches(ExpansionPredicate ep, PredicateSemiInstantiation p) {
		String regex = "";
		for(TextTemplate tt: ep.name) {
			if(tt.text != null) {
				regex += tt.text;
			} else {
				regex += "[a-zA-Z0-9]*";
			}
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(p.basePredicate.name);
		return (matcher.find() && ep.variables.length == p.basePredicate.varnum);
	}
	
	public static Pair<Integer,String> matches(ExpansionPredicate ep, Predicate p) {
		String regex = "";
		Integer variable = -1;
		for(TextTemplate tt: ep.name) {
			if(tt.text != null) {
				regex += tt.text;
			} else {
				variable = tt.var;
				regex += "[a-zA-Z0-9]*";
			}
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(p.name);
		if (matcher.find() && ep.variables.length == p.varnum) {
			return new ImmutablePair<Integer,String>(variable,p.name);
		} else return null;
	}
	
}
