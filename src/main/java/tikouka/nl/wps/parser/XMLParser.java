/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tikouka.nl.wps.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.apache.log4j.Logger;
import org.n52.wps.io.IStreamableParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
/**
 *
 * @author niels
 */
public class XMLParser extends AbstractXMLParser implements IStreamableParser {

    private static Logger LOGGER = Logger.getLogger(XMLParser.class);



	public XMLParser() {
            super();
	}

        public XMLParser(boolean pReadWPSConfig){
            super(pReadWPSConfig);
        }

    @Override
    public IData parseXML(String gml) {
        //throw new UnsupportedOperationException("Not supported yet.");
        StringBuffer payload = new StringBuffer(gml);

		LiteralStringBinding result = new LiteralStringBinding(payload
				.toString());

		return result;
    }

    @Override
    public IData parseXML(InputStream input) {
        //throw new UnsupportedOperationException("Not supported yet.");
        StringBuffer payload = new StringBuffer("");

		try {
			byte[] buffer = new byte[1];
			int c = input.read(buffer);
			while (c > 0) {
				payload.append(new String(buffer, 0, c));
				c = input.read(buffer);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return parse(payload.toString());
    }

    public Object parseXML(URI uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IData parse(InputStream input, String mimeType) {
		return parseXML(input);
	}

    public IData parse(String input) {

		StringBuffer payload = new StringBuffer(input);

		LiteralStringBinding result = new LiteralStringBinding(payload
				.toString());

		return result;

	}
//	@Override
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = { LiteralStringBinding.class };
		return supportedClasses;
	}

}
