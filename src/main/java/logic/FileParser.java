package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.mysql.cj.core.conf.url.ConnectionUrlParser.Pair;

public class FileParser {

	public static void parse(String filepath, Set<Predicate> predicates, Set<Rule> rules, Set<PredicateInstantiation> predicateInstantiation, boolean strictChecking) throws IOException  {
		
		File file = new File(filepath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		String mode = "";
		
		Map<String,Integer> varnamemap = null;
		String name = null;
		int varnum = -1;
		Set<ConversionTriple> translationToRDF = null;
		Set<ConversionFilter> translationToRDFFilters = null;
		List<TextTemplate> textLabel = null;
		
		Set<PredicateTemplate> rulePredicateTemplates = null;
		Set<PredicateInstantiation> ruleAntecedentPredicates = null;
		List<TextTemplate> ruleLabel = null;
		
		while ((line = bufferedReader.readLine()) != null) {
			if(line.startsWith("END PREDICATE")) {
				if(name != null && (translationToRDF != null || translationToRDFFilters != null) && textLabel != null && varnum >= 0) {
					predicates.add(new PredicateImpl(name, varnum, translationToRDF, translationToRDFFilters, textLabel));
				} else if(strictChecking){
					throw new RuntimeException("WARNING: end predicate statement reached, but a full predicate could not be created. To suppress this warning turn off flag strictChecking");
				}
			}
			if(line.startsWith("END RULE")) {
				if(rulePredicateTemplates != null && ruleAntecedentPredicates != null && 
						rulePredicateTemplates.size() > 0  && ruleAntecedentPredicates.size() > 0) {
					Rule r;
					if(ruleLabel != null && ruleLabel.size() > 0)
						r = new RuleImpl(ruleAntecedentPredicates, rulePredicateTemplates, ruleLabel);
					else
						r = new RuleImpl(ruleAntecedentPredicates, rulePredicateTemplates);
					rules.add(r);
				} else if(strictChecking){
					throw new RuntimeException("WARNING: end predicate statement reached, but a full predicate could not be created. To suppress this warning turn off flag strictChecking");
				}
			}
			if(line.startsWith("END")) {
				mode = "";
				varnamemap = null;
				name = null;
				varnum = -1;
				translationToRDF = null;
				translationToRDFFilters = null;
				textLabel = null;
				rulePredicateTemplates = null;
				ruleAntecedentPredicates = null;
				ruleLabel = null;
			}
			if(line.startsWith("START")) {
				if(!mode.equals("")) 
					throw new RuntimeException("ERROR: Started a new environment when parsing while environment "+mode+" was still active. Close this environemtn with an END clause");
			}
			if(line.startsWith("START PREDICATE")) {
				mode = "PREDICATE";
			}
			if(line.startsWith("START RULE")) {
				mode = "RULE";
			}
			if(line.startsWith("SIGNATURE") && mode.equals("PREDICATE")) {
				Pair<String,Map<String,Integer>> resultpair = parseSignature(line.replaceFirst("SIGNATURE", ""));
				name = resultpair.left;
				varnamemap = resultpair.right;
				varnum = resultpair.right.size();
			}
			if(line.startsWith("LABEL")) {
				if(mode.equals("PREDICATE"))
					textLabel = parseLabel(line.replaceFirst("LABEL", "").trim(), varnamemap);
				if(mode.equals("RULE"))
					ruleLabel = parseLabel(line.replaceFirst("LABEL", "").trim(), varnamemap);
			}
			if(line.startsWith("RDF") && mode.equals("PREDICATE")) {
				Pair<Set<ConversionTriple>, Set<ConversionFilter>> parsedResults = parseRDF(line.replaceFirst("RDF", "").trim(), varnamemap);
				translationToRDF  = parsedResults.left;
				translationToRDFFilters =  parsedResults.right;				
			}
			/*if(line.startsWith("RULE") && !mode.equals("PREDICATE")) {
				rules.add(parseRule(line.replaceFirst("RULE", "").trim(),predicates));
			}*/
			if(line.startsWith("START RULE")) {
				Triple<Set<PredicateTemplate>, Map<String,Integer>, Set<PredicateInstantiation>> ruleSignature = parseRule(line.replaceFirst("START RULE", "").trim(),predicates);
				varnamemap = ruleSignature.getMiddle();
				rulePredicateTemplates =  ruleSignature.getLeft();
				ruleAntecedentPredicates = ruleSignature.getRight();
			}
			if(line.startsWith("START AVAILABLE")) {
				predicateInstantiation.add(parseAvailablePredicate(line.replaceFirst("START AVAILABLE", "").trim(),predicates));
			}
		}
		bufferedReader.close();
		fileReader.close();
	}	
	
	private static PredicateInstantiation parseAvailablePredicate(String text, Set<Predicate> predicates) {
		return parseAntecedentPredicate(text, new HashMap<String,Integer>(), predicates);
	}
	
	private static Triple<Set<PredicateTemplate>, Map<String,Integer>, Set<PredicateInstantiation>> parseRule(String text, Set<Predicate> predicates) {
		String[] components = text.split("<--");
		if (components.length != 2) throw new RuntimeException("ERROR, wrongly formatted rule: "+text);
		Map<String,Integer> varNameMap = new HashMap<String,Integer>();
		Set<PredicateTemplate> consequent = parseConsequent(components[0],varNameMap);
		Set<PredicateInstantiation> antecedent = parseAntecedent(components[1],varNameMap, predicates);
		//Set<Predicate> derivedPredicates = matchDerivedPredicates(consequent, predicates);
		return new ImmutableTriple<Set<PredicateTemplate>, Map<String,Integer>, Set<PredicateInstantiation>>(consequent,varNameMap,antecedent);
	}
	
	
	private static Set<PredicateTemplate> parseConsequent(String text, Map<String,Integer> varNameMap){
		Set<PredicateTemplate> consequents = new HashSet<PredicateTemplate>();
		String[] predicatesText = text.split("\\sAND\\s");
		for(int i = 0; i < predicatesText.length; i++) {
			consequents.add(parseConsequentPredicate(predicatesText[i],varNameMap));
		}
		return consequents;
	}
	private static Set<PredicateInstantiation> parseAntecedent(String text,Map<String,Integer> varNameMap, Set<Predicate> predicates){
		Set<PredicateInstantiation> antecedent = new HashSet<PredicateInstantiation>();
		String[] predicatesText = text.split("\\sAND\\s");
		for(int i = 0; i < predicatesText.length; i++) {
			antecedent.add(parseAntecedentPredicate(predicatesText[i],varNameMap, predicates));
		}
		return antecedent;
	}
	
	private static PredicateInstantiation parseAntecedentPredicate(String text, Map<String,Integer> varNameMap, Set<Predicate> predicates) {
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(text);
		String variables = null;
	    if(m.find()) {
	    	variables = m.group(1);    
	    }
	    String[] variableTokens = variables.split(",");
	    String predicatename = text.replaceAll("\\(.*\\)", "").trim();

	    List<TextTemplate> tt = new LinkedList<TextTemplate>();

	    List<Binding> bindingList = new LinkedList<Binding>();
	    for(String t : variableTokens) {
	    	t = t.trim();
	    	if(t.startsWith("?")) {
	    		t = t.replaceFirst("\\?","").trim();
	    		if(! varNameMap.containsKey(t)) {					
	    			varNameMap.put(t, varNameMap.size());
	    		}
	    		bindingList.add(new BindingImpl(varNameMap.get(t)));
	    	} else {
	    		if(t.startsWith("\"") && t.endsWith("\""))
	    			bindingList.add(new BindingImpl(new ResourceLiteral(t.substring(1, t.length()-1))));
				else
					bindingList.add(new BindingImpl(new ResourceURI(t)));
	    		//bindingList.add(new BindingImpl(new ResourceURI(t)));
	    	}
	    }
	    Binding[] binding = new Binding[bindingList.size()];
	    for (int i=0; i < binding.length; i++)
	    {
	    	binding[i] = bindingList.get(i);
	    }
	    Predicate p = PredicateUtil.get(predicatename, bindingList.size(), predicates);
	    return new PredicateInstantiationImpl(p,binding);	    
	}
	
	private static PredicateTemplate parseConsequentPredicate(String text, Map<String,Integer> varNameMap) {
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
	    	t = t.trim();
	    	if(t.startsWith("?")) {
				t = t.replaceFirst("\\?", "");
				if(! varNameMap.containsKey(t)) {					
					varNameMap.put(t, varNameMap.size());
				}
				tt.add(new TextTemplateImpl(varNameMap.get(t)));
			}
			else {
				tt.add(new TextTemplateImpl(t));
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
	    Binding[] binding = new Binding[variablesID.size()];
	    for (int i=0; i < binding.length; i++)
	    {
	    	binding[i] = new BindingImpl(variablesID.get(i).intValue());
	    }
	    return new PredicateTemplateImpl(tt,binding);	    
	}
	
	private static Pair<String,Map<String,Integer>> parseSignature(String text) {
		Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(text);
		Map<String,Integer> varNameMap = new HashMap<String,Integer>();
		String variables = null;
		String[] variableTokens = new String[0];
	    if(m.find()) {
	    	variables = m.group(1);    
	    	variableTokens = variables.split(",");
	    }
	    for(String t : variableTokens) {
	    	t = t.trim();
	    	String varname = t.replaceFirst("\\?","").trim();
	    	varNameMap.put(varname, new Integer(varNameMap.size()));
	    }
	    String predicatename = text.replaceAll("\\(.*\\)", "").trim();

		return new Pair<String, Map<String, Integer>>(predicatename,varNameMap);
	}
	
	private static List<TextTemplate> parseLabel(String text, Map<String,Integer> varnamemap){
		List<TextTemplate> label = new LinkedList<TextTemplate>();
		String[] tokens = text.split(" ");
		for(String t: tokens) {
			t = t.trim();
			if(t.startsWith("?")) {
				t = t.replaceFirst("\\?", "");
				label.add(new TextTemplateImpl(varnamemap.get(t)));
			}
			else {
				label.add(new TextTemplateImpl(t));
			}
		}
		return label;
	}
	
	private static Pair<Set<ConversionTriple>,Set<ConversionFilter>> parseRDF(String text, Map<String,Integer> varnamemap){
		Set<ConversionTriple> RDFconversion = new HashSet<ConversionTriple>();
		Set<ConversionFilter> RDFconversionFilter = new HashSet<ConversionFilter>();
		String[] triples = text.split(" \\.");
		for(String triple: triples) {
			String[] tokens = triple.trim().split(" ");
			if(tokens[0].equals("FILTER")) {
				List<TextTemplate> templates = new LinkedList<TextTemplate>();
				for(String tt : tokens) {
					if(tt.startsWith("?")) {
						tt = tt.replaceFirst("\\?", "");
						if(!varnamemap.containsKey(tt)) {
							throw new RuntimeException("ERROR: trying to parse a FILTER with variable '"+tt+"' but this variable is not defined in the predicate signature.");
						}
						templates.add(new TextTemplateImpl(varnamemap.get(tt)));
					}
					else {
						templates.add(new TextTemplateImpl(tt));
					}
				}
				RDFconversionFilter.add(new ConversionFilter(templates));
			} else {				
				if(tokens.length == 3) {
					Binding subject = null;
					Binding predicate = null;
					Binding object = null;
					for(String t: tokens) {
						t = t.trim();
						Binding newBinding = null;
						if(t.startsWith("?")) {
							t = t.replaceFirst("\\?", "");
							if(!varnamemap.containsKey(t)) {
								throw new RuntimeException("ERROR: trying to parse a triple with variable '"+t+"' but this variable is not defined in the predicate signature.");
							}
							newBinding = new BindingImpl(varnamemap.get(t));
						}
						else {
							if(t.startsWith("\"") && t.endsWith("\""))
								newBinding = new BindingImpl(new ResourceLiteral(t.substring(1, t.length()-1)));
							else
								newBinding = new BindingImpl(new ResourceURI(t));
						}
						if(subject == null) {
							subject = newBinding;
						}
						else if(predicate == null) {
							predicate = newBinding;
						}
						else if(object == null) {
							object = newBinding;
						}
					}
					RDFconversion.add(new ConversionTripleImpl(subject,predicate,object));
				} else if(tokens.length > 0) {
					throw new RuntimeException("ERROR: RDF definition of a predicate is malformed, 3 tokens were expected but "+tokens.length+" were found: "+text);
				}
			}
		}
		if(RDFconversion.size() == 0) RDFconversion = null;
		if(RDFconversionFilter.size() == 0) RDFconversionFilter = null;
		return new Pair<Set<ConversionTriple>,Set<ConversionFilter>>(RDFconversion,RDFconversionFilter);
	}
	
	public static Map<String,String> parsePrefixes(String filepath) throws IOException{
		Map<String,String> map = new HashMap<String,String>();
		File file = new File(filepath);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line;
		
		while ((line = bufferedReader.readLine()) != null) {
			if(line.trim().length() > 0) {
				String[] parts = line.split(" ");
				if(parts.length != 2) throw new RuntimeException("ERROR: prefixes file malformed, each line should be either empty, or contain 2 space separated strings");
				map.put(parts[0], parts[1]);
			}
		}
		return map;
	}
}
