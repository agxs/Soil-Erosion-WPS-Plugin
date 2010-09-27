package tikouka.nl.wps.handler;



import org.xml.sax.Attributes;
import tikouka.nl.wps.algorithm.util.RasterTable;
import tikouka.nl.wps.algorithm.util.Table;

//import org.xml.sax.helpers.DefaultHandler;

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
