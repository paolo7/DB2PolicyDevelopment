package logic;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class RDFUtil {
	
	public static String LAMBDAURI = "http://w3id.org/prohow/GPPG#LAMBDA"+new java.util.Date().getTime();
	public static LabelService labelService;
	
	private static final Model mInternalModel = ModelFactory.createDefaultModel(); 
	
	public static PrefixMapping prefixes = PrefixMapping.Factory.create();
	
	private static void generateRuleInstantiationModelHelper(Model model, PredicateInstantiation psi, ConversionTriple ct, Map<String,RDFNode> bindingsMap, Integer i, Binding[] newPredicateBindings) {
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
		Binding objS = ct.getSubject();
		Binding objP = ct.getPredicate();
		Binding objO = ct.getObject();
		if(newPredicateBindings != null) {
			if(ct.getSubject().isVar() && objS.getVar() < newPredicateBindings.length) objS = newPredicateBindings[objS.getVar()];
			if(ct.getPredicate().isVar() && objP.getVar() < newPredicateBindings.length) objP = newPredicateBindings[objP.getVar()];
			if(ct.getObject().isVar() && objO.getVar() < newPredicateBindings.length) objO = newPredicateBindings[objO.getVar()];
		}
		
		// if element is variable mapped to constant		
		if(objS.isVar() && objS.getVar() < psi.getBindings().length && psi.getBinding(objS.getVar()).isConstant()) {
			if(!psi.getBinding(objS.getVar()).getConstant().isURI())
				throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
			subject = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(objS.getVar()).getConstant().getLexicalValue()));
		}
		if(objP.isVar() && objP.getVar() < psi.getBindings().length && psi.getBinding(objP.getVar()).isConstant()) {
			if(!psi.getBinding(objP.getVar()).getConstant().isURI())
				throw new RuntimeException("ERROR: the predicate of a triple cannot be a literal");
			predicate = ResourceFactory.createProperty(model.expandPrefix(psi.getBinding(objP.getVar()).getConstant().getLexicalValue()));
		}
		if(objO.isVar() && objO.getVar() < psi.getBindings().length && psi.getBinding(objO.getVar()).isConstant()) {
			if(psi.getBinding(objO.getVar()).getConstant().isURI())
				object = ResourceFactory.createResource(model.expandPrefix(psi.getBinding(objO.getVar()).getConstant().getLexicalValue()));
			else
				object = ResourceFactory.createPlainLiteral(psi.getBinding(objO.getVar()).getConstant().getLexicalValue());
		}
		// if element is variable mapped to variable	
		if(objS.isVar() && objS.getVar() < psi.getBindings().length && psi.getBinding(objS.getVar()).isVar()) {
			if(bindingsMap.containsKey("v"+psi.getBinding(objS.getVar()).getVar())) {
				RDFNode boundValue = bindingsMap.get("v"+psi.getBinding(objS.getVar()).getVar());
				if(boundValue == null) {
					RDFNode element = null;//bindingsMap.get("v"+psi.getBinding(ct.getSubject().getVar()).getVar());
					if(element != null && !element.isURIResource())
						throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
					if(element == null || element.asResource().getURI().equals(LAMBDAURI))
						subject = model.createResource(new AnonId(LAMBDAURI+psi.getBinding(objS.getVar())));
					else
						subject = element.asResource();					
				} else {
					subject = boundValue.asResource();
				}
			}
		}
		if(objP.isVar() && objP.getVar() < psi.getBindings().length && psi.getBinding(objP.getVar()).isVar() ) {
			RDFNode boundValue = bindingsMap.get("v"+psi.getBinding(objP.getVar()).getVar());
			if(boundValue == null) {
				RDFNode element = null;//bindingsMap.get("v"+psi.getBinding(ct.getPredicate().getVar()).getVar());
				if(element != null && !element.isURIResource())
					throw new RuntimeException("ERROR: the subject of a triple cannot be a literal");
				if(element == null || element.asResource().getURI().equals(LAMBDAURI))
					throw new RuntimeException("ERROR: the predicate of a triple cannot be assigned a lambda value as it cannot be a blank node");
					//predicate = model.createResource(new AnonId(LAMBDAURI+psi.getBinding(objP.getVar()));
				else
					predicate = ResourceFactory.createProperty(model.expandPrefix(element.asResource().getLocalName()));
				i++;
			} else {
				predicate = ResourceFactory.createProperty(boundValue.asResource().getURI());
			}
		}
		if(objO.isVar() && objO.getVar() < psi.getBindings().length && psi.getBinding(objO.getVar()).isVar()) {
			RDFNode boundValue = bindingsMap.get("v"+psi.getBinding(objO.getVar()).getVar());
			if(boundValue == null) {
				RDFNode element = null;//bindingsMap.get("v"+psi.getBinding(ct.getObject().getVar()).getVar());
				if(element == null || element.isURIResource() && element.asResource().getURI().equals(LAMBDAURI))
					object = model.createResource(new AnonId(LAMBDAURI+psi.getBinding(objO.getVar())));
				else 
					object = element;
			} else {
				object = boundValue.asResource();
			}
		}
		/*if(subject == null)
			subject = ResourceFactory.createResource();
		if(predicate == null) {
			predicate = ResourceFactory.createProperty(LAMBDAURI+h+1);
			i++;
		}
		if(object == null)
			object = ResourceFactory.createResource();
		//
*/		if(subject == null)
			subject = model.createResource(new AnonId(LAMBDAURI+"?v"+objS.getVar()));
		if(predicate == null) {
			throw new RuntimeException("ERROR: the predicate of a triple cannot be assigned a lambda value as it cannot be a blank node");
			//predicate = model.createResource(new AnonId(LAMBDAURI+"?v"+objP.getVar());
		}
		if(object == null)
			object = model.createResource(new AnonId(LAMBDAURI+"?v"+objO.getVar()));
		//
		Statement s = ResourceFactory.createStatement(subject, predicate, object);
		model.add(s);
	}
	
	public static Model generateRuleInstantiationModel(Rule r, Map<String,RDFNode> bindingsMap, Map<String,String> prefixes, Set<Predicate> knownPredicates, Set<PredicateInstantiation> inferrablePredicates) {
		Model model = ModelFactory.createDefaultModel();
		Integer i = 0;
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		for(PredicateInstantiation psi : r.getAntecedent()) {
			Set<ConversionTriple> translationPlusConsequences = new HashSet<ConversionTriple>();
			if(psi.getPredicate().getRDFtranslation() != null) translationPlusConsequences.addAll(psi.getPredicate().getRDFtranslation());
			translationPlusConsequences.addAll(psi.getAdditionalConstraints());
			Binding[] newB = r.getNewPredicateBasicBindings();
			for(ConversionTriple ct: translationPlusConsequences) {
				generateRuleInstantiationModelHelper(model, psi, ct, bindingsMap, i, null);	
			}
		}
		// TODO fix, this part is not working as intended
		for(PredicateInstantiation psi : inferrablePredicates) {
			Set<ConversionTriple> translationPlusConsequences = new HashSet<ConversionTriple>();
			translationPlusConsequences.addAll(psi.getPredicate().getRDFtranslation());
			translationPlusConsequences.addAll(psi.getAdditionalConstraints());
			for(ConversionTriple ct: translationPlusConsequences) {
				generateRuleInstantiationModelHelper(model, psi, ct, bindingsMap, i, r.getNewPredicateBasicBindings());	
			}
		}
		
		
		//try {
			//model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraphInstantiationModel.ttl")),"Turtle");
			//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
		return model;
	}
	
	public static Model generateBasicModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		int i = 0;		
		for(PredicateInstantiation psi: predicates) if (psi.getPredicate().getRDFtranslation() != null){
			Set<ConversionTriple> translationPlusConsequences = new HashSet<ConversionTriple>();
			translationPlusConsequences.addAll(psi.getPredicate().getRDFtranslation());
			translationPlusConsequences.addAll(psi.getAdditionalConstraints());
			for(ConversionTriple ct: translationPlusConsequences) {
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
	
	public static void addToDefaultPrefixes(Map<String,String> prefix) {
		for(String s: prefix.keySet()) {
			prefixes.setNsPrefix(s,prefix.get(s));
		}
	}
	public static void addToDefaultPrefixes(Model additionalVocabularies) {
		prefixes.setNsPrefixes(additionalVocabularies).getNsPrefixMap();
	}
	
	public static Model generateGPPGSandboxModel(Set<PredicateInstantiation> predicates, Map<String,String> prefixes) {
		Model model = ModelFactory.createDefaultModel();
		for(String s: prefixes.keySet()) {
			model.setNsPrefix(s,prefixes.get(s));
		}
		
		Property lambda = ResourceFactory.createProperty(LAMBDAURI);
		//Resource lambda = ResourceFactory.createResource(LAMBDAURI);
		
		for(PredicateInstantiation psi: predicates) {
			if(psi.getPredicate().getRDFtranslation() != null) for(ConversionTriple ct: psi.getPredicate().getRDFtranslation()) {
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
		
		//try {
		//	model.write(new FileOutputStream(new File(System.getProperty("user.dir") + "/resources/outputgraph.ttl")),"Turtle");
		//} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
		return model;
	}
	
	public static String expandPrefix(String URI) {
		return prefixes.expandPrefix(URI);
	}
	
	public static String resolveLabelOfURI(String URI) {
		if(labelService != null && labelService.hasLabel(URI))
			return labelService.getLabel(URI);
		if(labelService != null && labelService.hasLabel(expandPrefix(URI)))
			return labelService.getLabel(expandPrefix(URI));
		//return URI;
		if(URI.contains("/")) return URI.substring(expandPrefix(URI).lastIndexOf("/") + 1);
		else return URI;
	}
	
	public static boolean isNumericDatatypeIRI(IRI iri) {
		if(iri.equals(XMLSchema.DECIMAL) || 
				iri.equals(XMLSchema.DOUBLE) || 
				iri.equals(XMLSchema.FLOAT) ||
				iri.equals(XMLSchema.INT) ||
				iri.equals(XMLSchema.INTEGER) ||
				iri.equals(XMLSchema.LONG) ||
				iri.equals(XMLSchema.NEGATIVE_INTEGER) ||
				iri.equals(XMLSchema.POSITIVE_INTEGER) ||
				iri.equals(XMLSchema.SHORT) ||
				iri.equals(XMLSchema.UNSIGNED_INT) ||
				iri.equals(XMLSchema.UNSIGNED_LONG) ||
				iri.equals(XMLSchema.UNSIGNED_SHORT) 
				)
			return true;
		return false;
	}
	
	public static String resolveLabelOfURIasURIstring(String URI) {
		String availableLabel = resolveLabelOfURI(URI);
		// camel case code from https://stackoverflow.com/questions/249470/javame-convert-string-to-camelcase
		StringBuffer result = new StringBuffer(availableLabel.length());
		String strl = availableLabel.toLowerCase();
		boolean bMustCapitalize = true;
		for (int i = 0; i < strl.length(); i++)
		{
		  char c = strl.charAt(i);
		  if (c >= 'a' && c <= 'z')
		  {
		    if (bMustCapitalize)
		    {
		      result.append(strl.substring(i, i+1).toUpperCase());
		      bMustCapitalize = false;
		    }
		    else
		    {
		      result.append(c);
		    }
		  }
		  else
		  {
		    bMustCapitalize = true;
		  }
		}
		availableLabel = result.toString().replaceAll("[^A-Za-z0-9]", "");
		if(availableLabel.length() < 15 && availableLabel.length() > 0) return availableLabel;
		else return URI.substring(URI.lastIndexOf("/") + 1);
	}
	
	public static String getSPARQLprefixes(Model m) {
		String prefixes = "";
		for(String key: m.getNsPrefixMap().keySet()) {
			prefixes += "PREFIX "+key+": <"+m.getNsPrefixMap().get(key)+">\n";
		}
		return prefixes;
	}
	
	public static String getSPARQLprefixes(ExternalDB eDB) {
		String prefixes = "";
		Map<String,String> namespaces = eDB.getNamespaces();
		for(String key: namespaces.keySet()) {
			prefixes += "PREFIX "+key+": <"+namespaces.get(key)+">\n";
		}
		return prefixes;
	}
	
	public static String getSPARQLdefaultPrefixes() {
		String prefixesString = "";
		for(String key : prefixes.getNsPrefixMap().keySet()) {
			prefixesString += "PREFIX "+key+": <"+prefixes.getNsPrefixMap().get(key)+">\n";
		}
		return prefixesString;
	}
	
	public static Model loadModel(String path) {
		Model model = ModelFactory.createDefaultModel();
		model.read(path) ;
		return model;
	}
	
	//part of the code taken from from http://www.javased.com/index.php?source_dir=Empire/jena/src/com/clarkparsia/empire/jena/util/JenaSesameUtils.java
	public static RDFNode asJenaNode(Value theValue) { 
		  if (theValue instanceof org.eclipse.rdf4j.model.Literal) { 
		   return asJenaLiteral( (org.eclipse.rdf4j.model.Literal) theValue); 
		  } 
		  else { 
		   return asJenaResource( (org.eclipse.rdf4j.model.Resource) theValue); 
		  } 
		 } 
	
	public static void loadLabelsFromModel(Model m) {
		Selector selector = new SimpleSelector(null, ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"), (String) null);
		StmtIterator iter = m.listStatements(selector);
		while(iter.hasNext()) {
			Statement s = iter.next();
			String uri = s.getSubject().getURI();
			String label = s.getObject().asLiteral().getLexicalForm();
			RDFUtil.labelService.setLabel(uri, label);
		}
	}
	
	public static Literal asJenaLiteral(org.eclipse.rdf4j.model.Literal theLiteral) { 
		  if (theLiteral == null) { 
		   return null; 
		  } 
		  else if (theLiteral.getLanguage() != null) { 
			  Optional<String> language = theLiteral.getLanguage();
			  if (language.isPresent()) return mInternalModel.createLiteral(theLiteral.getLabel(), language.get() );
			  else return mInternalModel.createLiteral(theLiteral.getLabel());
		  } 
		  else if (theLiteral.getDatatype() != null) { 
		   return mInternalModel.createTypedLiteral(theLiteral.getLabel(), 
		              theLiteral.getDatatype().toString()); 
		  } 
		  else { 
		   return mInternalModel.createLiteral(theLiteral.getLabel()); 
		  } 
		 } 
	public static Resource asJenaResource(org.eclipse.rdf4j.model.Resource theRes) { 
		  if (theRes == null) { 
		   return null; 
		  } 
		  else if (theRes instanceof IRI) { 
		   return asJenaIRI( (IRI) theRes); 
		  } 
		  else { 
		   return mInternalModel.createResource(new org.apache.jena.rdf.model.AnonId(((BNode) theRes).getID())); 
		  } 
		 }
	 public static Property asJenaIRI(IRI theIRI) { 
		  if (theIRI == null) { 
		   return null; 
		  } 
		  else { 
		   return mInternalModel.getProperty(theIRI.toString()); 
		  } 
		 } 
	 
	 public static int index = 0;
	 public static String getBlankNodeBaseURI() {
		 String baseURI = prefixes.getNsPrefixURI("blanknode")+index+"n"+new Date().getTime()+"v";
		 index++;
		 return baseURI;
	 }
	 
	public static String getBlankNodeOrNewVarString(String baseNew, int var) {
		if (baseNew == null)
			return "_:b"+var;
		else return "<"+baseNew+var+">";
	}
	
	public static int filterRedundantPredicates(Set<PredicateInstantiation> set1, Set<PredicateInstantiation> set2, boolean strict) {
		Set<PredicateInstantiation> toRemove = new HashSet<PredicateInstantiation>();
		int before = set1.size() + set2.size();
		for(PredicateInstantiation pi1: set1) {
			for (PredicateInstantiation pi2: set1) {
				if(!pi1.equals(pi2))
					toRemove.add(getRedundant(pi1,pi2, strict));
			}
			for (PredicateInstantiation pi2: set2) {
				toRemove.add(getRedundant(pi1,pi2, strict));
			}
		}
		for(PredicateInstantiation pi1: set2) {
			for (PredicateInstantiation pi2: set1) {
				toRemove.add(getRedundant(pi1,pi2, strict));
			}
			for (PredicateInstantiation pi2: set2) {
				if(!pi1.equals(pi2))
					toRemove.add(getRedundant(pi1,pi2, strict));
			}
		}
		toRemove.remove(null);
		for(PredicateInstantiation pi2rm: toRemove) {
			System.out.println("REMOVING "+pi2rm);
			set1.remove(pi2rm);
			set2.remove(pi2rm);
		}
		return before - (set1.size() + set2.size());
	}
	private static PredicateInstantiation getRedundant(PredicateInstantiation pi1, PredicateInstantiation pi2, boolean strict) {
		if(isSubsumedBy(pi1,pi2, strict) && isSubsumedBy(pi2,pi1, strict)) {
			// if the only difference is the additional constraints, return the one that has a subset of constraints compareed to the other
			if(pi1.getAdditionalConstraints().containsAll(pi2.getAdditionalConstraints()))
				return pi1;
			if(pi2.getAdditionalConstraints().containsAll(pi1.getAdditionalConstraints()))
				return pi2;
			// if the set of constraints is different, then do not return either of them
			if(strict) return null;
			else {
				return null;
				/*if(pi1.getAdditionalConstraints().size() < pi2.getAdditionalConstraints().size()) 
					return pi2;
				else 
					return pi1;*/
			}
		}
		if(isSubsumedBy(pi1,pi2, strict)) 
			return pi1;
		if(isSubsumedBy(pi2,pi1, strict)) 
			return pi2;
		return null;
	}
	private static boolean isSubsumedBy(PredicateInstantiation pi1, PredicateInstantiation pi2, boolean strict) {
		// instantiations of different predicates cannot subsume each other
		if(! pi1.getPredicate().equals(pi2.getPredicate())) return false;
		for(int i = 0; i < pi1.getBindings().length; i++) {
			
			if(pi1.getPredicate().getName().equals("GeometryInGeometry") && pi1.getBindings()[i].isConstant() && pi1.getBindings()[i].getConstant().isURI() && pi1.getBindings()[i].getConstant().getLexicalValueExpanded().equals("<http://example.com/COconcentration>")) {
				System.out.println("");
			}
			
			if(! varIsSubsumedBy(pi1.getBindings()[i], pi2.getBindings()[i], strict)) return false;
		}
		return true;
	}
	private static boolean varIsSubsumedBy(Binding b1, Binding b2, boolean strict) {
		
		// if they are the same, the first one subsumes the other
		if(b1.equals(b2)) return true;
		// a constant is subsumed by a variable
		if(b1.isConstant() && b2.isVar()) return true;
		if(strict) {
			// different variables might result in different semantic interpretations
			if(b1.isVar() && b2.isVar() && !b1.equals(b2)) return false;
		} else {
			if(b1.isVar() && b2.isVar()) return true;
		}
		return false;
	}
	
}
