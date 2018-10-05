package dbconnection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ibatis.common.jdbc.ScriptRunner;

public class MySQLConnection implements DBConnection {

	private String username;
	private String password;
	private String databasename;
	private Connection conn;
	
	public MySQLConnection(String username, String password, String databasename) {
		this.username = username;
		this.password = password;
		this.databasename = databasename;
	}
	
	@Override
	public boolean initialiseDB() {
		Connection c;
		try {
			System.out.println("DB initialisation");
			//Class.forName("com.mysql.jdbc.Driver");
			c = DriverManager.getConnection
					("jdbc:mysql://localhost/?useSSL=false&user="+username+"&password="+password);
			boolean DBexists = false;
			ResultSet resultSet = c.getMetaData().getCatalogs();
	        while (resultSet.next()) {
	          String dbname = resultSet.getString(1);
	            if(dbname.equals(databasename)){
	            	DBexists = true;
	            }
	        }
	        resultSet.close();
	        System.out.println("DB already existing? "+DBexists);
	        if (DBexists) {
	        	System.out.println("Deleting database "+databasename);
	        	Statement s = c.createStatement();
	        	s.executeUpdate("DROP DATABASE "+databasename);
	        	System.out.println("Database "+databasename+" deleted");
	        } 
        	System.out.println("Creating database "+databasename);
        	Statement s = c.createStatement();
        	s.executeUpdate("CREATE DATABASE IF NOT EXISTS "+databasename);
        	System.out.println("Database "+databasename+" created");
        	c.close();
        	conn = DriverManager.getConnection
					("jdbc:mysql://localhost/"+databasename+"?useSSL=false&user="+username+"&password="+password);
        	System.out.println("Running SQL initialisation script");
        	ScriptRunner sr = new ScriptRunner(conn, false, false);
        	Reader reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "/resources/init.sql"));
        	sr.runScript(reader);
        	System.out.println("SQL initialisation script completed");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR, SQL exception");
			e.printStackTrace();
			return false;
		}/** catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR, cannot connect to DB");
			e.printStackTrace();
			
		} **/ catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean insertStatement(String s) {
		try {
			Statement st = conn.createStatement();
			st.executeUpdate(s);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean tableExists(String tablename) {
		DatabaseMetaData md;
		try {
			md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, tablename, null);
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean columnInTableExists(String tablename, String columname) {
		DatabaseMetaData md;
		try {
			md = conn.getMetaData();
			ResultSet rs = md.getColumns(null, null, tablename, columname);
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
