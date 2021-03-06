package porj.helpers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
/**
 * 
 * DatabaseConnection - class that is a helper class to connect to database
 *
 */

public class DatabaseConnection {
	private ServletContext cntx;
	public BasicDataSource ds;

	private DatabaseConnection(ServletContext cntx) throws NamingException, SQLException {
		super();
		this.cntx = cntx;
		Context context = new InitialContext();
		this.ds = (BasicDataSource)context.lookup(
				cntx.getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
		
	}
	
	public static void createDB(ServletContext cntx) throws NamingException, SQLException{
		if (AppVariables.db == null)
			AppVariables.db = new DatabaseConnection(cntx);
		
	}
	public Connection getConnection() throws SQLException{
		
		Connection conn = ds.getConnection();
		return conn;
	}
}
