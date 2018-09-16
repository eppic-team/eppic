package eppic.commons.blast;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * An EntityResolver for SAX XML parsing. 
 * To be set in an XMLReader in order to prevent network activity, if 
 * for instance one does not want the DTD to be read from a remote web site.
 * 
 * Example usage:
 * <p><code>
 *   XMLReader parser = XMLReaderFactory.createXMLReader();
 *   parser.setEntityResolver(new BlankingResolver());
 * </code></p>
 *  
 * Taken from http://stackoverflow.com/questions/5883542/disable-xml-validation-based-on-external-dtd-xsd
 * 
 * @author duarte_j
 *
 */
public class BlankingResolver implements EntityResolver
{

    @Override
	public InputSource resolveEntity( String arg0, String arg1 ) throws SAXException,
            IOException
    {

        return new InputSource( new ByteArrayInputStream( "".getBytes() ) );
    }

}