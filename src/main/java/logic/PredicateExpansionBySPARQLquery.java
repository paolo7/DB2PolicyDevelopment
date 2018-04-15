package logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
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
	
	private boolean debugPrint = true;
	
	private Map<Rule, Set<Map<String,RDFNode>>> inconsistentRuleApplications = new HashMap<Rule, Set<Map<String,RDFNode>>>();
	
	public PredicateExpansionBySPARQLquery(Set<Predicate> knownPredicates, Set<Rule> rules) {
		this.knownPredicates = knownPredicates;
		this.rules = rules;
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

	@Override
	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates,
			Set<Predicate> knownPredicates, Set<Rule> rules) {
		PredicateExpansionBySPARQLquery expansion = new PredicateExpansionBySPARQLquery(knownPredicates,rules);
		Set<PredicateInstantiation> expandedPredicates = expansion.expand(existingPredicates);
		return expandedPredicates;
	}
	
	public boolean checkOWLconsistency(Rule r, Map<String,RDFNode> bindingsMap, Model baseModel) {
		overallConsistencyChecks++;
		//OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_DL_MEM_RDFS_INF);
		//model.add(baseModel);
		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
		reasoner = reasoner.bindSchema(baseModel);
		
		Model modelExpanded = RDFUtil.generateRuleInstantiationModel(r,bindingsMap,RDFprefixes, knownPredicates).add(baseModel);
		try {
			modelExpanded.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphExpandedRule.ttl")),"Turtle");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
		
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, modelExpanded);
		ValidityReport validity = infmodel.validate();
		if (validity.isValid()) {
			System.out.print(".");
		} else {
		}
		return validity.isValid();
	}

	public Set<PredicateInstantiation> expand(Set<PredicateInstantiation> existingPredicates) {
		return expand(existingPredicates,true);
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
		if(consistencyCheck) basicModel = RDFUtil.generateBasicModel(existingPredicates,RDFprefixes);
		
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
		    	ruleApplicationConsidered++;
				QuerySolution binding = rs.nextSolution();
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
				if(!inconsistentRuleApplications.containsKey(r))
					inconsistentRuleApplications.put(r, new HashSet<Map<String,RDFNode>>());
				if(validBinding && consistencyCheck) {
					if(inconsistentRuleApplications.get(r).contains(bindingsMap)) {
						System.out.print("@");						
						validBinding = false;
						statinconsistencycheckreused++;
					}
					else {
						if (!checkOWLconsistency(r,bindingsMap, basicModel)) {
							validBinding = false;
							inconsistentRuleApplications.get(r).add(bindingsMap);
							System.out.print("#");
							statinconsistencycheckfound++;
						}
						statinconsistencycheck++;
					}
				}
				if(validBinding) {					
					newPredicates.addAll(r.applyRule(bindingsMap, knownPredicates));
					/*for(PredicateInstantiation p : r.applyRule(bindingsMap, knownPredicates)) {
						if(p.getPredicate().getName().equals("hasClass")) {
							System.out.println(p);
							System.out.println("");
						}
					}*/
				}
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
