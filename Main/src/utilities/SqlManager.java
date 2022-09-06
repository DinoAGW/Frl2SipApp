package utilities;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public enum SqlManager {
	INSTANCE;
	
	private static final String sqlConn = "jdbc:h2:file:".concat(Drive.dbPath);
	private static Connection connection;
	
	private static final String tabelle = "idTable";
	private static final String columns = "( id VARCHAR(10), found DATE )";
	
	static {
		try {
			connection = DriverManager.getConnection(sqlConn);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to initiate SQL connection", e);
		}
		try {
			INSTANCE.executeUpdate("CREATE TABLE IF NOT EXISTS " + tabelle + columns + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}
	
	public void loescheTabelle() {
		System.out.println("L�sche Datenbank...");
		try {
			INSTANCE.executeUpdate("DROP TABLE IF EXISTS " + tabelle);
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to delete Tables", e);
		}
		try {
			INSTANCE.executeUpdate("CREATE TABLE " + tabelle + columns + ";");
		} catch (SQLException e) {
			throw new IllegalStateException("Failed to create Tables", e);
		}
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		ResultSet resultSet = null;
		Statement statement = SqlManager.connection.createStatement();
		resultSet = statement.executeQuery(sql);
		return resultSet;
	}

	public ResultSet executePreparedSql(String sql) throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement prepsInsertProduct = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		prepsInsertProduct.execute();
		// Retrieve the generated key from the insert.
		resultSet = prepsInsertProduct.getGeneratedKeys();
		return resultSet;
	}

	public int executeUpdate(String sql) throws SQLException {
		Statement statement = SqlManager.connection.createStatement();
		int ret = statement.executeUpdate(sql);
		return ret;
	}
	
	public Connection getConnection() {
		return SqlManager.connection;
	}

	public String getDbFilepath() {
		return Drive.dbPath;
	}
}