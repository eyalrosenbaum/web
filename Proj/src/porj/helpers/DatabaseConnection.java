package porj.helpers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;


public class DatabaseConnection {
	private ServletContext cntx;
	private BasicDataSource ds;

	private DatabaseConnection(ServletContext cntx) throws NamingException, SQLException {
		super();
		this.cntx = cntx;
		Context context = new InitialContext();
		this.ds = (BasicDataSource)context.lookup("java:comp/env/jdbc/projDatasource");
	//			cntx.getInitParameter(AppConstants.DB_NAME) + AppConstants.OPEN);
	}
	
	public static void createDB(ServletContext cntx) throws NamingException, SQLException{
		if (AppVariables.db == null)
			AppVariables.db = new DatabaseConnection(cntx);
		
	}
	public Connection getConnection() throws SQLException{
		Connection conn = this.ds.getConnection();
		return conn;
	}
}
