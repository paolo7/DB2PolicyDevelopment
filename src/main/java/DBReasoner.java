import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;

import dbconnection.DBConnection;

public class DBReasoner {

	public static String locationURI = "http://obsp.com/Location";
	public static int uriCounter = 0;
	/**
	 * 
	 * @param typeDefinitionTables
	 * @param observablePropertyTables
	 * @param URILabels must contain a key equal to String locationURI to specify its label
	 * @throws IOException
	 */
	public static void analyseDB(Map<String,String> typeDefinitionTables, 
								Map<String,String> observablePropertyTables,
								Map<String,String> URILabels) throws IOException {
		DBConnection db = StartSimulator.db;
		Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("DB-analysis.txt"), "utf-8"));
		Writer writer2 = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("DB-analysis2.txt"), "utf-8"));
		
		String locationLabel = URILabels.get(locationURI);
		
		for(String table : typeDefinitionTables.keySet() ) {
			String type = typeDefinitionTables.get(table);
			String typeLabel = URILabels.get(type);
			
			if(db.tableExists(table)) {
				
				if (db.columnInTableExists(table, "entityID")) {
					String predicate = typeLabel+"(?x)";
					String nlDescription = " - Entity ?x is a "+typeLabel;
					String sql = "SQL: Find if ?x is a "+typeLabel+" >> select 1 from dual where exists ( select entityID from "+table+" where entityID = ?x); \n"
							+ "SQL: Find all ?x such that "+predicate+" >> SELECT entityID FROM "+table;
					System.out.println(predicate+"\n"+nlDescription+"\n"+sql+"\n");
					writer.write(predicate+"\n"+nlDescription+"\n"+sql+"\n");
					String rdf = "?x"+uriCounter+" rdf:type <"+type+"> . ";
					writer2.write(rdf+"\n");
					uriCounter++;
					writer2.write("\n");
				}
				
				if (db.columnInTableExists(table, "location")) {
					String predicate = locationLabel+"OfFeature(?x,l)";
					String nlDescription = " - Property "+locationLabel+" of feature ?x is ?l";
					System.out.println(predicate+"\n"+nlDescription+"\n");
					writer.write(predicate+"\n"+nlDescription+"\n");				
					String rdf = "?x"+uriCounter+" <"+locationURI+"> ?l . ";
					writer2.write(rdf+"\n");
					uriCounter++;
					writer2.write("\n");
				}
				
				if (db.columnInTableExists(table, "entityID") && db.columnInTableExists(table, "location")) {
					String predicate = locationLabel+"Of"+typeLabel+"(?x,l) IFF "+typeLabel+"(?x) AND "+locationLabel+"OfFeature(?x,l)";
					String nlDescription = " - Property "+locationLabel+" of "+typeLabel+" ?x is ?l";
					System.out.println(predicate+"\n"+nlDescription+"\n");
					writer.write(predicate+"\n"+nlDescription+"\n");				
					String rdf = "?x"+uriCounter+" <"+locationURI+"> ?l . \n";
					rdf += "?x"+uriCounter+" rdf:type <"+type+"> . ";
					writer2.write(rdf+"\n");
					uriCounter++;
					writer2.write("\n");
				}
				
			}
		}
			
		for(String table : observablePropertyTables.keySet() ) {
			String type = observablePropertyTables.get(table);
			String typeLabel = URILabels.get(type);	
			if (db.columnInTableExists(table, "featureOfInterest") && 
					(db.columnInTableExists(table, "result") || db.columnInTableExists(table, "simpleResult"))) {
				String predicate = typeLabel+"OfFeature(?x,?y)";
				String nlDescription = " - Property "+typeLabel+" of feature ?x is ?y";
				String sql = "SQL: Discover the value of property "+typeLabel+" for feature ?x >> SELECT simpleResult FROM "+table+" WHERE featureOfInterest = ?x";
				System.out.println(predicate+"\n"+nlDescription+"\n"+sql+"\n");
				writer.write(predicate+"\n"+nlDescription+"\n"+sql+"\n");
				String rdf = "?x"+uriCounter+" rdf:type <http://www.w3.org/ns/sosa/Observation> . \n"
						+ "?x"+uriCounter+" <http://www.w3.org/ns/sosa/hasSimpleResult> ?y"+uriCounter+" . \n"
						+ "?x"+uriCounter+" <http://www.w3.org/ns/sosa/hasFeatureOfInterest> <"+type+"> . \n";
				writer2.write(rdf+"\n");
				uriCounter++;
				writer2.write("\n");
			}
			if (db.columnInTableExists(table, locationLabel) && 
					(db.columnInTableExists(table, "result") || db.columnInTableExists(table, "simpleResult"))) {
				String predicate = typeLabel+"AtLocation(?x,?l)";
				String nlDescription = " - At location ?l property "+typeLabel+" was measured to be ?x";
				System.out.println(predicate+"\n"+nlDescription+"\n");
				writer.write(predicate+"\n"+nlDescription+"\n");
				String rdf = "?x"+uriCounter+" rdf:type <http://www.w3.org/ns/sosa/Observation> . \n"
						+ "?x"+uriCounter+" <http://www.w3.org/ns/sosa/hasSimpleResult> ?y"+uriCounter+" . \n"
						+ "?x"+uriCounter+" <"+locationURI+"> ?l . \n";
				writer2.write(rdf+"\n");
				uriCounter++;
				writer2.write("\n");
			}	
				
			
			
		}
		
		writer2.close();
		writer.close();
	}
	
	public static void convertDBtoRDF(Map<String,String> typeDefinitionTables, 
			Map<String,String> observablePropertyTables,
			Map<String,String> URILabels) {
		
		
	}
}
