package nl.utwente.cs.pxml.transform;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

/**
 * NamespaceContext implementation lazily delegating queries to a context document.
 * 
 * @author Mattijs Ugen
 */
public class NamespaceResolver implements NamespaceContext {

	protected Document doc;

	/**
	 * Creates a new NamespaceResolver using doc as the source for queries.
	 * 
	 * @param doc
	 *            The Document to use for queries.
	 */
	public NamespaceResolver(Document doc) {
		this.doc = doc;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return doc.lookupNamespaceURI(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return doc.lookupPrefix(namespaceURI);
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		return null;
	}

}
