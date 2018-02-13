package logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

public class RDFUtil {
	
	public static String bnodeProxy = "http://w3id.org/prohow#BNODEPROXY"+new java.util.Date().getTime();
	
	public static Model generateModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		Property bnode = ResourceFactory.createProperty(bnodeProxy);
		
		for(PredicateInstantiation psi: predicates) {
			for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				Resource subject = bnode;
				Property predicate = bnode;
				RDFNode object = bnode;
				
				
				if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
				if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
				if(ct.getObject().isConstant()) object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
				
				if(ct.getSubject().isVar() && ct.getSubject().getVar() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar()).isConstant())
					subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getSubject().getVar()).getConstant().getLexicalValue()));
				if(ct.getPredicate().isVar() && ct.getPredicate().getVar() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar()).isConstant())
					predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(ct.getPredicate().getVar()).getConstant().getLexicalValue()));
				if(ct.getObject().isVar() && ct.getObject().getVar() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar()).isConstant())
					object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue()));
				
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
	
	public static String resolveLabelOfURI(String URI) {
		return URI.substring(URI.lastIndexOf("/") + 1);
	}
	
	public static String getSPARQLprefixes(Model m) {
		String prefixes = "";
		for(String key: m.getNsPrefixMap().keySet()) {
			prefixes += "PREFIX "+key+": <"+m.getNsPrefixMap().get(key)+">\n";
		}
		return prefixes;
	}
	
	
}
