package logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileParserTest {

	public static void main(String[] args) throws IOException {
		
		Map<String,String> prefixes = FileParser.parsePrefixes(System.getProperty("user.dir") + "/resources/prefixes.txt");
		/*prefixes.put("example", "http://example.com/");
		prefixes.put("sosa", "http://www.w3.org/ns/sosa/");
		prefixes.put("ssn", "http://www.w3.org/ns/ssn/");
		prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		prefixes.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");*/
		
		RDFUtil.prefixes.setNsPrefixes(prefixes);
		
		Set<Predicate> predicates = new HashSet<Predicate>();
		Set<Rule> rules = new HashSet<Rule>();
		Set<PredicateInstantiation> existingPredicates = new HashSet<PredicateInstantiation>();
		
		FileParser.parse(System.getProperty("user.dir") + "/resources/rulesSimple.txt",
				predicates, rules, existingPredicates, true);
		
		System.out.println("*************** KNOWN PREDICATES\n" + 
				"*************** These are the definitions of the predicates that we want to consider\n");
		
		for(Predicate p: predicates) {
			System.out.println(p);
		}
		
		System.out.println("*************** RULES\n" + 
				"*************** These are rules that we want to apply\n");
		
		for(Rule r: rules) {
			System.out.println(r);
		}
		
		System.out.println("*************** AVAILABLE PREDICATES\n" + 
				"*************** These are the predicates that we assume are available from the start, i.e. the predicates that we can infer from a database\n");
		
		for(PredicateInstantiation p: existingPredicates) {
			System.out.println("AVAILABLE PREDICATE: "+p+"\n");
		}
		
		System.out.println("*************** APPLYING EXPANSION\n");
		PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(predicates, rules);
		expansion.setPrefixes(prefixes);
		Set<PredicateInstantiation> newPredicates = expansion.expand(existingPredicates);
		
		System.out.println("*************** INFERRED PREDICATES\n" + 
				"*************** These are the predicates that we can derived from the ones that we assume to be available\n");
		
		for(PredicateInstantiation p: newPredicates) {
			System.out.println("AVAILABLE PREDICATE: "+p+"\n----\n"+p.getPredicate()+"----\n");
		}
		
	}
}
