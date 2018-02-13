package logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;

public class PredicateTemplateImpl extends PredicateTemplateAbstr{

	private List<TextTemplate> name;
	private Binding[] bindings;
	
	public PredicateTemplateImpl(List<TextTemplate> name, Binding[] bindings) {
		this.name = name;
		this.bindings = bindings;
	}

	public List<TextTemplate> getName() {
		return name;
	}

	public Binding[] getBindings() {
		return bindings;
	}

	@Override
	public PredicateInstantiation applyRule(Map<String, RDFNode> bindingsMap, Set<Predicate> predicates, List<TextTemplate> label, Set<PredicateInstantiation> antecedent) {
		String predicateName = "";
		for(TextTemplate tt : name) {
			if(tt.isText()) predicateName += tt.getText();
			else {
				RDFNode node = bindingsMap.get("v"+tt.getVar());
				if(node == null || node.isAnon()) throw new RuntimeException("ERROR: Trying to instantiate a new predicate with an unbound variable in the predicate name.");
				else if(node.isLiteral()) predicateName += node.asLiteral().getLexicalForm();
				else if(node.isURIResource()) predicateName += RDFUtil.resolveLabelOfURI(node.asResource().getURI());
				else throw new RuntimeException("ERROR: cannot create predicate instantiation because node is neither null, nor blank, nor literal, nor URI");
			} 
		}
		Predicate predicate = null;
		if(PredicateUtil.containsOne(predicateName, bindings.length, predicates)) {
			predicate = PredicateUtil.get(predicateName, bindings.length, predicates);
		} else {
			Set<ConversionTriple> translationToRDF = new HashSet<ConversionTriple>();
			for(PredicateInstantiation pi: antecedent) {
				translationToRDF.addAll(pi.applyBinding(bindingsMap, bindings));
			}
			
			List<TextTemplate> textLabel = new LinkedList<TextTemplate>();
			for(TextTemplate tt : label) {
				if(tt.isText()) textLabel.add(tt);
				else {
					RDFNode newBinding = bindingsMap.get("v"+tt.getVar());
					if(newBinding != null && !newBinding.isAnon()) {
						if(newBinding.isLiteral()) textLabel.add(new TextTemplateImpl(newBinding.asLiteral().getLexicalForm()));
						else if(newBinding.isURIResource()) textLabel.add(new TextTemplateImpl(RDFUtil.resolveLabelOfURI(newBinding.asResource().getURI())));
						else throw new RuntimeException("ERROR: cannot create predicate instantiation because node is neither null, nor blank, nor literal, nor URI");
					} else {
						int found = -1;
						for(int i = 0; i < bindings.length; i++) {
							if(bindings[i].isVar() && bindings[i].getVar() == tt.getVar())
								found = i;
						}
						if(found != -1) textLabel.add(new TextTemplateImpl(found));
						else throw new RuntimeException("ERROR: trying to instantiate a predicate, but there is a variable in the label that is not a variable of the predicate signature.");
					}
				}
			}
			
			predicate = new PredicateImpl(predicateName, bindings.length, translationToRDF, textLabel);
		}
		Binding[] newBindings = new Binding[bindings.length];
		for(int i = 0; i < newBindings.length; i++) {
			if(bindings[i].isVar()) {
				// if the predicate template variables are not constant, check what they are bound to with this new binding
				RDFNode newBinding = bindingsMap.get("v"+bindings[i].getVar());
				if(newBinding != null && !newBinding.isAnon()) {
					newBindings[i] = new BindingImpl(new ResourceURI(newBinding.asResource().getURI()));
				} else newBindings[i] = bindings[i];
			} else {
				newBindings[i] = bindings[i];
			}
		}
		int order = 0;
		for(int i = 0; i < newBindings.length; i++) {
			if(newBindings[i].isVar()) {
				newBindings[i] = new BindingImpl(order);
				order++;
			}
		}
		return new PredicateInstantiationImpl(predicate, newBindings); 
	}



/*	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PredicateTemplateImpl other = (PredicateTemplateImpl) obj;
		if (!Arrays.equals(bindings, other.bindings))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}	*/
	
}
