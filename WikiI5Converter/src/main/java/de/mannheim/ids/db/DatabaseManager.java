package de.mannheim.ids.db;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * Manages a database connection to the wikipedia database storing language
 * links. The database should be created from a language links dump
 * corresponding to the wikipedia dump used.
 * 
 * @author margaretha
 *
 */
public class DatabaseManager {

	public String[] langCodes = new String[]{"de", "fr", "en"};

	public DataSource poolingDataSource;
	public Connection conn;
	private String langCode;

	public static String langlink;
	public static String articleLink;
	public static String pageId;
	public static String categoryInsert;
	public static String categorySelect;
	public static String englishCategory;
	
	/**
	 * Constructs a DatabaseManager instance and connects to the database based
	 * on the credential information given as variables.
	 * 
	 * @param dbUrl
	 *            a database URL
	 * @param username
	 *            a database username
	 * @param password
	 *            the database password
	 * @param langCode
	 *            2 letter language code
	 * @throws SQLException
	 *             an SQLException
	 */
	public DatabaseManager(String dbUrl, String username, String password,
			String langCode) throws SQLException {
		setDataSource(dbUrl, username, password);
		this.langCode = langCode;
		
		langlink = "SELECT ll_lang, ll_title FROM " + langCode
				+ "_langlinks WHERE ll_from = ?";
		articleLink = "SELECT ll_title FROM " + langCode
				+ "_langlinks WHERE ll_from= ? AND ll_lang= ?";
		pageId = "SELECT page_id FROM " + langCode
				+ "_page where page_title=? AND page_namespace=?";
		categoryInsert = "INSERT INTO " + langCode
				+ "_category(article_id, category_url) VALUES(?,?)";
		categorySelect = "SELECT category_url FROM " + langCode
				+ "_category WHERE article_id=?";
		englishCategory = "SELECT ll_title FROM " + langCode
				+ "_langlinks WHERE ll_from=? AND ll_lang=?";
	}

	private void setDataSource(String url, String username, String password) {
		// try {
		// if (url.contains("mariadb")){
		// Class.forName("com.mariadb.jdbc.Driver");
		// }
		// }
		// catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }
		ConnectionFactory cf = new DriverManagerConnectionFactory(url, username,
				password);
		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, null);
		// pcf.setMaxOpenPrepatedStatements(100);

		ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(
				pcf);
		pcf.setPool(connectionPool);
		poolingDataSource = new PoolingDataSource<>(connectionPool);
	}

	/**
	 * Select all the language links of the given wikipage id.
	 * 
	 * @param ll_from
	 *            the wikipage id
	 * @return a LanguageLinks object
	 * @throws SQLException
	 *             an SQLException
	 * @throws UnsupportedEncodingException
	 *             an UnsupportedEncodingException
	 */
	public LanguageLinks retrieveLanguageLinks(String ll_from)
			throws SQLException, UnsupportedEncodingException {

		if (ll_from == null || ll_from.isEmpty()) {
			throw new IllegalArgumentException(
					"ll_from cannot be null or empty.");
		}

		LanguageLinks langlinks = new LanguageLinks(ll_from);
		String languageCode;
		String pageTitle;

		Connection conn = poolingDataSource.getConnection();
		PreparedStatement preparedStatement = conn.prepareStatement(langlink);
		preparedStatement.setString(1, ll_from);
		ResultSet rs = preparedStatement.executeQuery();

		while (rs.next()) {
			languageCode = rs.getString("ll_lang");
			pageTitle = new String(rs.getBytes("ll_title"), "UTF-8");
			langlinks.getTitleMap().put(languageCode, pageTitle);
		}
		conn.close();
		return langlinks;
	}

	public List<String> retrieveCategoryLinks(String articleTitle) throws SQLException {
		List<String> categoryLinks = new ArrayList<>();
		Connection conn = poolingDataSource.getConnection();
		PreparedStatement preparedStatement = conn
				.prepareStatement(pageId);
		preparedStatement.setString(1, articleTitle);
		preparedStatement.setInt(2, 0);
		ResultSet rs = preparedStatement.executeQuery();
		if (rs.next()) {
			int articleId = rs.getInt("page_id");
			preparedStatement = conn.prepareStatement(categorySelect);
			preparedStatement.setInt(1, articleId);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				categoryLinks.add(rs.getString("category_url"));
			}
		}
		conn.close();
		return categoryLinks;
	}
	
	public String retrieveEnglishCategory(String categoryTitle) throws SQLException {
		String enTitle = null;
		Connection conn = poolingDataSource.getConnection();
		PreparedStatement preparedStatement = conn
				.prepareStatement(pageId);
		preparedStatement.setString(1, categoryTitle);
		preparedStatement.setInt(2, 14);
		ResultSet rs = preparedStatement.executeQuery();
		if (rs.next()) {
			int categoryId = rs.getInt("page_id");
			preparedStatement = conn.prepareStatement(englishCategory);
			preparedStatement.setInt(1, categoryId);
			preparedStatement.setString(2, "en");
			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				enTitle = rs.getString("ll_title");
			}
		}
		conn.close();
		return enTitle;
	}
	
	public LanguageLinks retrieveArticleLinks(String articleTitle)
			throws SQLException, UnsupportedEncodingException {
		if (articleTitle == null || articleTitle.isEmpty()) {
			throw new IllegalArgumentException(
					"Page title cannot be null or empty.");
		}

		Connection conn = poolingDataSource.getConnection();
		PreparedStatement preparedStatement = conn
				.prepareStatement(pageId);
		preparedStatement.setString(1, articleTitle);
		preparedStatement.setInt(2, 0);
		ResultSet rs = preparedStatement.executeQuery();
		LanguageLinks langlinks = null;
		if (rs.next()) {
			int pageId = rs.getInt("page_id");
			langlinks = new LanguageLinks(String.valueOf(pageId));
			String pageTitle;

			for (String langCode : this.langCodes) {
				if (!langCode.equals(this.langCode)) {
					preparedStatement = conn.prepareStatement(articleLink);
					preparedStatement.setInt(1, pageId);
					preparedStatement.setString(2, langCode);
					rs = preparedStatement.executeQuery();
					if (rs.next()) {
						pageTitle = new String(rs.getBytes("ll_title"),
								"UTF-8");
						langlinks.getTitleMap().put(langCode, pageTitle);
					}
				}
			}
		}
		
		conn.close();
		return langlinks;	}

	public void storeCategory(String pageId, String categoryURL)
			throws SQLException {
		if (pageId == null || pageId.isEmpty()) {
			throw new IllegalArgumentException(
					"Page id cannot be null or empty.");
		}
		if (categoryURL == null || categoryURL.isEmpty()) {
			throw new IllegalArgumentException(
					"Category cannot be null or empty.");
		}

		Connection conn = poolingDataSource.getConnection();
		PreparedStatement preparedStatement = conn
				.prepareStatement(categoryInsert);
		preparedStatement.setInt(1, Integer.parseInt(pageId));
		preparedStatement.setString(2, categoryURL);
		
		preparedStatement.executeUpdate();
		conn.close();
	}
	
	public void createCategoryTable() throws SQLException {
		String categoryTable = "CREATE TABLE IF NOT EXISTS "+this.langCode+"_category("
				+ "id INTEGER PRIMARY KEY AUTO_INCREMENT, "
				+ "article_id INTEGER NOT NULL, "
				+ "category_url VARCHAR(250) NOT NULL, "
				+ "UNIQUE INDEX article_category (article_id,category_url));";
		
		Connection conn = poolingDataSource.getConnection();
		conn.createStatement().execute(categoryTable);
		conn.close();
	}
}
