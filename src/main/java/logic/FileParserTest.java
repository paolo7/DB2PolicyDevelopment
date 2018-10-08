package logic;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.LogManager;

import org.apache.jena.rdf.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

import GraphDB.QueryUtil;

public class FileParserTest {

	
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    root.setLevel(level);
	}	

	
	public static void main(String[] args) throws Exception {
		
		setLoggingLevel(ch.qos.logback.classic.Level.ERROR);
		
		// this implementation uses GraphDB as an external triplestore that is GeoSparql enabled.
		// but any other triplestore that follows the rdf4j framework should be compatible
		ExternalDB eDB = new ExternalDB_GraphDB("http://152.78.64.224:7200/", "test", "temp");
		eDB.loadRDF(new File(System.getProperty("user.dir")+"/resources/localRDF.ttl"), RDFFormat.TURTLE);
		countTriples(eDB);
		
		Map<String,String> prefixes = FileParser.parsePrefixes(System.getProperty("user.dir") + "/resources/prefixes.txt");

		
		RDFUtil.prefixes.setNsPrefixes(prefixes);
		
		Set<Predicate> predicates = new HashSet<Predicate>();
		Set<Rule> rules = new HashSet<Rule>();
		Set<PredicateInstantiation> existingPredicates = new HashSet<PredicateInstantiation>();
		
		FileParser.parse(System.getProperty("user.dir") + "/resources/rulesSimulation.txt",
				predicates, rules, existingPredicates, true, eDB);
		
		LabelService labelservice = new LabelServiceImpl(existingPredicates, prefixes);
		RDFUtil.labelService = labelservice;
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
		
		
		System.out.println("\n*************** APPLYING RULES TO TRIPLESTORE BEFORE EXPANSION\n");
		
		//Model dataset = RDFUtil.loadModel(System.getProperty("user.dir") + "/resources/simulationData/data.ttl");
		for(String s: prefixes.keySet()) {
			eDB.setNamespace(s,prefixes.get(s));
		}
		PredicateEvaluation.evaluate(eDB, existingPredicates);
		
		
		LogManager.getLogManager().reset();
		System.out.println("*************** APPLYING EXPANSION\n");
		PredicateExpansion expansion = new PredicateExpansionBySPARQLquery(predicates, rules);
		expansion.setPrefixes(prefixes);
		Set<PredicateInstantiation> newPredicates = expansion.expand(existingPredicates);
		
		System.out.println("*************** INFERRED PREDICATES\n" + 
				"*************** These are the predicates that we can derive from the ones that we assume to be available\n");
		
		for(PredicateInstantiation p: newPredicates) {
			System.out.println("AVAILABLE PREDICATE: "+p+"\n----\n"+p.getPredicate()+"----\n");
		}
		
		// output results as JSON
		existingPredicates.addAll(newPredicates);
		JSONoutput.outputAsJSON("JSONoutput.json", existingPredicates);
		
		System.out.println("\n*************** APPLYING RULES TO TRIPLESTORE\n");
		
		//Model dataset = RDFUtil.loadModel(System.getProperty("user.dir") + "/resources/simulationData/data.ttl");
		//for(String s: prefixes.keySet()) {
		//	dataset.setNsPrefix(s,prefixes.get(s));
		//}
		//PredicateEvaluation.evaluate(dataset, existingPredicates);
		
		eDB.clearDB();
	}
	
	public static void countTriples(ExternalDB eDB) {
		TupleQueryResult result = eDB.query("SELECT (COUNT(*) AS ?no) WHERE { ?s ?p ?o  }");
		while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Value no = bindingSet.getBinding("no").getValue();

            System.out.println("Loaded dataset with "+no.stringValue()+" triples.");
        }
        result.close();
	}
	
	public static void testGeo(ExternalDB eDB) {
		/*SPARQLRepository q = new SPARQLRepository("http://152.78.64.224:7200/repositories/test1");
		q.initialize();
		RepositoryConnection connection = q.getConnection();
		TupleQueryResult result = QueryUtil.evaluateSelectQuery(connection,"PREFIX my: <http://example.org/ApplicationSchema#>\n" + 
				"PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" + 
				"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" + 
				"\n" + 
				"SELECT ?a ?b ?aWKT ?bWKT \n" + 
				"WHERE {\n" + 
				"    ?a <http://www.opengis.net/rdf#hasGeometry> ?aGeom .\n" + 
				"    ?b <http://www.opengis.net/rdf#hasGeometry> ?bGeom .\n" + 
				"    ?aGeom geo:asWKT ?aWKT .\n" + 
				"    ?bGeom geo:asWKT ?bWKT .\n" + 
				"    FILTER (geof:sfContains(?aWKT, ?bWKT))\n" + 
				"}");
		while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            for(String name : bindingSet.getBindingNames()) {
            	Value v = bindingSet.getBinding(name).getValue();
            	System.out.println(name+": "+v.stringValue());
            }
            System.out.println("");
        }
        result.close();*/
        
		TupleQueryResult result = eDB.query("PREFIX my: <http://example.org/ApplicationSchema#>\n" + 
				"PREFIX geo: <http://www.opengis.net/ont/geosparql#>\n" + 
				"PREFIX geof: <http://www.opengis.net/def/function/geosparql/>\n" + 
				"\n" + 
				"SELECT ?a ?b ?aWKT ?bWKT \n" + 
				"WHERE {\n" + 
				"    ?a <http://www.opengis.net/rdf#hasGeometry> ?aGeom .\n" + 
				"    ?b <http://www.opengis.net/rdf#hasGeometry> ?bGeom .\n" + 
				"    ?aGeom geo:asWKT ?aWKT .\n" + 
				"    ?bGeom geo:asWKT ?bWKT .\n" + 
				"    FILTER (geof:sfContains(?aWKT, ?bWKT))\n" + 
				"}");
		while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            for(String name : bindingSet.getBindingNames()) {
            	Value v = bindingSet.getBinding(name).getValue();
            	System.out.println(name+": "+v.stringValue());
            }
            System.out.println("");
        }
        result.close();
	}
}
