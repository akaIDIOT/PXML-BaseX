package nl.utwente.cs.pxml.transform;

import static nl.utwente.cs.pxml.ProbabilityNodeType.EVENTS;
import static nl.utwente.cs.pxml.ProbabilityNodeType.EXPLICIT;
import static nl.utwente.cs.pxml.ProbabilityNodeType.INDEPENDENT;
import static nl.utwente.cs.pxml.ProbabilityNodeType.MUTEX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import nl.utwente.cs.pxml.ProbabilityNodeType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DocumentTransformer {

	public static final String NS_PREFIX = "p";
	public static final String NS_URI = "http://www.utwente.nl/~keulen/pxml/";

	// how often probability nodes will occur (0.0: never, 1.0: at every chance, taken from XMLToPXMLTransformer.java)
	protected float pNodesOccurrence = 0.2f;
	// array of probability node types (more occurrences will make the type more likely to be picked, taken from
	// XMLToPXMLTransformer.java)
	protected ProbabilityNodeType[] pNodeDistribution = {
			MUTEX, MUTEX, MUTEX, MUTEX, INDEPENDENT, INDEPENDENT, INDEPENDENT, INDEPENDENT, EVENTS
	};
	// the number of random variables relative to the expected number of EVENT-type pNodes
	protected float pVariablesRatio = 0.1f;

	// TODO: incorporate _ratioExpSubsets, and _maxExpSubsetsPwr from XMLToPXMLTransformer.java

	public DocumentTransformer() {
	}

	public DocumentTransformer(float pNodesOccurrence, ProbabilityNodeType[] pNodeDistribution) {
		this();
		this.pNodesOccurrence = pNodesOccurrence;
		this.pNodeDistribution = pNodeDistribution;
	}

	public void transform(Document doc) throws DocumentTransformerException {
		try {
			// add the pxml namespace to the document
			doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + NS_PREFIX, NS_URI);

			// create a new xpath object (...)
			XPath xpath = XPathFactory.newInstance().newXPath();
			// (...) and make sure it knows about the namespaces used in the document
			xpath.setNamespaceContext(new NamespaceResolver(doc));

			// find all the nodes in the document element (make sure to not wrap the root (TODO: does this work?))
			NodeList nodes = (NodeList) xpath.evaluate("//*", doc.getDocumentElement(), XPathConstants.NODESET);
			int length = nodes.getLength();
			System.out.println("  document has " + length + " cadidate nodes");
			int numVariables = this.determineNumVariables(length);
			System.out.println("  will use " + numVariables + " random variables");

			// select length * occurrence nodes at random
			for (int i = 0, max = (int) (length * this.pNodesOccurrence); i < max; i++) {
				Node el = nodes.item((int) (Math.random() * length));
				// select a random number of preceding/following siblings to wrap together
				List<Node> selected = this.selectNodes(el);
				// create a pNode to wrap the selected elements with (make sure to create exactly enough attributes)
				Node pNode = this.createPNode(doc, selected.size(), numVariables);
				// replace the first selected node with the newly created pNode
				Node firstSelected = selected.get(0);
				firstSelected.getParentNode().replaceChild(pNode, firstSelected);
				// attach all the selected nodes to the pNode now in the document
				// (appending a node will move it if it is already in the DOM
				for (Node node : selected) {
					pNode.appendChild(node);
				}
			}

			// fetch all pNode type elements from the document and add attributes (due to DOM modification, this can not
			// be done when inserting)
			NodeList independents = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + INDEPENDENT.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + independents.getLength() + " independent pNodes as <" + NS_PREFIX + ":" + INDEPENDENT.nodeName + ">");
			for (int i = 0, max = independents.getLength(); i < max; i++) {
				Element pNode = (Element) independents.item(i);
				this.insertIndependantAttributes(doc, pNode, pNode.getChildNodes().getLength()); // TODO: count only the
																									// actual elements?
			}

			NodeList mutexes = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + MUTEX.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + mutexes.getLength() + " mutex pNodes as <" + NS_PREFIX + ":" + MUTEX.nodeName + ">");
			for (int i = 0, max = mutexes.getLength(); i < max; i++) {
				Element pNode = (Element) mutexes.item(i);
				this.insertMutexAttributes(doc, pNode, pNode.getChildNodes().getLength()); // TODO: count only the
																							// actual elements?
			}

			NodeList explicits = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + EXPLICIT.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + explicits.getLength() + " explicit pNodes as <" + NS_PREFIX + ":" + EXPLICIT.nodeName + ">");
			for (int i = 0, max = explicits.getLength(); i < max; i++) {
				Element pNode = (Element) explicits.item(i);
				this.insertExplicitAttributes(doc, pNode, pNode.getChildNodes().getLength()); // TODO: count only the
																								// actual elements?
			}

			NodeList events = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + EVENTS.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + events.getLength() + " event conjunction pNodes as <" + NS_PREFIX + ":" + EVENTS.nodeName + ">");
			for (int i = 0, max = events.getLength(); i < max; i++) {
				Element pNode = (Element) events.item(i);
				this.insertEventsAttributes(doc, pNode, numVariables);
			}

			// add the used random variables to the document
			Node varList = doc.createElementNS(NS_URI, NS_PREFIX + ":vars");
			for (int i = 0; i < numVariables; i++) {
				Element var = doc.createElementNS(NS_URI, NS_PREFIX + ":var-" + i);
				double probability = Math.random();
				var.setAttributeNS(NS_URI, NS_PREFIX + ":val-0", "" + (1.0 - probability));
				var.setAttributeNS(NS_URI, NS_PREFIX + ":val-1", "" + probability);
				varList.appendChild(var);
			}
			doc.getDocumentElement().appendChild(varList);
		} catch (XPathException e) {
			throw new DocumentTransformerException("Unexpected XPath error while transforming: " + e.getMessage(), e);
		}
	}

	protected int determineNumVariables(int numNodes) {
		// count the number of times EVENTS occurs in the distribution
		int occurrence = 0;
		for (ProbabilityNodeType type : this.pNodeDistribution) {
			if (type == EVENTS) {
				occurrence++;
			}
		}

		// calculate the number of variables from the number of expected EVENTS-type pNodes
		return (int) Math.max(numNodes * pNodesOccurrence * ((float) occurrence / this.pNodeDistribution.length)
				* this.pVariablesRatio, 1);
	}

	protected List<Node> selectNodes(Node el) {
		// default to child index 0 (...)
		int index = 0;
		// (...) but try to find the actual child index
		NodeList children = el.getParentNode().getChildNodes();
		for (int i = 0, max = children.getLength(); index == 0 && i < max; i++) {
			if (children.item(i) == el) {
				// el found, update index
				index = i;
			}
		}

		// determine the number of siblings to select (× 2 to get the *average* at pNodesSiblings)
		int numSiblings = (int) (Math.random() * children.getLength());
		// make sure to not select too many elements but at least one
		numSiblings = Math.max(Math.min(numSiblings, children.getLength()), 1);

		// determine the starting index (but it should be at least zero)
		int startIndex = Math.max(index - (numSiblings / 2), 0);
		int endIndex = Math.min(startIndex + numSiblings, children.getLength() - 1);

		// TODO: fix end - start not being equal to num?

		// gather the actual nodes
		List<Node> selected = new ArrayList<Node>(numSiblings);
		for (int i = startIndex; i <= endIndex; i++) {
			selected.add(children.item(i));
		}
		// return the selected nodes
		return selected;
	}

	protected Node createPNode(Document origin, int numChilds, int numVariables) {
		// select a type at random
		ProbabilityNodeType type = this.pNodeDistribution[(int) (Math.random() * this.pNodeDistribution.length)];
		// create a new node of the required type
		Element pNode = origin.createElementNS(NS_URI, NS_PREFIX + ":" + type.nodeName);
		// return the newly created node
		return pNode;
	}

	protected void insertIndependantAttributes(Document origin, Element pNode, int numChilds) {
		for (int i = 1; i <= numChilds; i++) {
			// add a p:child-i probability for each child
			pNode.setAttributeNS(NS_URI, NS_PREFIX + ":child-" + i, "" + Math.random());
		}
	}

	protected void insertMutexAttributes(Document origin, Element pNode, int numChilds) {
		// TODO: create p-distribution for num + 1 items
		double[] distribution = new double[numChilds + 1];

		// add probability for no child
		pNode.setAttributeNS(NS_URI, NS_PREFIX + ":none", "" + distribution[0]);
		for (int i = 1; i <= numChilds; i++) {
			// add probability for child i
			pNode.setAttributeNS(NS_URI, NS_PREFIX + ":child-" + i, "" + distribution[i]);
		}
	}

	protected void insertExplicitAttributes(Document origin, Element pNode, int numChilds) {
		// TODO
	}

	protected void insertEventsAttributes(Document origin, Element pNode, int numVariables) {
		// determine number of vars to use (max log_2(total vars), min 1)
		int numUsed = 1 + (int) (Math.random() * Math.log(numVariables) / Math.log(2));
		
		// create list of available variables
		List<Integer> toUse = new ArrayList<Integer>(numVariables);
		for (int i = 0; i < numVariables; i++) {
			toUse.add(i);
		}
		// randomize the list
		Collections.shuffle(toUse);
		for (int i = 0; i < numUsed; i++) {
			// add 'requirement' for a variable to be either true or false (using the numUsed first items in toUse)
			pNode.setAttributeNS(NS_URI, NS_PREFIX + ":val-" + toUse.get(i), Math.random() > 0.5 ? "1" : "0");
		}
	}

	public static void main(String... args) {
		try {
			Document input = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new File("/home/akaidiot/Documents/Projects/PXML-BaseX/res/data/XMark-D.xml"));
			new DocumentTransformer().transform(input);
			TransformerFactory.newInstance().newTransformer()
					.transform(new DOMSource(input), new StreamResult(new File("/tmp/output.xml")));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (DocumentTransformerException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
	}

}
