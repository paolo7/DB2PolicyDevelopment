package logic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;

public class RDFUtil {
	
	//remove
	public static String bnodeProxy = "http://w3id.org/prohow#BNODEPROXY"+new java.util.Date().getTime();
	public static String LAMBDAURI = "http://w3id.org/prohow/GPPG#LAMBDA"+new java.util.Date().getTime();

	
	public static PrefixMapping prefixes = PrefixMapping.Factory.create();
	
	private static void generateRuleInstantiationModelHelper(Model model, PredicateInstantiation psi, ConversionTriple ct, Map<String,RDFNode> bindingsMap, Integer i) {
		Resource subject = null;
		Property predicate = null;
		RDFNode object = null;
		// if element is constant
		if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
		if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
		if(ct.getObject().isConstant()) {
			if(ct.getObject().getConstant().isURI())
				object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
			else 
				object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
		}
		// if element is variable mapped to constant		
		if(ct.getSubject().isVar() && ct.getSubject().getVar() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar()).isConstant()) {
			if(!psi.getBinding(ct.getSubject().getVar()).getConstant().isURI())
				throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
			subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getSubject().getVar()).getConstant().getLexicalValue()));
		}
		if(ct.getPredicate().isVar() && ct.getPredicate().getVar() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar()).isConstant()) {
			if(!psi.getBinding(ct.getPredicate().getVar()).getConstant().isURI())
				throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
			predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(ct.getPredicate().getVar()).getConstant().getLexicalValue()));
		}
		if(ct.getObject().isVar() && ct.getObject().getVar() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar()).isConstant()) {
			if(psi.getBinding(ct.getObject().getVar()).getConstant().isURI())
				object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue()));
			else
				object = ResourceFactory.createPlainLiteral(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue());
		}
		// if element is variable mapped to variable	
		if(ct.getSubject().isVar() && ct.getSubject().getVar() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar()).isVar()) {
			RDFNode element = bindingsMap.get("v"+psi.getBinding(ct.getSubject().getVar()));
			if(element != null && !element.isURIResource())
				throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
			if(element == null || element.asResource().getURI().equals(LAMBDAURI))
				subject = ResourceFactory.createResource(LAMBDAURI+psi.getBinding(ct.getSubject().getVar()));
			else
				subject = element.asResource();
		}
		if(ct.getPredicate().isVar() && ct.getPredicate().getVar() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar()).isVar()) {
			RDFNode element = bindingsMap.get("v"+psi.getBinding(ct.getPredicate().getVar()));
			if(element != null && !element.isURIResource())
				throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
			if(element == null || element.asResource().getURI().equals(LAMBDAURI))
				predicate = ResourceFactory.createProperty(LAMBDAURI+psi.getBinding(ct.getPredicate().getVar()));
			else
				predicate = ResourceFactory.createProperty(model.expandPrefix(element.asResource().getLocalName()));
			i++;
		}
		if(ct.getObject().isVar() && ct.getObject().getVar() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar()).isVar()) {
			RDFNode element = bindingsMap.get("v"+psi.getBinding(ct.getObject().getVar()));
			if(element == null || element.isURIResource() && element.asResource().getURI().equals(LAMBDAURI))
				object = ResourceFactory.createResource(LAMBDAURI+psi.getBinding(ct.getObject().getVar()));
			else 
				object = element;
		}
		if(subject == null)
			subject = ResourceFactory.createResource();
		if(predicate == null) {
			predicate = ResourceFactory.createProperty(LAMBDAURI+i);
			i++;			
		}
		if(object == null)
			object = ResourceFactory.createResource();
		//
		if(subject == null)
			subject = ResourceFactory.createResource(LAMBDAURI+psi.getBinding(ct.getSubject().getVar()));
		if(predicate == null) {
			predicate = ResourceFactory.createProperty(LAMBDAURI+psi.getBinding(ct.getPredicate().getVar()));
		}
		if(object == null)
			object = ResourceFactory.createResource(LAMBDAURI+psi.getBinding(ct.getObject().getVar()));
		//
		Statement s = ResourceFactory.createStatement(subject, predicate, object);
		model.add(s);
	}
	
	public static Model generateRuleInstantiationModel(Rule r, Map<String,RDFNode> bindingsMap, Map<String,String> prefixes, Set<Predicate> knownPredicates) {
		Model model = ModelFactory.createDefaultModel();
		Integer i = 0;
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		for(PredicateInstantiation psi : r.getAntecedent()) {
			for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				generateRuleInstantiationModelHelper(model, psi, ct, bindingsMap, i);	
			}
		}
		for(PredicateInstantiation psi : r.applyRule(bindingsMap, knownPredicates)) {
			for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				generateRuleInstantiationModelHelper(model, psi, ct, bindingsMap, i);	
			}
		}
		
		return model;
	}
	
	
	public static Model generateBasicModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		int i = 0;		
		for(PredicateInstantiation psi: predicates) {
			for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				// if element is variable mapped to variable	
				Resource subject = ResourceFactory.createResource();
				Property predicate = ResourceFactory.createProperty(LAMBDAURI+i);
				i++;
				RDFNode object = ResourceFactory.createResource();	
				// if element is constant
				if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
				if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
				if(ct.getObject().isConstant()) {
					if(ct.getObject().getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
					else 
						object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
				}
				// if element is variable mapped to constant	
				if(ct.getSubject().isVar() && ct.getSubject().getVar() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar()).isConstant()) {
					if(!psi.getBinding(ct.getSubject().getVar()).getConstant().isURI())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getSubject().getVar()).getConstant().getLexicalValue()));
				}
				if(ct.getPredicate().isVar() && ct.getPredicate().getVar() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar()).isConstant()) {
					if(!psi.getBinding(ct.getPredicate().getVar()).getConstant().isURI())
						throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
					predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(ct.getPredicate().getVar()).getConstant().getLexicalValue()));
				}
				if(ct.getObject().isVar() && ct.getObject().getVar() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar()).isConstant()) {
					if(psi.getBinding(ct.getObject().getVar()).getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue()));
					else
						object = ResourceFactory.createPlainLiteral(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue());
				}
				
				Statement s = ResourceFactory.createStatement(subject, predicate, object);
				model.add(s);
			}
		}
		
		//try {
		//	model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphBasic.ttl")),"Turtle");
		//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		return model;
	}
	
	public static Model generateGPPGSandboxModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		Property lambda = ResourceFactory.createProperty(LAMBDAURI);
		//Resource lambda = ResourceFactory.createResource(LAMBDAURI);
		
		for(PredicateInstantiation psi: predicates) {
			for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
				Resource subject = lambda;
				Property predicate = lambda;
				RDFNode object = lambda;				
				if(ct.getSubject().isConstant()) subject = ResourceFactory.createResource(model.expandPrefix(ct.getSubject().getConstant().getLexicalValue()));
				if(ct.getPredicate().isConstant()) predicate = ResourceFactory.createProperty(model.expandPrefix(ct.getPredicate().getConstant().getLexicalValue()));
				if(ct.getObject().isConstant()) {
					if(ct.getObject().getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(ct.getObject().getConstant().getLexicalValue()));
					else 
						object = ResourceFactory.createPlainLiteral(ct.getObject().getConstant().getLexicalValue());
				}
				
				if(ct.getSubject().isVar() && ct.getSubject().getVar() < psi.getBindings().length && psi.getBinding(ct.getSubject().getVar()).isConstant()) {
					if(!psi.getBinding(ct.getSubject().getVar()).getConstant().isURI())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getSubject().getVar()).getConstant().getLexicalValue()));
				}
				if(ct.getPredicate().isVar() && ct.getPredicate().getVar() < psi.getBindings().length && psi.getBinding(ct.getPredicate().getVar()).isConstant()) {
					if(!psi.getBinding(ct.getPredicate().getVar()).getConstant().isURI())
						throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
					predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(ct.getPredicate().getVar()).getConstant().getLexicalValue()));
				}
				if(ct.getObject().isVar() && ct.getObject().getVar() < psi.getBindings().length && psi.getBinding(ct.getObject().getVar()).isConstant()) {
					if(psi.getBinding(ct.getObject().getVar()).getConstant().isURI())
						object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue()));
					else
						object = ResourceFactory.createPlainLiteral(psi.getBinding(ct.getObject().getVar()).getConstant().getLexicalValue());
				}
				
				Statement s = ResourceFactory.createStatement(subject, predicate, object);
				model.add(s);
			}
		}
		
		try {
			model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraph.ttl")),"Turtle");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}
	
	public static String expandPrefix(String URI) {
		return prefixes.expandPrefix(URI);
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
