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
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;

public class PredicateExpansionBySPARQLquery implements PredicateExpansion{
	
	private Set<Predicate> knownPredicates;

	private Set<Rule> rules;
	
	private Map<String,String> RDFprefixes;
	
	private Model additionalVocabularies;
	
	private boolean debugPrint = true;
	private boolean debugPrintOWLconsistencyChecks = false;
	
	private Map<Rule, Set<Map<String,RDFNode>>> inconsistentRuleApplications = new HashMap<Rule, Set<Map<String,RDFNode>>>();
	
	public PredicateExpansionBySPARQLquery(Set<Predicate> knownPredicates, Set<Rule> rules, Model additionalVocabularies) {
		this.knownPredicates = knownPredicates;
		this.rules = rules;
		this.additionalVocabularies = additionalVocabularies;
	}
	
	public PredicateExpansionBySPARQLquery() {
		knownPredicates = new HashSet<Predicate>();
		rules = new HashSet<Rule>();
	}
	
	private int statinconsistencycheck;
	private int statinconsistencycheckfound;
	private int statinconsistencycheckreused;
	private int overallConsistencyChecks;
	private int rulesConsidered;
	private int ruleApplicationConsidered;
	
	public boolean checkOWLconsistency(Rule r, Map<String,RDFNode> bindingsMap, Model baseModel, Set<PredicateInstantiation> inferrablePredicates) {
		overallConsistencyChecks++;
		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
		
		//try {
		//	baseModel.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphXBASEMODEL.ttl")),"Turtle");
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
		
		// TODO this is not compatible yet with owl:DatatypeProperty
		// to fix this we can just turn lambda nodes to blank nodes
		
		Model modelExpanded = RDFUtil.generateRuleInstantiationModel(r,bindingsMap,RDFprefixes, knownPredicates, inferrablePredicates).add(baseModel);
		
		reasoner = reasoner.bindSchema(modelExpanded);
		//try {
		//	modelExpanded.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphExpandedRule.ttl")),"Turtle");
		//		} catch (FileNotFoundException e) {
		//			e.printStackTrace();
		//		}
		
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, modelExpanded);
		ValidityReport validity = infmodel.validate();
		if (validity.isValid()) {
			if(debugPrintOWLconsistencyChecks) System.out.print(".");
		} else {
		}
		return validity.isValid();
	}

	@Override
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates) {
		return expand(existingPredicates,false);
	}
	
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates, boolean consistencyCheck) {
		statinconsistencycheck = 0;
		statinconsistencycheckfound = 0;
		statinconsistencycheckreused = 0;
		overallConsistencyChecks = 0;
		rulesConsidered = 0;
		ruleApplicationConsidered = 0;
		
		if(debugPrint) System.out.println("*************** Expansion Iteration");
		if(debugPrint) System.out.println("***************   Num. of known predicates "+knownPredicates.size()+"");
		if(debugPrint) System.out.println("***************   Num. of rules "+rules.size()+"\n");
		if(debugPrint) System.out.println("***************   Num. of available predicates "+existingPredicates.size()+"");
		
		Set<PredicateInstantiation> newPredicates = new HashSet<PredicateInstantiation>();
		Model basicModel = null;
		if(consistencyCheck) {
			basicModel = RDFUtil.generateBasicModel(existingPredicates,RDFprefixes);
			basicModel.add(additionalVocabularies);
		}
		
		// compute sandbox model for the Graph-Pattern evaluation over a Pattern-Constrained Graph (GPPG) 
		Model sandboxModel = RDFUtil.generateGPPGSandboxModel(existingPredicates,RDFprefixes);
		for(Rule r: rules) {
			rulesConsidered++;
			// Perform GPPG
			// Compute query expansion
			String SPARQLquery = RDFUtil.getSPARQLprefixes(sandboxModel)+r.getGPPGAntecedentSPARQL();
			Set<Integer> varsNoLit = r.getNoLitVariables();
			// Evaluate query over the sandbox graph
			Query query = QueryFactory.create(SPARQLquery) ;
			QueryExecution qe = QueryExecutionFactory.create(query, sandboxModel);
		    ResultSet rs = qe.execSelect();
		    while (rs.hasNext())
			{
		    	/*if(r.getConsequent().iterator().next().getName().get(0).toString().equals("xxaaxx")) {
		    		System.out.println("xxaaxx");
		    	}*/
		    	QuerySolution binding = rs.nextSolution();
		    	// the results of a GPPG evaluation, because of the Duplicate Empty Set assumption, might contain bindings without all the required variables
		    	// these bindings can be ignored as they are semantic duplicates of other bindings that contain all the variables
		    	boolean completeResultSet = true;
		    	for(String var : query.getResultVars()) 
		    		if (!binding.contains(var)) 
		    			completeResultSet = false;
		    	if(completeResultSet) {		
		    		ruleApplicationConsidered++;
		    		Map<String,RDFNode> bindingsMap = new HashMap<String,RDFNode>();
		    		// Perform delta filtering
		    		boolean validBinding = true;
		    		for(Iterator<String> i = binding.varNames(); i.hasNext();) {
		    			String var =  i.next();
		    			RDFNode value = binding.get(var);
		    			if(value.isResource() && value.isAnon()) value = null;
		    			if(value.isLiteral()) {
		    				// remove assignments from variables to literals if such variables are used in subj or pred position in the antecedent
		    				if(varsNoLit.contains(new Integer(var.replaceFirst("v", ""))))
		    					validBinding = false;
		    			}
		    			else if(value.isResource() && (!value.isAnon()) && value.asResource().getURI().equals(RDFUtil.LAMBDAURI))
		    				value = null;
		    			bindingsMap.put(var, value);
		    		}
		    		// just create an entry if it's not there, it contains no maps in the set
		    		if(!inconsistentRuleApplications.containsKey(r))
		    			inconsistentRuleApplications.put(r, new HashSet<Map<String,RDFNode>>());
		    		Set<PredicateInstantiation> inferrablePredicates = null;
		    		if(validBinding) {
		    			if(consistencyCheck) {		    				
		    				if(inconsistentRuleApplications.get(r).contains(bindingsMap)) {
		    					if(debugPrintOWLconsistencyChecks) System.out.print("@");						
		    					validBinding = false;
		    					statinconsistencycheckreused++;
		    				}
		    			}
		    			else {
		    				inferrablePredicates = r.applyRule(bindingsMap, knownPredicates, existingPredicates);
		    				if (consistencyCheck && !checkOWLconsistency(r,bindingsMap, basicModel, inferrablePredicates)) {
		    					validBinding = false;
		    					inconsistentRuleApplications.get(r).add(bindingsMap);
		    					if(debugPrintOWLconsistencyChecks) System.out.print("#");
		    					statinconsistencycheckfound++;
		    					statinconsistencycheck++;
		    				}
		    			}
		    		}
		    		if(validBinding) {	
		    			newPredicates.addAll(inferrablePredicates);
		    		}
		    	}
			}
		}
		newPredicates.removeAll(existingPredicates);
		
		int removed = RDFUtil.filterRedundantPredicates(existingPredicates,newPredicates, false);
		if(debugPrint) 
			System.out.println("Filtered out "+removed+" redundant predicate instantiations.");
		
		for(PredicateInstantiation pi : newPredicates) {
			Predicate p = pi.getPredicate();
			if(PredicateUtil.containsOne(p.getName(), p.getVarnum(), knownPredicates)) {
				knownPredicates.add(p);
			}

		}
		
		if(debugPrint) System.out.println("\n*************** **** Considered "+rulesConsidered+" rules, for a total of "+ruleApplicationConsidered+" combinations.");
		if(debugPrint) System.out.println("*************** **** Consistency checks made "+overallConsistencyChecks);
		if(debugPrint) System.out.println("*************** **** "+statinconsistencycheck+" OWL reasoning checks, of which "+statinconsistencycheckfound+" found an inconsistency. Previous results were reused "+statinconsistencycheckreused+" times.");
		
		
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
