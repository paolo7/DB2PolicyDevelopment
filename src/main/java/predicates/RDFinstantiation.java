package predicates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

public class RDFinstantiation {

	public RDFinstantiation() {
		
	}
	
	public static Model generateModel(Set<PredicateSemiInstantiation> predicates, boolean singleBNode) {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("example", "http://example.com/");
		model.setNsPrefix("sosa", "http://www.w3.org/ns/sosa/");
		model.setNsPrefix("ssn", "http://www.w3.org/ns/ssn/");
		model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		
		Resource bnode = model.createResource();
		
		for(PredicateSemiInstantiation psi: predicates) {
			for(ConversionTriple ct: psi.getEquivalentRDF()) {
				Resource subject = model.createResource();
				Property predicate = ResourceFactory.createProperty(model.expandPrefix(ct.predicate));
				RDFNode object = model.createResource();
				
				if (singleBNode) {
					subject = bnode;
					object = bnode;
				}
				
				if(ct.subject != null) subject = ResourceFactory.createResource(model.expandPrefix(ct.subject));
				if(ct.object != null) object = ResourceFactory.createResource(model.expandPrefix(ct.object));
				
				Statement s = ResourceFactory.createStatement(subject, predicate, object);
				model.add(s);
			}
		}
		
		try {
			model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraph.ttl")),"RDF/XML");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return model;
	}
	
	public static String getSPARQLprefixes(Model m) {
		String prefixes = "";
		for(String key: m.getNsPrefixMap().keySet()) {
			prefixes += "PREFIX "+key+": <"+m.getNsPrefixMap().get(key)+">\n";
		}
		return prefixes;
	}
}
