package nl.utwente.cs.pxml.transform;

import static nl.utwente.cs.pxml.ProbabilityNodeType.EVENTS;
import static nl.utwente.cs.pxml.ProbabilityNodeType.INDEPENDENT;
import static nl.utwente.cs.pxml.ProbabilityNodeType.MUTEX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

	protected XPath xpath;

	public DocumentTransformer() {
		this.xpath = XPathFactory.newInstance().newXPath();
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

			// find all the nodes in the document element (make sure to not wrap the root)
			NodeList nodes = (NodeList) xpath.evaluate("//*", doc.getDocumentElement(), XPathConstants.NODESET);
			int length = nodes.getLength();
			int numVariables = this.determineNumVariables(length);
			System.out.println("  document has " + length + " cadidate nodes");

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
				// (appending a node will move it if it is already in the DOM (TODO: check this))
				for (Node node : selected) {
					pNode.appendChild(node);
				}
			}

			// add the used random variables to the document
			Node varList = doc.createElementNS(NS_URI, NS_PREFIX + ":vars");
			for (int i = 0; i < numVariables; i++) {
				Element var = doc.createElementNS(NS_URI, NS_PREFIX + ":var" + i);
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
		Node node = origin.createElementNS(NS_URI, NS_PREFIX + ":" + type.nodeName);

		// return the newly created node
		return node;
	}

	public static void main(String... args) {
		try {
			Document input = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new File("/tmp/input.xml"));
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
