package xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * With default (javax) XML utilities
 */
public class DefaultXMLSchema {

    public static class XmlErrorHandler implements ErrorHandler {

        private final List<SAXParseException> exceptions;

        public XmlErrorHandler() {
            this.exceptions = new ArrayList<>();
        }

        public List<SAXParseException> getExceptions() {
            return exceptions;
        }

        @Override
        public void warning(SAXParseException exception) {
            exceptions.add(exception);
        }

        @Override
        public void error(SAXParseException exception) {
            exceptions.add(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
            exceptions.add(exception);
        }
    }

    public List<SAXParseException> listParsingExceptions(String xsdPath, String xmlPath) throws IOException, SAXException {
        XmlErrorHandler xsdErrorHandler = new XmlErrorHandler();
        Validator validator = initValidator(xsdPath);
        validator.setErrorHandler(xsdErrorHandler);
        try {
            validator.validate(new StreamSource(getFile(xmlPath)));
        } catch (SAXParseException e) {
            e.printStackTrace();
        }
        xsdErrorHandler.getExceptions().forEach(e -> System.out.println(e.getMessage()));
        return xsdErrorHandler.getExceptions();
    }

    public boolean isValid(String xsdPath, String xmlPath) throws IOException, SAXException {
        Validator validator = initValidator(xsdPath);
        try {
            validator.validate(new StreamSource(getFile(xmlPath)));
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

    private File getFile(String location) {
        return new File(getClass().getClassLoader().getResource(location).getFile());
    }

    private Validator initValidator(String xsdPath) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(getFile(xsdPath));
        Schema schema = factory.newSchema(schemaFile);
        return schema.newValidator();
    }

    public static void main(String... args) {
        // Required files are in the 'resources' folder
        String xsd = "xml/full-person.xsd"; // "xml/person.xsd"; //
        String xml = "xml/person.xml";

        DefaultXMLSchema xmlTool = new DefaultXMLSchema();
        try {
            boolean isValid = xmlTool.isValid(xsd, xml);
            System.out.printf("Validating %s on %s: %b%n", xml, xsd, isValid);
            if (!isValid) {
                final List<SAXParseException> saxParseExceptions = xmlTool.listParsingExceptions(xsd, xml);
                System.out.println("--------------");
                saxParseExceptions.forEach(saxEx -> System.out.println(saxEx.getMessage()));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } finally {
            System.out.println("Done!");
        }
    }
}
