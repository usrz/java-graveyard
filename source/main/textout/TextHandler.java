package textout;

import java.io.Writer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TextHandler extends DefaultHandler {

    private Locator locator = null;
    private TextWriter output = null;
    private Writer writer = null;
    private Style style = null;

    public TextHandler() {
        super();
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Writer getWriter() {
        return (writer);
    }

    @Override
    public void setDocumentLocator (Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument ()
    throws SAXException {
        try {
            if (writer == null) {
                throw new NullPointerException("Output not specified");
            }
            output = new TextWriter(writer);
            style = new Document(null);
        } catch (Exception e) {
            throw new TextException("Processing document start", e,
                    locator);
        }
    }

    @Override
    public void endDocument()
    throws SAXException {
        try {
            output.close(false);
            output = null;
            style = null;
        } catch (Exception e) {
            throw new TextException("Processing document end", e,
                    locator);
        }
    }

    @Override
    public void startElement (String uri, String loc, String qn,
            Attributes attributes)
    throws SAXException {
        try {
            output.divide();

            if (loc.equals("document")) {
                Document document = new Document(style);
                style = document.processAttributes(attributes);
                output.separate();
            } else if (loc.equals("paragraph")) {
                Paragraph paragraph = new Paragraph(style);
                style = paragraph.processAttributes(attributes);
                if (paragraph.isSeparated()) output.separate();
            } else if (loc.equals("box")) {
                Box box = new Box(style);
                style = box.processAttributes(attributes);
                if (box.isSeparated()) output.separate();
                output.fill(box.applyTop(output));
            } else if (loc.equals("list")) {
                List list = new List(style);
                style = list.processAttributes(attributes);
                if (list.isSeparated()) output.separate();
            } else if (loc.equals("item")) {
                Item item = new Item(style);
                style = item.processAttributes(attributes);
                if (item.isSeparated()) output.separate();
            } else if (loc.equals("rule")) {
                Rule rule = new Rule(style);
                style = rule.processAttributes(attributes);
                if (rule.isSeparated()) output.separate();
            } else if (loc.equals("break")) {
                Break brk = new Break(style);
                style = brk.processAttributes(attributes);
                if (brk.isSeparated()) output.separate();
            } else {
                throw new SAXException("Invalid tag");
            }
            style.apply(output);
        } catch (Exception e) {
            throw new TextException("Processing start tag <" + loc + ">", e,
                    locator);
        }
    }

    @Override
    public void endElement (String uri, String loc, String qn)
    throws SAXException {
        try {
            Style parent = style.getParent();
            output.divide();

            if (loc.equals("document")) {
                parent.apply(output);
                output.separate();
            } else if (loc.equals("paragraph")) {
                Paragraph paragraph = (Paragraph)style;
                parent.apply(output);
                if (paragraph.isSeparated()) output.separate();
            } else if (loc.equals("box")) {
                Box box = (Box)style;
                output.fill(box.applyBottom(output));
                parent.apply(output);
                if (box.isSeparated()) output.separate();
            } else if (loc.equals("list")) {
                List list = (List)style;
                parent.apply(output);
                if (list.isSeparated()) output.separate();
            } else if (loc.equals("item")) {
                Item item = (Item)style;
                parent.apply(output);
                if (item.isSeparated()) output.separate();
            } else if (loc.equals("rule")) {
                Rule rule = (Rule)style;
                output.fill(rule.getStyle());
                parent.apply(output);
                if (rule.isSeparated()) output.separate();
            } else if (loc.equals("break")) {
                Break brk = (Break)style;
                parent.apply(output);
                if (brk.isSeparated()) output.separate();
            }

            style = parent;
        } catch (Exception e) {
            throw new TextException("Processing end tag </" + loc + ">", e,
                locator);
        }
    }

    @Override
    public void characters (char ch[], int start, int length)
    throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (Exception e) {
            throw new TextException("Processing characters", e, locator);
        }
    }

    public static void main(String arg[])
    throws Throwable {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        TextHandler handler = new TextHandler();
        handler.setWriter(new java.io.OutputStreamWriter(System.out));
        try {
            parser.parse(System.in, handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
