package fut.server.common;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;


/**
 * The class SQL<br>
 * <p>
 * This class is responsible for SQL connection and queries.
 *
 * <p>
 * Class is constructed as singleton object to ensure that there is only one
 * instance at a time.
 *
 * @author Peter Bielik
 * @since 25.2.2008
 * @version 1.1
 */
public final class Sql {
    private static volatile Sql INSTANCE=null;

    private Connection cn;
    private ResultSet rs;
    private Statement st;

    private String user ="";
    private String url ="";
    private String psswd ="";
    private String jdbc="";
    private boolean autoCommit = false;

    private  Sql() {
    }

    /**
     * Gets the instance of Sql class.
     *
     * @return single instance of this class.
     */
    public static Sql getInstance() {
	if (INSTANCE==null) {//double check idiom
	    synchronized (Sql.class) {
		if (INSTANCE==null) {
		    INSTANCE = new Sql();
		}
	    }
	}
	return INSTANCE;
    }

    /**
     * Attempts to establish a connection to the given database URL.
     *
     * @param url
     *            a database url of the form
     *            <code>jdbc:<em>subprotocol</em>:<em>subname</em></code>
     * @param user
     *            the database user on whose behalf the connection is being made
     * @param psswd
     *            the user's password
     * @param jdbc
     *            the jdbc string for proper driver identification
     * @param autoCommit
     *            Determines wether do autocomit or not
     *
     * @throws SQLException
     *             An exception that provides information on a database access
     *             error or other errors.
     * @throws ClassNotFoundException
     *             Throwen, when no definition for the class with the specified
     *             name could be found.
     */
    public void connect(String jdbc,String url, String user, String psswd, boolean autoCommit) throws ClassNotFoundException, SQLException {

	///jdbc:mysql://[host][,failoverhost...][:port]/[database]
	///	[?propertyName1][=propertyValue1][&propertyName2][=propertyValue2]...

	this.autoCommit  = autoCommit;
	this.jdbc = jdbc;
	this.url = url;
	this.user = user;
	this.psswd = psswd;

	doConnection();

    }

    private void doConnection() throws ClassNotFoundException, SQLException {
	Class.forName(jdbc);
	if ("".equals(user) && "".equals(psswd)) {
	    cn = DriverManager.getConnection(url);
	} else {
	    cn = DriverManager.getConnection(url, user,psswd);
	}
	cn.setAutoCommit(autoCommit);
    }

    /**
     * Gets the username.
     *
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Gets the DB url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the jdbc.
     *
     * @return the jdbc
     */
    public String getJdbc() {
        return jdbc;
    }

    /**
     * Checks if is auto commit.
     *
     * @return true, if is auto commit
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * Checks if there is active connection to the SQL server.
     *
     * @return true - connection is not closed, false otherwise.
     */
    public boolean isConnected() {
	try {
	    if (cn!=null)
		return !cn.isClosed();
	} catch (SQLException e) {
	    //no logging
	}
	return false;
    }

    /**
     * Closes {@link ResultSet} and {@link Statement}.
     */
    public void close() {
	if (rs != null) {
	    try {
		rs.close();
	    } catch (SQLException ignored) {}
	    rs = null;
	}

	if (st != null)
	{  try {
	    st.close();
	} catch (SQLException ignored) {}
	st = null;
	}
    }

    /**
     * Closes connection to the SQL server.
     */
    public void closeConnection() {
	if (cn != null) {
	    try {
		cn.close();
	    } catch (SQLException e) {
		cn = null;
	    }
	}
    }

    /**
     * Gets the connection metadata.
     *
     * @return the connection metadata
     *
     * @throws SQLException
     *             the SQL exception
     */
    public DatabaseMetaData getConnectionMetadata() throws SQLException {
	return (cn != null ? cn.getMetaData() : null);
    }

    /**
     * Checks if given data type is supported by DB.
     *
     * @param type
     *            one of the {@link java.sql.Types}
     * @return true, if data type is supported
     * @throws SQLException
     *             the SQL exception
     */
    public boolean isDataTypeSupported(int type) throws SQLException {
	DatabaseMetaData meta = getConnectionMetadata();

	if (meta != null) {
	    ResultSet rs = meta.getTypeInfo();
	    while(rs.next()) {
		if (rs.getInt("DATA_TYPE") == type) {
		    rs.close();
		    return true;
		}
	    }
	}

	return false;
    }

    /**
     * Closes connection if we are garbage collected.
     */
    @Override
    protected void finalize() {
	close();
	closeConnection();
    }

    /**
     * Gets the last {@link ResultSet}.
     *
     * @return the {@link ResultSet}
     */
    public ResultSet getLastResultSet() {
	return rs;
    }

