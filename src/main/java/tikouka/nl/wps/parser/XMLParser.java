/*
 * Copyright 2011 by EDINA, University of Edinburgh, Landcare Research
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
