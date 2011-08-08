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

package tikouka.nl.wps.handler;



import org.xml.sax.Attributes;
import tikouka.nl.wps.algorithm.util.RasterTable;
import tikouka.nl.wps.algorithm.util.Table;
/**
 *
 * @author niels
 * derived from a SAXParser example
 */
public class XMLHandler extends XMLContentHandler {

     private RasterTable rastertable;

	public RasterTable getRasterRable() {
		return rastertable;
	}

	protected Object createElement(Object parent, String name, Attributes attributes) throws Exception
	{
		Object element = null;

		if( name.compareToIgnoreCase("rastertable") == 0 )
			element = createRasterTable(attributes);
		else if( name.compareToIgnoreCase("table") == 0 )
			element = createTable((RasterTable)parent, attributes);

		return element;
	}

	private RasterTable createRasterTable(Attributes attributes)
	{
                String name = attributes.getValue("name");
		rastertable = new RasterTable(name);
		return rastertable;
	}

	private Table createTable(RasterTable rastertable, Attributes attributes)
	{
		String id = attributes.getValue("id");
		String key = attributes.getValue("key");
                Integer value = Integer.parseInt(attributes.getValue("value"));

		Table table = new Table(id, key, value);
		rastertable.addTable(table);

		return table;
	}

	protected void processText(Object element,  String str) throws Exception
	{
	}

	protected void processCDATA(Object element, String str) throws Exception {
	}
}
