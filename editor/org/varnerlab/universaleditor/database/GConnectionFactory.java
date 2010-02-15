package org.varnerlab.universaleditor.database;

//Import statements
import java.util.Hashtable;

import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.Connection;

/* 
 * GConnection - Wrapper around the standard connection object. This class is used by all genencor (java) dbtools apps.
 * @author J.Varner
 */
public class GConnectionFactory extends Object implements IGConnection,IGFactory {
	// Class/instance attributes
	private String _strDriver;                      // Drivername
	private String _strUserName;                    // Connection user name
	private String _strPassword;                    // Password
	private String _strPort;                        // Port information
	private String _strServerName;                  // DB Servername
	private String _strDatabaseName;                // Database name
	private String _strDBType;                      // Vendor/type of database
	private Hashtable _hstConnectionTable;         // Properties object that is build in the setProp's method
	private boolean _isPropsSet;


	// Constructor -- Does nothing (except init);
	public GConnectionFactory(){
		// Initialize me
		init();

		// Setup logging -- grab simzilla's logger
		//_logger=LogManager.getLogManager().getLogger("com.genencor.database");
	}

	// init method
	private void init(){
		// Create new properties object
		_hstConnectionTable=new Hashtable();
	}

	/*
	 * setConnectionProperties - Accepts a string literal and a value and puts them a properties object.
	 */
	public void setProperty(Object key,Object value){
		//System.out.println(key+"="+value);

		// Log the values
		//_logger.fine(key+"="+value);

		_hstConnectionTable.put(key,value);
	}

	/*
	 * buildComponent - Reads the Conection Properties object and returns a database connection.
	 * @return java.sql.Connection
	 * @throws Exception
	 */
	public Object buildComponent() throws Exception {
		return(buildConnection());
	}

	// Private method does all the work...
	private Object buildConnection() throws Exception {
		// Method attributes
		StringBuffer cStringBuffer=new StringBuffer();
		Connection connection=null;

		// Read the connection properties object and cache the values
		_strDriver=(String)_hstConnectionTable.get(IGConnection.DRIVER);
		_strUserName=(String)_hstConnectionTable.get(IGConnection.USERNAME);
		_strPassword=(String)_hstConnectionTable.get(IGConnection.PASSWORD);
		_strPort=(String)_hstConnectionTable.get(IGConnection.PORT);
		_strServerName=(String)_hstConnectionTable.get(IGConnection.SERVERNAME);
		_strDatabaseName=(String)_hstConnectionTable.get(IGConnection.DATABASENAME);
		_strDBType=(String)_hstConnectionTable.get(IGConnection.DBTYPE);


		// Oracle has a different syntax
		if (_strDBType.indexOf("oracle")!=-1) {
			// If we are here, then oracle is in the dbtype
			cStringBuffer.append("jdbc:");
			cStringBuffer.append(_strDBType);
			cStringBuffer.append(":thin:");
			cStringBuffer.append(_strUserName);
			cStringBuffer.append("/");
			cStringBuffer.append(_strPassword);
			cStringBuffer.append("@");
			cStringBuffer.append(_strServerName);
			cStringBuffer.append(":");
			cStringBuffer.append(_strPort);
			cStringBuffer.append(":");
			cStringBuffer.append(_strDatabaseName);

			// This is the old way of doing things...however it works w/no JNDI
			Class.forName(_strDriver).newInstance();

			// Print cStringBuffer
			System.out.println(cStringBuffer.toString());

			// Goto the DriverManager and get a connection
			connection=DriverManager.getConnection(cStringBuffer.toString());
		}
		else {

			// Put the connection bits into the cStringBuffer
			cStringBuffer.append("jdbc:");
			cStringBuffer.append(_strDBType);
			cStringBuffer.append("://");
			cStringBuffer.append(_strServerName);
			cStringBuffer.append(":");
			cStringBuffer.append(_strPort);
			cStringBuffer.append("/");
			cStringBuffer.append(_strDatabaseName);
			cStringBuffer.append("?user=");
			cStringBuffer.append(_strUserName);
			cStringBuffer.append("&");
			cStringBuffer.append("password=");
			cStringBuffer.append(_strPassword);

			// This is the old way of doing things...however it works w/no JNDI
			Class.forName(_strDriver).newInstance();

			// Print cStringBuffer
			System.out.println(cStringBuffer.toString());

			//_logger.fine(cStringBuffer.toString());

			// Goto the DriverManager and get a connection
			connection=DriverManager.getConnection(cStringBuffer.toString());
		}



		// return the connection to the caller -- later we will add code
		// to config the connection here...
		return(connection);
	}
}
