package de.mannheim.ids.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DatabaseManager {
	
	private static final String langlink = "SELECT ll_lang, ll_title FROM langlinks WHERE ll_from = ?";	
	public DataSource poolingDataSource;
	public Connection conn;
	
	public DatabaseManager(String dbUrl, String username, String password) throws SQLException {
		setDataSource(dbUrl, username, password);		
	}
	
	public void setDataSource(String url, String username, String password) {
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		ConnectionFactory cf = new DriverManagerConnectionFactory(url, username, password);
		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, null);
		//pcf.setMaxOpenPrepatedStatements(100);
		
		ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(pcf);		
		pcf.setPool(connectionPool);		
		poolingDataSource = new PoolingDataSource<>(connectionPool);		
	}
		
	public LanguageLinks retrieveLanguageLinks(String ll_from) throws SQLException{
		LanguageLinks langlinks = new LanguageLinks(ll_from);
		String languageCode;
		String pageTitle;
		
		Connection conn = poolingDataSource.getConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(langlink);
		preparedStatement.setString(1, ll_from);
		ResultSet rs = preparedStatement.executeQuery();
		while (rs.next()) {
			languageCode = rs.getString("ll_lang");
			pageTitle = rs.getString("ll_title");
			langlinks.getTitleMap().put(languageCode, pageTitle);		
		}
		conn.close();
		return langlinks;
	}	
}
