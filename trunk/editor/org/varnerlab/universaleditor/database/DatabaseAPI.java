/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.varnerlab.universaleditor.database;

// import statements -
import java.util.*;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import org.varnerlab.universaleditor.service.PublishService;



/**
 *
 * @author jeffreyvarner
 */
public class DatabaseAPI {
	// Class/instance attributes -
	private Hashtable _propTable = new Hashtable();
	//private String factory="com.genencor.database.GConnectionFactory";
	//private String handler="com.genencor.database.GDefaultHandler";
	private IGFactory test=null;
	private Connection connection=null;
	private Statement stmt=null;


	public void setProperty(Object key, Object val)
	{
		_propTable.put(key, val);
	}

	public Object getProperty(Object key)
	{
		return(_propTable.get(key));
	}



	public boolean insertUserInformation(String strUser,String strPassword) throws Exception
	{
		// Method attributes -
		boolean _hasBeenInserted = false;
		StringBuffer buffer = new StringBuffer();

		// Build the insert statement -
		buffer.append("INSERT INTO user_table(username,password) VALUES ('");
		buffer.append(strUser);
		buffer.append("','");
		buffer.append(strPassword);
		buffer.append("');");

		// Go...
		_hasBeenInserted = stmt.execute(buffer.toString());


		// return the flag -
		return(_hasBeenInserted);
	}

	public boolean insertProjectInformation(String strUserName,String strSessionID,String strProjectName) throws Exception
	{
		// Method attributes -
		boolean _hasBeenInserted = false;
		StringBuffer buffer = new StringBuffer();

		// Build the insert statement -
		buffer.append("INSERT INTO user_project_table(username,sessionid,projectname) VALUES ('");
		buffer.append(strUserName);
		buffer.append("','");
		buffer.append(strSessionID);
		buffer.append("','");
		buffer.append(strProjectName);
		buffer.append("');");

		// Go...
		_hasBeenInserted = stmt.execute(buffer.toString());


		// return the flag -
		return(_hasBeenInserted);
	}



	public boolean insertJobRecord(String strUser,String strJobName,String strJobDescription,String sessionId) throws Exception
	{
		// Method attributes -
		boolean _isRecordInserted = true;
		String strUserID = "";
		StringBuffer buffer = new StringBuffer();

		// Get the user id from the db -
		strUserID = this.getUserID(strUser);

		// Formulate the query -
		buffer.append("INSERT INTO model_gen_table (userid,model_location,model_name,model_notes) VALUES ('");
		buffer.append(strUserID);
		buffer.append("','");
		buffer.append(sessionId);
		buffer.append("','");
		buffer.append(strJobName);
		buffer.append("','");
		buffer.append(strJobDescription);
		buffer.append("');");

		// Excute the command -
		stmt.execute(buffer.toString());

		// return the flag -
		return(_isRecordInserted);
	}


	private String getUserID(String strUser) throws Exception
	{
		// Method attributes -
		String strTmp = "";
		StringBuffer buffer = new StringBuffer();
		ResultSet rs = null;

		// Ok, we need to lookup the userid -
		buffer.append("SELECT userid FROM user_table WHERE username='");
		buffer.append(strUser);
		buffer.append("';");

		// Ok, lets do this -
		stmt.execute(buffer.toString());

		// Get the ResultSet -
		rs = stmt.getResultSet();

		// Ok, we need to the password -
		while (rs.next())
		{
			// Get the string -
			strTmp = rs.getString("userid");
		}

		System.out.println("QUERY - "+buffer.toString());
		System.out.println("UserID = "+strTmp);


		// Return to caller -
		return(strTmp);
	}


	public Hashtable getUserProjects(String strUserName) throws Exception
	{
		// Method attributes -
		Hashtable translationTable = new Hashtable();
		StringBuffer buffer = new StringBuffer();
		ResultSet rs = null;
		String strSessionID = "";
		String strProjectName = "";

		// Ok, we need to lookup the userid -
		buffer.append("SELECT sessionid,projectname FROM user_project_table WHERE username='");
		buffer.append(strUserName);
		buffer.append("';");

		PublishService.submitData(buffer.toString());


		// Ok, lets do this -
		stmt.execute(buffer.toString());

		// Get the ResultSet -
		rs = stmt.getResultSet();

		// Ok, we need to the password -
		while (rs.next())
		{
			// Get the string -
			strSessionID = rs.getString("sessionid");
			strProjectName = rs.getString("projectname");

			PublishService.submitData("Getting (sessionid,projectname) = ("+strSessionID+","+strProjectName+")");

			// load the translation table -
			translationTable.put(strSessionID, strProjectName);
		}

		// return the vector -
		return(translationTable);
	}