    /**
     * Gets the last warning from last {@link ResultSet}.
     *
     * @return {@link SQLWarning}
     */
    public SQLWarning getLastWarning() {
	SQLWarning warning=null;
	try {
	    warning =  rs.getWarnings();
	} catch (Exception e) {
	    warning = null;
	}
	return  warning;
    }

    /**
     * Executes the SQL commands.
     *
     * @param query
     *            the SQL query.
     *
     * @return {@link ResultSet} if <code>SELECT</code> was requested,
     *         <code>null</code> otherwise.
     *
     * @throws SQLException
     *             An exception that provides information on a database related
     *             errors.
     */

    public ResultSet execute(String query) throws SQLException {

	close();
	if (cn==null) {
	    throw new IllegalStateException("No connection to the server, unable to do SQL query.");
	}
	st = cn.createStatement();

	//do the query
	if  (st.execute(query)) //if it is a SELECT
	    rs = st.getResultSet(); // returns ResultSet
	else
	    rs = null;//null otherwise

	return rs;
    }

    /**
     * Prepares SQL statement.
     *
     * @param data
     *            SQL query.
     *
     * @return the prepared statement.
     *
     * @throws SQLException
     *             An exception that provides information on a database related
     *             errors.
     */
    public PreparedStatement prepareStatement(String data) throws SQLException {
	if (cn==null ) {
	    throw new IllegalStateException("No connection to the server, unable to do prepared SQL statement.");
	}
	return  cn.prepareStatement(data);
    }
    /**
     * Prepares SQL statement.
     *
     * @param data
     *            SQL query.
     *
     * @return the prepared statement.
     *
     * @throws SQLException
     *             An exception that provides information on a database related
     *             errors.
     */
    public PreparedStatement prepareStatement(String data, int autoGeneratedKeys) throws SQLException {
	if (cn==null ) {
	    throw new IllegalStateException("No connection to the server, unable to do prepared SQL statement.");
	}
	return  cn.prepareStatement(data,autoGeneratedKeys);
    }


    /**
     * Commits all changes made to DB.
     *
     * @throws SQLException
     *             the SQL exception
     */
    public void commit() throws SQLException {
	if (cn==null ) {
	    throw new IllegalStateException("No connection to the server, unable to do prepared SQL statement.");
	}
	    cn.commit();
    }

    /**
     * Rollbacks all changes made to DB.
     *
     * @param sp
     *            the save point. Undoes all changes made after the given
     *            Savepoint object was set. If set to null, undoes all changes
     *            made in the current transaction and releases any database
     *            locks currently held by this Connection object.
     * @throws SQLException
     *             the SQL exception
     */
    public void rollback(Savepoint sp) throws SQLException {
	if (cn==null ) {
	    throw new IllegalStateException("No connection to the server, unable to do prepared SQL statement.");
	}
	if (sp == null)
	    cn.rollback();
	else
	    cn.rollback(sp);
    }

    public void setTransactionIsolationLevel(int level) throws SQLException {
	cn.setTransactionIsolation(level);
    }

    public int getTransactionIsolationLEvel() throws SQLException {
	return cn.getTransactionIsolation();
    }

    public Savepoint createSavePoint(String name) throws SQLException {
	return cn.setSavepoint(name);
    }

    public void ReleaseSavepoint(Savepoint sp) throws SQLException {
	cn.releaseSavepoint(sp);
    }


    /**
     * Gets row count in {@link ResultSet}.
     *
     * @param res
     *            the {@link ResultSet}
     *
     * @return the number of rows.
     *
     * @throws SQLException
     *             An exception that provides information on a database related
     *             errors.
     */
    public static  int getRowCount(ResultSet res) throws SQLException {
	int i=0,j=0;

	    j=res.getRow();
	    res.last();
	    i=res.getRow();
	    res.absolute(j);


	return i;
    }

    /**
     * Escapes SQL commands from SQL-injection attacks.<br>
     * <b>This is very simple method !</b><br>
     * Escaped characteres :
     * <li> ' to ''
     * <li> \\ to \\\\
     *
     * <br>Deleted characters :
     * <li> ;
     * <li> --
     * <li> /&#42
     * <li> &#42/
     * <li> #
     * <li> {
     * <li> }
     * <li> %
     *
     * @param data
     *            The SQL commands.
     *
     * @return escaped SQL commands.
     */

    public static String escape(String data) {
	data = data.replaceAll("'", "''");
	data = data.replace(";", "");

	//eliminate all comments
	data = data.replace("--", ""); //complies with the ANSI/ISO standard for SQL

	data = data.replace("/*", "");//comply with the SQL-99 standard.
	data = data.replace("*/", "");

	data = data.replace("#", "");

	data = data.replace("{", "");//Informix extension to the ANSI/ISO standard.
	data = data.replace("}", "");

	data = data.replace("\\", "\\\\"); // escape '\' to '\\'

	data = data.replace("%", ""); // url decoder

	return data;
    }

}
