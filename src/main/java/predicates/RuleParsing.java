package predicates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.cj.core.conf.url.ConnectionUrlParser.Pair;

public class RuleParsing {

	public static void parse(String filepath, Set<Predicate> predicates, Set<ExpansionRule> rules) throws IOException {
		File file = new File(filepath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		StringBuffer stringBuffer = new StringBuffer();
		String line;
		String mode = "";
		Predicate newpred = null;
		Map<String,Integer> varnamemap = null;
		while ((line = bufferedReader.readLine()) != null) {
			if(line.startsWith("END PREDICATE")) {
				mode = "";
				if(newpred != null) {
					predicates.add(newpred);
				}
			}
			if(line.startsWith("START PREDICATE")) {
				mode = "PREDICATE";
			}
			if(line.startsWith("SIGNATURE") && mode.equals("PREDICATE")) {
				Pair<Predicate,Map<String,Integer>> resultpair = parseSignature(line.replaceFirst("SIGNATURE", ""));
				newpred = resultpair.left;
				varnamemap = resultpair.right;
			}
			if(line.startsWith("LABEL") && mode.equals("PREDICATE")) {
				newpred.addLabel(parseLabel(line.replaceFirst("LABEL", "").trim(), varnamemap));
			}
			if(line.startsWith("RDF") && mode.equals("PREDICATE")) {
				newpred.setEquivalentRDF(parseRDF(line.replaceFirst("RDF", "").trim(), varnamemap));
			}
			if(line.startsWith("RULE") && !mode.equals("PREDICATE")) {
				rules.add(parseRule(line.replaceFirst("RULE", "").trim(),predicates));
			}
			
		}
		fileReader.close();
	}
	
	private static ExpansionRule parseRule(String text, Set<Predicate> predicates) {
		String[] components = text.split("<--");
		if (components.length != 2) throw new RuntimeException("ERROR, wrongly formatted rule: "+text);
		Map<String,Integer> varNameMap = new HashMap<String,Integer>();
		Set<ExpansionPredicate> consequent = parseExpansionPredicates(components[0],varNameMap);
		Set<ExpansionPredicate> antecedent = parseExpansionPredicates(components[1],varNameMap);
		//Set<Predicate> derivedPredicates = matchDerivedPredicates(consequent, predicates);
		return new ExpansionRule(antecedent,consequent,predicates);
	}
	
	private static Set<Predicate> matchDerivedPredicates(Set<ExpansionPredicate> consequent, Set<Predicate> predicates){
		Set<Predicate> matchedPredicates = new HashSet<Predicate>();
		for(ExpansionPredicate ep: consequent) {
			for(Predicate p: predicates) {
				if(PredicateExpansion.matches(ep, p) != null) {
					matchedPredicates.add(p);
				}
			}
		}
		return matchedPredicates;
	}
	
	private static Set<ExpansionPredicate> parseExpansionPredicates(String text,Map<String,Integer> varNameMap){
		Set<ExpansionPredicate> predicates = new HashSet<ExpansionPredicate>();
		String[] predicatesText = text.split("\\sAND\\s");
		for(int i = 0; i < predicatesText.length; i++) {
			predicates.add(parseExpansionPredicate(predicatesText[i],varNameMap));
		}
		return predicates;
	}
	
	private static ExpansionPredicate parseExpansionPredicate(String text, Map<String,Integer> varNameMap) {
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(text);
		String variables = null;
	    if(m.find()) {
	    	variables = m.group(1);    
	    }
	    String[] variableTokens = variables.split(",");
	    String predicatename = text.replaceAll("\\(.*\\)", "").trim();
	    String[] predicateTokens = predicatename.split("(\\[)|(\\])");
	    
	    List<TextTemplate> tt = new LinkedList<TextTemplate>();
	    
	    for(String t: predicateTokens) {
	    	if(t.startsWith("?")) {
				t = t.replaceFirst("\\?", "");
				if(! varNameMap.containsKey(t)) {					
					varNameMap.put(t, varNameMap.size());
				}
				tt.add(new TextTemplate(varNameMap.get(t)));
			}
			else {
				tt.add(new TextTemplate(t));
			}
	    }
	    List<Integer> variablesID = new LinkedList<Integer>();
	    for(String t : variableTokens) {
	    	t = t.replaceFirst("\\?","").trim();
	    	if(! varNameMap.containsKey(t)) {					
				varNameMap.put(t, varNameMap.size());
			}
	    	variablesID.add(varNameMap.get(t));
	    }
	    int[] variableIDarray = new int[variablesID.size()];
	    for (int i=0; i < variableIDarray.length; i++)
	    {
	    	variableIDarray[i] = variablesID.get(i).intValue();
	    }
	    return new ExpansionPredicate(tt,variableIDarray);	    
	}
	
	private static List<TextTemplate> parseLabel(String text, Map<String,Integer> varnamemap){
		List<TextTemplate> label = new LinkedList<TextTemplate>();
		String[] tokens = text.split(" ");
		for(String t: tokens) {
			if(t.startsWith("?")) {
				t = t.replaceFirst("\\?", "");
				label.add(new TextTemplate(varnamemap.get(t)));
			}
			else {
				label.add(new TextTemplate(t));
			}
		}
		return label;
	}
	
	private static Set<ConversionTriple> parseRDF(String text, Map<String,Integer> varnamemap){
		Set<ConversionTriple> RDFconversion = new HashSet<ConversionTriple>();
		String[] triples = text.split(" \\.");
		for(String triple: triples) {
			String[] tokens = triple.trim().split(" ");
			if(tokens.length == 3) {
				String subject = null;
				String predicate = null;
				String object = null;
				int subjectvar = -1;
				int predicatevar = -1;
				int objectvar = -1;
				for(String t: tokens) {
					String entity = null;
					int num = -1;
					if(t.startsWith("?")) {
						t = t.replaceFirst("\\?", "");
						if(!varnamemap.containsKey(t)) {
							throw new RuntimeException("ERROR: trying to parse a triple with variable '"+t+"' but this variable is not defined in the predicate signature.");
						}
						num = varnamemap.get(t);
					}
					else {
						entity = t;
					}
					if(subject == null && subjectvar == -1) {
						subject = entity;
						subjectvar = num;
					}
					else if(predicate == null && predicatevar == -1) {
						predicate = entity;
						predicatevar = num;
					}
					else if(object == null && objectvar == -1) {
						object = entity;
						objectvar = num;
					}
				}
				RDFconversion.add(new ConversionTriple(subject,predicate,object,subjectvar,predicatevar,objectvar));
			} else if(tokens.length > 0) {
				throw new RuntimeException("ERROR: RDF definition of a predicate is malformed, 3 tokens were expected but "+tokens.length+" were found: "+text);
			}
		}
		return RDFconversion;
	}
	
	private static Pair<Predicate,Map<String,Integer>> parseSignature(String text) {
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(text);
		Map<String,Integer> varNameMap = new HashMap<String,Integer>();
		String variables = null;
	    if(m.find()) {
	    	variables = m.group(1);    
	    }
	    String[] variableTokens = variables.split(",");
	    for(String t : variableTokens) {
	    	String varname = t.replaceFirst("\\?","").trim();
	    	varNameMap.put(varname, new Integer(varNameMap.size()));
	    }
	    String predicatename = text.replaceAll("\\(.*\\)", "").trim();

		return new Pair<Predicate, Map<String, Integer>>(new Predicate(predicatename,varNameMap.size()),varNameMap);
	}
}