	public Vector getOldJobs(String strUser) throws Exception
	{
		// Method attributes -
		StringBuffer buffer = new StringBuffer();
		StringBuffer query = new StringBuffer();
		ResultSet rs = null;
		ResultSet rsQuery = null;
		String strUserId = "";
		String strTmp = "";
		Vector<DataItem> vecJobs = new Vector<DataItem>();

		// Ok, we need to lookup the userid -
		buffer.append("SELECT userid FROM user_table WHERE username='");
		buffer.append(strUser);
		buffer.append("';");

		// Ok, lets do this -
		stmt.execute(buffer.toString());

		// Get the ResultSet -
		rs = stmt.getResultSet();

		// Ok, we need to the password -
		while (rs.next())
		{
			// Get the string -
			strUserId = rs.getString("userid");
		}

		// Ok, build the buffer for the job query on the db -
		query.append("SELECT * FROM model_gen_table WHERE userid='");
		query.append(strUserId);
		query.append("';");

		// Ok, lets do this -
		stmt.execute(query.toString());

		// Get the ResultSet -
		rsQuery = stmt.getResultSet();

		System.out.println("QUERY - "+query.toString());
		System.out.println("UserID = "+strUserId);

		// Ok, we need to the password -
		while (rsQuery.next())
		{

			// Create a new DataItem -
			DataItem data_item = new DataItem();


			// Get the string -
			strTmp = rsQuery.getString("model_location");
			data_item.setProperty("MODEL_LOCATION", strTmp);
			data_item.setProperty("USER_ID", strUserId);

			// Get name -
			strTmp = rsQuery.getString("model_name");
			data_item.setProperty("MODEL_NAME", strTmp);

			// Get notes -
			strTmp = rsQuery.getString("model_notes");
			data_item.setProperty("MODEL_NOTES", strTmp);


			vecJobs.add(data_item);
		}


		// return statements -
		return(vecJobs);
	}



	public boolean checkUserInformation(String strUser,String strPassword) throws Exception
	{
		// Method attributes -
		boolean _isUserInfoOk = false;
		StringBuffer buffer = new StringBuffer();
		ResultSet rs = null;
		String tmpString = "";

		// Ok, we need to formulate the query -
		buffer.append("SELECT password FROM user_table WHERE username='");
		buffer.append(strUser);
		buffer.append("';");

		System.out.println("Im hitting the db with - "+buffer.toString());

		// Ok, lets do this -
		stmt.execute(buffer.toString());

		// Get the ResultSet -
		rs = stmt.getResultSet();

		// Ok, we need to the password -
		while (rs.next())
		{
			// Get the string -
			tmpString = rs.getString("password");
		}

		System.out.println("strPassword = "+strPassword+" db returned - "+tmpString);

		// Do a comparison -
		if (strPassword.equalsIgnoreCase(tmpString))
		{
			_isUserInfoOk = true;
		}

		// return the flag -
		return(_isUserInfoOk);
	}

	public void loadConnection() throws Exception
	{
		// Method attributes -
		String strConfigXMLFile = "";

		try {

			/*
        	// Load the config xml file -
            strConfigXMLFile = (String)getProperty("DATABASE_CONFIG_FILE");

            // Launch the test -- read the config file
            test=GBuilder.buildFactory(factory,handler,strConfigXMLFile);
			 */

			// Ok, so we are going to do some other crazy shit to load the db connection -
			test = (IGFactory)getProperty("CONNECTION_FACTORY");
			
			// all the options should be loaded -
			

			// Check to see if test is null
			if (test==null){
				System.out.println("Test failed - NULL factory");
			}
			else 
			{
				System.out.println("Test passed! :) - Ive constructed the builder.");

				// If we get to here then the factory is not null,but
				// it may not be correctly config'd. This is the next test.
				connection=(Connection)test.buildComponent();

				// Get the metadata from the database and display
				DatabaseMetaData mData=connection.getMetaData();
				//String test="INSERT INTO test_table (test_string) VALUES (\"This is a test - spank you.\")";
				//Statement stmt=connection.createStatement();
				//System.out.println("Did the line get inserted? "+stmt.execute(testInsert));


				// display some attributes of the connection
				System.out.println("Drivername - "+mData.getDriverName());
				System.out.println("Driver version - "+mData.getDriverName());
				System.out.println("Database product name - "+mData.getDatabaseProductName());
				System.out.println("Database version - "+mData.getDatabaseProductVersion()); 


				// Create statement -
				stmt = connection.createStatement();
			}

		}
		catch (Exception error)
		{
			error.printStackTrace();
		}
	}



}
