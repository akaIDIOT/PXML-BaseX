package nl.utwente.cs.pxml.transform.doc;

import static nl.utwente.cs.pxml.ProbabilityNodeType.EVENTS;
import static nl.utwente.cs.pxml.ProbabilityNodeType.EXPLICIT;
import static nl.utwente.cs.pxml.ProbabilityNodeType.INDEPENDENT;
import static nl.utwente.cs.pxml.ProbabilityNodeType.MUTEX;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
import nl.utwente.cs.pxml.util.CollectionUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class DocumentTransformer {

	// the prefix used for the probability namespace
	@Parameter(names = "--ns-prefix", description = "namespace prefix to use")
	public static final String NS_PREFIX = "p";
	// the uri used for the probability namespace
	@Parameter(names = "--ns-uri", description = "namespace uri to use")
	public static final String NS_URI = "http://www.cs.utwente.nl/~keulen/pxml";

	// how often probability nodes will occur (0.0: never, 1.0: at every chance, taken from XMLToPXMLTransformer.java)
	@Parameter(names = "--pnodes", description = "probability of pnode insertion")
	protected float pNodesOccurrence = 0.2f;
	// array of probability node types (more occurrences will make the type more likely to be picked, taken from
	// XMLToPXMLTransformer.java)
	// TODO: encode this into an @Parameter
	protected ProbabilityNodeType[] pNodeDistribution = {
			MUTEX, MUTEX, MUTEX, MUTEX, INDEPENDENT, INDEPENDENT, INDEPENDENT, INDEPENDENT, EVENTS
	};
	// the number of random variables relative to the expected number of EVENT-type pNodes
	@Parameter(names = "--num-vars", description = "fraction of random variables relative to the number of EVENT-type nodes")
	protected float pVariablesRatio = 0.1f;

	// how to handle text nodes in the case of mux, ind and exp nodes
	@Parameter(names = "--text-nodes", description = "handle text nodes as 'wrap' ('ignore' to be added later)")
	protected String textNodeStrategy = "";

	// TODO: incorporate _ratioExpSubsets, and _maxExpSubsetsPwr from XMLToPXMLTransformer.java

	protected Random random;

	// file names list (collected in a list due to JCommander)
	@Parameter(description = "<infile> <outfile>", arity = 2)
	protected List<String> fileNames = new ArrayList<String>();

	/**
	 * Creates a new DocumentTransformer using the default distribution ratios specified.
	 */
	public DocumentTransformer() {
		this.random = new Random();
	}

	/**
	 * Overlays a multiverse of possible worlds over a given document using node types defined when this
	 * DocumentTransformer was constructed.
	 * 
	 * @param doc
	 *            The document to process.
	 * @throws DocumentTransformerException
	 *             When an XML or XPath error occurs while processing.
	 */
	public void transform(Document doc) throws DocumentTransformerException {
		System.err.println("text node strategy is " + this.textNodeStrategy);
		try {
			// add the pxml namespace to the document
			doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + NS_PREFIX, NS_URI);

			// create a new xpath object (...)
			XPath xpath = XPathFactory.newInstance().newXPath();
			// (...) and make sure it knows about the namespaces used in the document
			xpath.setNamespaceContext(new NamespaceResolver(doc));

			// find all the nodes in the document element
			NodeList nodes = (NodeList) xpath.evaluate("//*", doc.getDocumentElement(), XPathConstants.NODESET);
			int length = nodes.getLength();
			System.out.println("  document has " + length + " cadidate nodes");
			int numVariables = this.determineNumVariables(length);
			System.out.println("  will use " + numVariables + " random variables");

			// select length * occurrence nodes at random
			for (int i = 0, max = (int) (length * this.pNodesOccurrence); i < max; i++) {
				Node el = nodes.item(this.random.nextInt(length));
				// select a random number of preceding/following siblings to wrap together
				List<Node> selected = this.selectNodes(el);
				// create a pNode to wrap the selected elements with (make sure to create exactly enough attributes)
				Node pNode = this.createPNode(doc);
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
			System.out.println("  inserted " + independents.getLength() + " independent pNodes as <" + NS_PREFIX + ":"
					+ INDEPENDENT.nodeName + ">");
			for (int i = 0, max = independents.getLength(); i < max; i++) {
				Element pNode = (Element) independents.item(i);
				this.insertIndependantAttributes(doc, pNode, pNode.getChildNodes().getLength());
				// wrap the text nodes if requested
				if ("wrap".equalsIgnoreCase(this.textNodeStrategy)) {
					this.wrapTextNodes(doc, pNode.getChildNodes());
				}
			}

			NodeList mutexes = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + MUTEX.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + mutexes.getLength() + " mutex pNodes as <" + NS_PREFIX + ":"
					+ MUTEX.nodeName + ">");
			for (int i = 0, max = mutexes.getLength(); i < max; i++) {
				Element pNode = (Element) mutexes.item(i);
				this.insertMutexAttributes(doc, pNode, pNode.getChildNodes().getLength());
				// wrap the text nodes if requested
				if ("wrap".equalsIgnoreCase(this.textNodeStrategy)) {
					this.wrapTextNodes(doc, pNode.getChildNodes());
				}
			}

			NodeList explicits = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + EXPLICIT.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + explicits.getLength() + " explicit pNodes as <" + NS_PREFIX + ":"
					+ EXPLICIT.nodeName + ">");
			for (int i = 0, max = explicits.getLength(); i < max; i++) {
				Element pNode = (Element) explicits.item(i);
				this.insertExplicitAttributes(doc, pNode, pNode.getChildNodes().getLength());
				// wrap the text nodes if requested
				if ("wrap".equalsIgnoreCase(this.textNodeStrategy)) {
					this.wrapTextNodes(doc, pNode.getChildNodes());
				}
			}

			NodeList events = (NodeList) xpath.evaluate("//" + NS_PREFIX + ":" + EVENTS.nodeName,
					doc.getDocumentElement(), XPathConstants.NODESET);
			System.out.println("  inserted " + events.getLength() + " event conjunction pNodes as <" + NS_PREFIX + ":"
					+ EVENTS.nodeName + ">");
			for (int i = 0, max = events.getLength(); i < max; i++) {
				Element pNode = (Element) events.item(i);
				this.insertEventsAttributes(doc, pNode, numVariables);
			}

			// add the used random variables to the document
			Node varList = doc.createElementNS(NS_URI, NS_PREFIX + ":variables");
			for (int i = 0; i < numVariables; i++) {
				Element var = doc.createElementNS(NS_URI, NS_PREFIX + ":var-" + i);
				double probability = this.random.nextDouble();
				var.setAttributeNS(NS_URI, NS_PREFIX + ":val-0", "" + (1.0 - probability));
				var.setAttributeNS(NS_URI, NS_PREFIX + ":val-1", "" + probability);
				varList.appendChild(var);
			}
			doc.getDocumentElement().appendChild(varList);
		} catch (XPathException e) {
			throw new DocumentTransformerException("Unexpected XPath error while transforming: " + e.getMessage(), e);
		}
	}

	/**
	 * Determines the number of variables to use for the processing of a particular number of available nodes using the
	 * settings provided when this DocumentTransformer was creted.
	 * 
	 * @param numNodes
	 *            The total number of available nodes in a document.
	 * @return The number of variables to be used when processing a document with the given amount of nodes.
	 */
	protected int determineNumVariables(int numNodes) {
		// count the number of times EVENTS occurs in the distribution
		int occurrence = 0;
		for (ProbabilityNodeType type : this.pNodeDistribution) {
			if (type == EVENTS) {
				occurrence++;
			}
		}

		// calculate the number of variables from the number of expected EVENTS-type pNodes
		return (int) Math.max(numNodes * pNodesOccurrence * ((double) occurrence / this.pNodeDistribution.length)
				* this.pVariablesRatio, 1);
	}

	/**
	 * Selects a list of nodes that are siblings of the given node to be wrapped by a pNode. A minimum number of 1
	 * elements are selected (the node itself). The total number of siblings is the maximum number of nodes selected.
	 * 
	 * @param el
	 *            The selected element.
	 * @return A list of siblings of el (including el itself).
	 */
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

		// determine the number of siblings to select
		int numSiblings = this.random.nextInt(children.getLength());
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

	/**
	 * Wraps all text nodes in the node list with a <code>&lt;p:text&gt;</code> node.
	 * 
	 * @param nodes
	 *            The nodes to iterate.
	 * @param origin
	 *            The document to be used when creating new elements.
	 */
	protected void wrapTextNodes(Document origin, NodeList nodes) {
		System.err.println("wrapTextNodes called");
		for (int i = 9, length = nodes.getLength(); i < length; i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.TEXT_NODE) {
				// create a wrapper element named <p:text>
				Element wrapper = origin.createElementNS(NS_URI, NS_PREFIX + ":text");
				//
				node.getParentNode().replaceChild(wrapper, node);
				wrapper.appendChild(node);
			}
		}
	}

	/**
	 * Creates a random pNode chosen from the distribution provided when this DocumentTransformer was constructed.
	 * 
	 * @param origin
	 *            The source document, used to create a new node.
	 * @return A newly created, randomly selected pNode.
	 */
	protected Node createPNode(Document origin) {
		// select a type at random
		ProbabilityNodeType type = this.pNodeDistribution[this.random.nextInt(this.pNodeDistribution.length)];
		// create a new node of the required type
		Element pNode = origin.createElementNS(NS_URI, NS_PREFIX + ":" + type.nodeName);
		// return the newly created node
		return pNode;
	}

	/**
	 * Inserts numChilds attributes into pNode representing independent chances for pNode's children.
	 * 
	 * @param origin
	 *            The source document, used to create attributes.
	 * @param pNode
	 *            The node to add attributes to.
	 * @param numChilds
	 *            The number of childs of pNode.
	 */
	protected void insertIndependantAttributes(Document origin, Element pNode, int numChilds) {
		for (int i = 1; i <= numChilds; i++) {
			// add a p:child-i probability for each child
			pNode.setAttributeNS(NS_URI, NS_PREFIX + ":child-" + i, "" + this.random.nextDouble());
		}
	}

	/**
	 * Inserts numChilds + 1 attributes into pNode representing the chances of none or a single child.
	 * 
	 * @param origin
	 *            The source document, used to create attributes.
	 * @param pNode
	 *            The node to add attributes to.
	 * @param numChilds
	 *            The number childs of pNode.
	 */
	protected void insertMutexAttributes(Document origin, Element pNode, int numChilds) {
		// create a distribution of random numbers
		int[] distribution = new int[numChilds + 1];
		// keep track of the sum
		long sum = 0;
		for (int i = 0, amount = distribution.length; i < amount; i++) {
			// make sure to only use positive numbers
			int value = Math.abs(this.random.nextInt());
			distribution[i] = value;
			sum += value;
		}

		// add probability for no child (cast to double to force floating point result)
		pNode.setAttributeNS(NS_URI, NS_PREFIX + ":none", "" + distribution[0] / (double) sum);
		for (int i = 1; i <= numChilds; i++) {
			// add probability for child i
			pNode.setAttributeNS(NS_URI, NS_PREFIX + ":child-" + i, "" + distribution[i] / (double) sum);
		}
	}

	/**
	 * Unimplemented.
	 */
	protected void insertExplicitAttributes(Document origin, Element pNode, int numChilds) {
		// TODO
	}

	/**
	 * Inserts a number of predicate attributes into pNode, representing values of random variables. The number of
	 * variables used varies between 1 and log_2(total number of available variables).
	 * 
	 * @param origin
	 *            The source documents, used to create attributes.
	 * @param pNode
	 *            The node to add attributes to.
	 * @param numVariables
	 *            The total number of available variables.
	 */
	protected void insertEventsAttributes(Document origin, Element pNode, int numVariables) {
		// determine number of vars to use (max log_2(total vars), min 1)
		int numUsed = numVariables == 1 ? 1 : 1 + this.random.nextInt((int) Math.round((Math.log(numVariables) / Math
				.log(2))));

		// create list of available variables
		List<Integer> toUse = new ArrayList<Integer>(numVariables);
		for (int i = 0; i < numVariables; i++) {
			toUse.add(i);
		}

		List<String> descriptors = new ArrayList<String>(numUsed);
		// randomize the list
		Collections.shuffle(toUse);
		for (int i = 0; i < numUsed; i++) {
			String value = this.random.nextBoolean() ? "1" : "0";
			// add 'requirement' for a variable to be either true or false (using the numUsed first items in toUse)
			pNode.setAttributeNS(NS_URI, NS_PREFIX + ":var-" + toUse.get(i), value);
			// save a string representation of the descriptor
			descriptors.add("var-" + toUse.get(i) + "=" + value);
		}

		// save the entire description list to a single attribute for ease the XQuery expression later
		pNode.setAttributeNS(NS_URI, NS_PREFIX + ":descriptors", CollectionUtils.join(descriptors, " "));
	}

	/**
	 * Utility method to test DocumentTransformer. Reads a document from $1 and writes the document with a probabilistic
	 * overlay to $2.
	 * 
	 * @param args
	 *            Unused.
	 */
	public static void main(String... args) {
		// create transformer instance
		DocumentTransformer transformer = new DocumentTransformer();
		JCommander arguments = new JCommander(transformer);

		try {
			// have JCommander parse and assign arguments
			arguments.parse(args);
			// create input from first filename argument
			Document input = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new File(transformer.fileNames.get(0)));
			// do the actual transforming
			transformer.transform(input);
			// write the output to the second filename argument
			TransformerFactory.newInstance().newTransformer()
					.transform(new DOMSource(input), new StreamResult(new File(transformer.fileNames.get(1))));
		} catch (ParameterException e) {
			arguments.usage();
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
