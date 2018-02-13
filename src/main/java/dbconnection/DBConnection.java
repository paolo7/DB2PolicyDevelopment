package dbconnection;

public interface DBConnection {
	public boolean initialiseDB();
	
	public boolean insertStatement(String s);
	
	public boolean tableExists(String tablename);
	
	public boolean columnInTableExists(String tablename, String columname);
	
	
	
}
