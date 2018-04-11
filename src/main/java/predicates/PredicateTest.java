package predicates;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.mysql.cj.core.conf.url.ConnectionUrlParser.Pair;

public class PredicateTest {

	public static void main(String[] args) throws IOException {
		
		Set<Predicate> predicates = new HashSet<Predicate>();//generateBasePredicates();
		Set<ExpansionRule> rules = new HashSet<ExpansionRule>(); //generateExpansionRules();
		
		RuleParsing.parse(System.getProperty("user.dir") + "/resources/rules.txt",predicates,rules);
		
		Set<PredicateSemiInstantiation> initialPredicates = new HashSet<PredicateSemiInstantiation>();
		
		List<Pair<String,String[]>> predicatesToAdd = new LinkedList<Pair<String,String[]>>();
		predicatesToAdd.add(new Pair<String,String[]>("room",new String[]{null}));
		predicatesToAdd.add(new Pair<String,String[]>("employee",new String[]{null}));
		predicatesToAdd.add(new Pair<String,String[]>("measurementOfObservation",new String[]{null,null}));
		predicatesToAdd.add(new Pair<String,String[]>("propertyOfObservation",new String[]{null,"example:CO2concentration"}));
		predicatesToAdd.add(new Pair<String,String[]>("featureOfObservation",new String[]{null,null}));
		
		
		for(Predicate p : predicates) {
			for(Pair<String,String[]> pta : predicatesToAdd) {				
				if(p.name.equals(pta.left)
						) initialPredicates.add(new PredicateSemiInstantiation(p,pta.right));
			}
		}
		
		PredicateExpansion expansion = new PredicateExpansion(rules);
		Set<PredicateSemiInstantiation> newpredicates = expansion.expand(initialPredicates);
		
		/*System.out.println("\n***\nPrinting all predicates:\n***\n");
		for(Predicate p : predicates) {
			System.out.println(p.prettyToString());
		}
		*/
		for(Predicate p : predicates) {
			if(p.equivalentRDF == null) throw new RuntimeException("aaa");
		}
		
		System.out.println("\n***\nPrinting rules:\n***\n");
		for(ExpansionRule rule : rules) {
			System.out.println(rule.prettyToString());
		}
		System.out.println("\n***\nPrinting initial predicates:\n***\n");
		for(PredicateSemiInstantiation predicate : initialPredicates) {
			System.out.println(predicate.prettyToString());
		}
		System.out.println("\n***\nPrinting new predicates:\n***\n");
		for(PredicateSemiInstantiation predicate : newpredicates) {
			if(!initialPredicates.contains(predicate))
				System.out.println(predicate.prettyToString());
		}
		
	}
	
	public static Set<ExpansionRule> generateExpansionRules(){
		Predicate observation = new Predicate("observation",1);
		{
			Set<ConversionTriple> equivalentRDF = new HashSet<ConversionTriple>();
			ConversionTriple ctriple = new ConversionTriple(
					null, 
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
					"http://www.w3.org/ns/sosa/Observation",
					0,-1,-1);
			equivalentRDF.add(ctriple);
			observation.setEquivalentRDF(equivalentRDF);
			Set<List<TextTemplate>> labels = new HashSet<List<TextTemplate>>();
			List<TextTemplate> label = new LinkedList<TextTemplate>();
			label.add(new TextTemplate("observation"));
			label.add(new TextTemplate(0));
			labels.add(label);
			observation.setLabels(labels);
		}
		
		Set<ExpansionRule> rules = new HashSet<ExpansionRule>();
		{
			Set<ExpansionPredicate> antecedent = new HashSet<ExpansionPredicate>();
			Set<ExpansionPredicate> consequent = new HashSet<ExpansionPredicate>();
			{
				List<TextTemplate> name = new LinkedList<TextTemplate>();
				name.add(new TextTemplate("observation"));
				ExpansionPredicate ep1 = new ExpansionPredicate(name,new int[] {0});
				consequent.add(ep1);
			}
			{
				List<TextTemplate> name = new LinkedList<TextTemplate>();
				name.add(new TextTemplate("madeObservation"));
				ExpansionPredicate ep1 = new ExpansionPredicate(name,new int[] {1,0});
				antecedent.add(ep1);
			}
			Set<Predicate> predicatesToInfer = new HashSet<Predicate>();
			predicatesToInfer.add(observation);
			ExpansionRule rule = new ExpansionRule(antecedent, consequent,predicatesToInfer);
			rules.add(rule);
		}
		
		return rules;
	}
	
	public static Set<Predicate> generateBasePredicates(){
		
		Set<Predicate> predicates = new HashSet<Predicate>();
		{
			Predicate predicate = new Predicate("madeObservation",2);
			Set<ConversionTriple> equivalentRDF = new HashSet<ConversionTriple>();
			ConversionTriple ctriple = new ConversionTriple(
					null, 
					"http://www.w3.org/ns/sosa/madeObservation", 
					null,
					0,-1,1);
			equivalentRDF.add(ctriple);
			predicate.setEquivalentRDF(equivalentRDF);
			predicates.add(predicate);
			Set<List<TextTemplate>> labels = new HashSet<List<TextTemplate>>();
			List<TextTemplate> label = new LinkedList<TextTemplate>();
			label.add(new TextTemplate(0));
			label.add(new TextTemplate("made"));
			label.add(new TextTemplate(1));
			labels.add(label);
			predicate.setLabels(labels);
		}
		/**
		{
			Predicate predicate = new Predicate("observation",1);
			Set<ConversionTriple> equivalentRDF = new HashSet<ConversionTriple>();
			ConversionTriple ctriple = new ConversionTriple(
					null, 
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
					"http://www.w3.org/ns/sosa/Observation",
					0,-1,-1);
			equivalentRDF.add(ctriple);
			predicate.setEquivalentRDF(equivalentRDF);
			predicates.add(predicate);
			Set<List<TextTemplate>> labels = new HashSet<List<TextTemplate>>();
			List<TextTemplate> label = new LinkedList<TextTemplate>();
			label.add(new TextTemplate("observation"));
			label.add(new TextTemplate(0));
			labels.add(label);
			predicate.setLabels(labels);
		}
		{
			Predicate predicate = new Predicate("sensor",1);
			Set<ConversionTriple> equivalentRDF = new HashSet<ConversionTriple>();
			ConversionTriple ctriple = new ConversionTriple(
					null, 
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
					"http://www.w3.org/ns/sosa/Sensor",
					0,-1,-1);
			equivalentRDF.add(ctriple);
			predicate.setEquivalentRDF(equivalentRDF);
			predicates.add(predicate);
			Set<List<TextTemplate>> labels = new HashSet<List<TextTemplate>>();
			List<TextTemplate> label = new LinkedList<TextTemplate>();
			label.add(new TextTemplate("sensor"));
			label.add(new TextTemplate(0));
			labels.add(label);
			predicate.setLabels(labels);
		}
		**/
		return predicates;
	}
}
