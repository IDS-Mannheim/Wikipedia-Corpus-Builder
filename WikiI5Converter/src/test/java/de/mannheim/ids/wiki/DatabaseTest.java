package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import de.mannheim.ids.db.DatabaseManager;

public class DatabaseTest {

	@Test
	public void testUniqueCategory()
			throws ParseException, IOException, SQLException {

		WikiI5Converter converter = new WikiI5Converter();
		Configuration config = converter.createConfig(
				new String[]{"-prop", "dewiki-article.properties",
						"-storeCategories"});

		Connection conn = createConnection(config);
		Statement s = conn.createStatement();
		s.execute("drop table if exists de_category");
		conn.commit();
		
		DatabaseManager dbManager = new DatabaseManager(config.getDatabaseUrl(),
				config.getDatabaseUsername(), config.getDatabasePassword(),
				config.getLanguageCode());
		
		dbManager.createCategoryTable();
		dbManager.storeCategory("1",
				"https://de.wikipedia.org/wiki/Kategorie:FVp-Mitglied");
		dbManager.storeCategory("1",
				"https://de.wikipedia.org/wiki/Kategorie:FVP-Mitglied");
		dbManager.storeCategory("1",
				"https://de.wikipedia.org/wiki/Kategorie:FVP-Mitglied");
		
		s = conn.createStatement();
		ResultSet rs = s.executeQuery("select count (*) from de_category where article_id=1");
		rs.next();
		assertEquals(2, rs.getInt(1));
		
	}
	
	private Connection createConnection(Configuration config) throws SQLException {
		return DriverManager.getConnection(config.getDatabaseUrl(),
				config.getDatabaseUsername(), config.getDatabasePassword());
	}
}
