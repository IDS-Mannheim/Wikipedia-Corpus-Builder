package de.mannheim.ids.wiki;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import de.mannheim.ids.transform.Transformer;
import de.mannheim.ids.transform.WikiI5Part;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class TransformerTest {
    
    @Test
    public void testXsltResultArticle () throws Exception {
        Configuration config = WikiI5Converter.createConfig(
                new String[] { "-prop", "dewiki-article.properties" });
        Transformer.resetTransformer(config);
        Statistics statistics = new Statistics();
        I5ErrorHandler errorHandler = new I5ErrorHandler(config);

        String idx = "1";
        String pageId = "6873";
        String xmlPath = idx + "/" + pageId + ".xml";

        Transformer t = new Transformer(config, statistics, errorHandler,
                xmlPath, idx, pageId);

        WikiI5Part wikipart = t.call();
        assertEquals(true, wikipart.isIDSText());
        InputStream is = wikipart.getInputStream();

        Builder builder = new Builder();
        Document doc = builder.build(is);
        Node idsText = doc.query("/idsText").get(0);
        assertEquals("WPD17.10000.06873",
                idsText.query("@id").get(0).getValue());
        assertEquals("de.1802", idsText.query("@n").get(0).getValue());

        Node profileDesc = idsText.query("idsHeader/profileDesc").get(0);
        assertEquals("Enzyklopädie-Artikel",
                profileDesc.query("textDesc/textTypeArt").get(0).getValue());

        Node body = idsText.query("text/body").get(0);
        // text
        assertEquals("0", body.query("div/@n").get(0).getValue());
        assertEquals("template",
                body.query("div/p/gap/@desc").get(0).getValue());
        assertEquals("table", body.query("div/p/gap/@desc").get(1).getValue());
        // header
        assertEquals("2", body.query("div/div/@n").get(0).getValue());
    }


    @Test
    public void testXsltResultTalkPage () throws Exception {
        Configuration config = WikiI5Converter.createConfig(
                new String[] { "-prop", "dewiki-talk.properties" });
        Transformer.resetTransformer(config);
        Statistics statistics = new Statistics();
        I5ErrorHandler errorHandler = new I5ErrorHandler(config);

        String idx = "B";
        String pageId = "8568531";
        String xmlPath = idx + "/" + pageId + ".xml";

        Transformer t = new Transformer(config, statistics, errorHandler,
                xmlPath, idx, pageId);

        WikiI5Part wikipart = t.call();
        assertEquals(true, wikipart.isIDSText());
        InputStream is = wikipart.getInputStream();

        Builder builder = new Builder();
        Document doc = builder.build(is);
        Node idsText = doc.query("/idsText").get(0);
        assertEquals("WDD17.B0085.68531",
                idsText.query("@id").get(0).getValue());
        assertEquals("de.Diskussion:Brian_Connell",
                idsText.query("@n").get(0).getValue());

        Node fileDesc = idsText.query("idsHeader/fileDesc").get(0);
        assertEquals("WDD17/B0085.68531",
                fileDesc.query("titleStmt/textSigle").get(0).getValue());
        assertEquals(
                "WDD17/B0085.68531 Diskussion:Brian Connell, In: Wikipedia "
                        + "- URL:http://de.wikipedia.org/wiki/Diskussion:"
                        + "Brian_Connell: Wikipedia, 2017",
                fileDesc.query("titleStmt/t.title").get(0).getValue());

        Node analytic = fileDesc.query("sourceDesc/biblStruct/analytic").get(0);
        assertEquals("Diskussion:Brian Connell",
                analytic.query("h.title").get(0).getValue());
        assertEquals("Zsasz,  u.a.",
                analytic.query("h.author").get(0).getValue());
        assertEquals("http://de.wikipedia.org/wiki/Diskussion:Brian_Connell",
                analytic.query("imprint/pubPlace/ref/@target").get(0)
                        .getValue());
        assertEquals("8568531", analytic.query("idno").get(0).getValue());

        Node monogr = fileDesc.query("sourceDesc/biblStruct/monogr").get(0);
        assertEquals(
                "Dump file \"dewiki-20170701-pages-meta-current.xml\" "
                        + "retrieved from http://dumps.wikimedia.org",
                monogr.query("edition/further").get(0).getValue());
        assertEquals("http://de.wikipedia.org",
                monogr.query("imprint/pubPlace/ref/@target").get(0).getValue());
        assertEquals("2017", monogr.query("imprint/pubDate[@type=\"year\"]")
                .get(0).getValue());
        assertEquals("07", monogr.query("imprint/pubDate[@type=\"month\"]")
                .get(0).getValue());
        assertEquals("01", monogr.query("imprint/pubDate[@type=\"day\"]").get(0)
                .getValue());

        assertEquals(
                "WDD17/B0085.68531 Wikipedia; Diskussion:Brian Connell, "
                        + "(Letzte Änderung 26.1.2015) 1.7.2017",
                fileDesc.query("sourceDesc/reference[@type=\"short\"]").get(0)
                        .getValue());

        Node profileDesc = idsText.query("idsHeader/profileDesc").get(0);
        assertEquals("2015.01.26",
                profileDesc.query("creation/creatDate").get(0).getValue());

        // same values
        assertEquals("(Letzte Änderung 26.1.2015)",
                profileDesc.query("creation/creatRef").get(0).getValue());
        assertEquals("(Letzte Änderung 26.1.2015)",
                profileDesc.query("creation/creatRefShort").get(0).getValue());

        assertEquals("Diskussion",
                profileDesc.query("textDesc/textTypeArt").get(0).getValue());

        // posting
        Node posting = idsText.query("text/body/div/div/posting").get(0);
        assertEquals("i.8568531_1_1", posting.query("@id").get(0).getValue());
        assertEquals("0", posting.query("@indentLevel").get(0).getValue());
        assertEquals("2015-01-26T18:08+01",
                posting.query("@when-iso").get(0).getValue());

        Node signature = posting.query("p/signed").get(0);
        assertEquals("signed", signature.query("@type").get(0).getValue());
        assertEquals("https://de.wikipedia.org/wiki/Benutzer:Zsasz",
                signature.query("ref/@target").get(0).getValue());
        assertEquals("Zsasz", signature.query("ref/name").get(0).getValue());
        assertEquals("18:08, 26. Jan. 2015 (CET)",
                signature.query("date").get(0).getValue());

    }


    @Test
    public void testHeaderWithinRef () throws Exception {
        Configuration config = WikiI5Converter.createConfig(
                new String[] { "-prop", "enwiki-article.properties" });
        Transformer.resetTransformer(config);
        Statistics statistics = new Statistics();
        I5ErrorHandler errorHandler = new I5ErrorHandler(config);

        String idx = "B";
        String pageId = "260696";
        String xmlPath = idx + "/" + pageId + ".xml";

        Transformer t = new Transformer(config, statistics, errorHandler,
                xmlPath, idx, pageId);

        WikiI5Part wikipart = t.call();
        assertEquals(true, wikipart.isIDSText());
        InputStream is = wikipart.getInputStream();

        Builder builder = new Builder();
        Document doc = builder.build(is);
        Node idsText = doc.query("/idsText").get(0);
        Node item = idsText.query("text/body/div/list/item").get(2);

        assertEquals(2, item.query("ref").size());
    }


    @Test
    public void testHeaderWithinLi () throws ParseException, IOException,
            I5Exception, ValidityException, ParsingException {
        Configuration config = WikiI5Converter.createConfig(
                new String[] { "-prop", "enwiki-article.properties" });
        Transformer.resetTransformer(config);
        Statistics statistics = new Statistics();
        I5ErrorHandler errorHandler = new I5ErrorHandler(config);

        String idx = "L";
        String pageId = "41178893";
        String xmlPath = idx + "/" + pageId + ".xml";

        Transformer t = new Transformer(config, statistics, errorHandler,
                xmlPath, idx, pageId);

        WikiI5Part wikipart = t.call();
        assertEquals(true, wikipart.isIDSText());
        InputStream is = wikipart.getInputStream();
        
//        String line = "";
//        try (BufferedReader bufferedReader = new BufferedReader(
//                new InputStreamReader(is))) {
//            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(line);
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
        

        Builder builder = new Builder();
        Document doc = builder.build(is);
        Node idsText = doc.query("/idsText").get(0);
        Node item = idsText.query("text/body/div/list/item").get(0);
        assertEquals(1, item.query("list/item").size());
    }

}
