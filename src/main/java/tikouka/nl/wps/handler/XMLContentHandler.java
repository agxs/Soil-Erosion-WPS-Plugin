package tikouka.nl.wps.handler;

import java.io.CharArrayWriter;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
/**
 *
 * @author niels
 * derived from a SAXParser example
 */
public abstract class XMLContentHandler extends DefaultHandler2
{
	/* serves as context container while building a data structure top down */
	private Stack valueStack = new Stack();

	private boolean readingCDATA = false;
	private CharArrayWriter text = new CharArrayWriter();
	private CharArrayWriter cdata = new CharArrayWriter();

	private boolean isValueStackEmpty() {
		return valueStack.isEmpty();
	}

	private void push(Object element) {
		valueStack.push(element);
	}

	private Object pop() {
		return valueStack.pop();
	}

	private Object peek() {
		return valueStack.peek();
	}

	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException
	{
		try {
			if( isValueStackEmpty() )
				push(createElement(null, name, attributes));
			else
				push(createElement(peek(), name, attributes));

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SAXException("", ex);
		}
	}

	/**
	 * Subclasses provide an implementation for this.
	 *
	 * @param parent		Represents the parent element created before
	 * @param name			Represents the name of the XML element.
	 * @param attributes	Represents the attributes of the XML element
	 * @return
	 * @throws Exception
	 */
	abstract protected Object createElement(Object parent, String name, Attributes attributes) throws Exception;

	public void endElement(String uri, String localName, String name) throws SAXException
	{
		try {
			/* let the element store the text */
			processText(peek(), text.toString());
			text.reset();

			/* pop the element from the stack */
			pop();

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new SAXException("", ex);
		}
	}

	abstract protected void processText(Object element, String str) throws Exception;

	public void endCDATA() throws SAXException
	{
		try {
			processCDATA(peek(), cdata.toString());
		} catch(Exception ex) {
			ex.printStackTrace();
			throw new SAXException("", ex);
		}

		readingCDATA = false;
		cdata.reset();
	}

	abstract protected void processCDATA(Object element, String str) throws Exception;

	public void startCDATA() throws SAXException {
		readingCDATA = true;
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if( readingCDATA )
			cdata.write(ch, start, length);
		else
			text.write(ch, start, length);
	}
}