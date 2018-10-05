package logic;

import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class PredicateEvaluation {

	public static void evaluate(ExternalDB eDB, Set<PredicateInstantiation> predicates) {
		for (PredicateInstantiation p: predicates) {
			evaluate(eDB, p);
		}
	}
	
public static void evaluate(ExternalDB eDB, PredicateInstantiation predicate) {
	if(!predicate.hasVariables()) return;
	String SPARQLquery = RDFUtil.getSPARQLprefixes(eDB) + "SELECT * WHERE {" + predicate.toSPARQL() + "}";
	TupleQueryResult result = eDB.query(SPARQLquery);
	while (result.hasNext()) {
		BindingSet bindingSet = result.next();
		String resultString = "";
    	for (TextTemplate tt: predicate.getPredicate().getTextLabel()) {
    		if (tt.isText()) {
    			resultString += tt.getText()+" ";
    		} else {
    			Binding b = predicate.getBindings()[tt.getVar()];
    			if(b.isConstant()) {
    				resultString += RDFUtil.resolveLabelOfURI(b.getConstant().getLexicalValue())+" ";
    			} else {    				
    				Value v = bindingSet.getBinding("v"+b.getVar()).getValue(); 
    				resultString += RDFUtil.resolveLabelOfURI(v.stringValue())+" ";
    			}
    			
    		}
    	}
    	System.out.println(resultString);
		
		
        /*BindingSet bindingSet = result.next();
        for(String name : bindingSet.getBindingNames()) {
        	Value v = bindingSet.getBinding(name).getValue();
        	System.out.println(name+": "+v.stringValue());
        }
        System.out.println("");*/
    }
    result.close();
    
	}
}
