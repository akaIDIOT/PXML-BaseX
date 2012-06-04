package nl.utwente.cs.pxml.transform;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import nl.utwente.cs.pxml.util.ArrayIterator;

public class DocumentTransformer {

	public static final String NS_PREFIX = "p";
	public static final String NS_URI = "http://www.utwente.nl/~keulen/pxml/";

	protected XMLEventFactory events;

	public DocumentTransformer() { // TODO: feed distribution information here
		this.events = XMLEventFactory.newInstance();
	}

	public void transform(XMLEventReader in, XMLEventWriter out) throws DocumentTransformerException {
		try {
			// pass events up to the root element (the first StartElement)
			XMLEvent current = in.nextEvent();
			while (!current.isStartElement()) {
				out.add(current);
				current = in.nextEvent();
			}

			this.events.createNamespace(NS_PREFIX, NS_URI);

			// current is now a StartElement representing the root element, add the namespace to it
			StartElement root = this.addNamespaces(current.asStartElement(), events.createNamespace(NS_PREFIX, NS_URI));
			out.add(root);
			
			// children of root are next, proceed with probability elements insertion
			// TODO
		} catch (XMLStreamException e) {
			throw new DocumentTransformerException("error parsing xml stream: " + e.getMessage(), e);
		}
	}
	
	public StartElement addNamespaces(StartElement element, Namespace... namespaces) {
		return this.events
				.createStartElement(element.getName(), element.getAttributes(), new ArrayIterator(namespaces));
	}

}
