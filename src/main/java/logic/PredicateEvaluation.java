package logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

	public class PredicateEvaluation {
	
		public static void evaluate(ExternalDB eDB, Set<PredicateInstantiation> predicates) {
			for (PredicateInstantiation p: predicates) {
				evaluate(eDB, p);
			}
		}
		
	public static void evaluate(ExternalDB eDB, PredicateInstantiation predicate) {
	
		//System.out.println(predicate.hasVariables()+" >> "+predicate);
		//if(!predicate.hasVariables()) return;
		String SPARQLquery = RDFUtil.getSPARQLprefixes(eDB) + "SELECT * WHERE {" + predicate.toSPARQL() + "}";
		TupleQueryResult result = eDB.query(SPARQLquery);
		//System.out.println(predicate.getPredicate().getName());
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			String resultString = "";
	    	for (TextTemplate tt: predicate.getPredicate().getTextLabel()) {
	    		if (tt.isText()) {
	    			resultString += tt.getText()+" ";
	    		} else {
	    			Binding b = predicate.getBindings()[tt.getVar()];
	    			if(b.isConstant()) {
	    				resultString += RDFUtil.resolveLabelOfURI(b.getConstant().getLexicalValue())+" ";
	    			} else {    				
	    				Value v = bindingSet.getBinding("v"+b.getVar()).getValue(); 
	    				resultString += RDFUtil.resolveLabelOfURI(v.stringValue())+" ";
	    			}
	    			
	    		}
	    	}
	    	System.out.println(resultString);
			
			
	        /*BindingSet bindingSet = result.next();
	        for(String name : bindingSet.getBindingNames()) {
	        	Value v = bindingSet.getBinding(name).getValue();
	        	System.out.println(name+": "+v.stringValue());
	        }
	        System.out.println("");*/
	    }
	    result.close();
	    
		}
	
	public static void computeRuleClosure(ExternalDB eDB, Set<Rule> rules, Set<Predicate> knownPredicates) {
		System.out.println("Start) Triples in DB: "+eDB.countTriples());
		int iteration = 1;
		boolean terminationReached = false;
		while (!terminationReached) {
			int triples = eDB.countTriples();
			for(Rule r : rules) {
				if(!r.createsNewPredicate()) applyRule(eDB, r, knownPredicates);
			}			
			if(eDB.countTriples() == triples) terminationReached = true;
			System.out.println(iteration++ + ") Triples in DB: "+eDB.countTriples());
		}
	    
	}
	
	
	public static void applyRule(ExternalDB eDB, Rule rule, Set<Predicate> knownPredicates) {
		if(rule.getConsequent().iterator().next().getName().iterator().next().getText().equals("hasLocation")) {
			System.out.println(rule);
		}
		if(rule.createsNewPredicate()) throw new RuntimeException("Error, cannot apply rule to dataset because it is a predicate creation rule: \n"+rule.toString());
		String SPARQLquery = RDFUtil.getSPARQLdefaultPrefixes()+rule.getAntecedentSPARQL();
		TupleQueryResult result = eDB.query(SPARQLquery);
		//System.out.println(predicate.getPredicate().getName());
		while (result.hasNext()) {
			Set<PredicateInstantiation> pis =  new HashSet<PredicateInstantiation>();
			BindingSet bindingSet = result.next();
			Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
			for(String var :  bindingSet.getBindingNames()) {
    			Value value = bindingSet.getValue(var);
    			//if(value.getClass() == BNode.class) value = null;
    			bindingsMap.put(var, RDFUtil.asJenaNode(value));
    		}
			
			for(PredicateTemplate pt: rule.getConsequent()) {
				//Predicate predicate = PredicateUtil.get(predicateName, bindings.length, predicates);
				pis.add(pt.applyRule(bindingsMap, knownPredicates, null, null, null));
			}
			for(PredicateInstantiation pi : pis) {
				eDB.insertFullyInstantiatedPredicate(pi);
			}
	    }
	}
}
