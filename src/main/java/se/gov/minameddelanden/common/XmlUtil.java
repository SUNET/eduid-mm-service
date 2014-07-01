package se.gov.minameddelanden.common;

import static se.gov.minameddelanden.common.EncodingUtils.bytesToString;
import static se.gov.minameddelanden.common.EncodingUtils.stringToBytes;
import static se.gov.minameddelanden.common.MiscUtils.firstOf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public final class XmlUtil {
    private static final XMLInputFactory XML_INPUT_FACTORY = getXmlInputFactory();

    private XmlUtil() {
    }

    static byte[] domToBytes(Document document) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        transform(getTransformer(), new DOMSource(document), new StreamResult(byteArrayOutputStream));
        return byteArrayOutputStream.toByteArray();
    }

    static Transformer getTransformer() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            return transformer;
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    static void transform(Transformer transformer, Source source, Result result) {
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeXmlNsPrefixes(Document document) {
        removeXmlNsPrefixes((Node)document);
        document.normalizeDocument();
    }

    public static void removeXmlNsPrefixes(Node node) {
        for (Node n : domList(node.getChildNodes())) {
            if (n instanceof Element) {
                Element e = (Element) n;
                for (Attr attribute : getAttributes(e)) {
                    String attributeName = attribute.getNodeName();
                    if (attributeName.startsWith("xmlns:")) {
                        e.removeAttributeNode(attribute);
                    }
                }
                removeXmlNsPrefixes(n);
                e.setPrefix("");
            }
        }
    }

    private static List<Attr> getAttributes(Element e) {
        List<Attr> attributes = new ArrayList<Attr>();
        NamedNodeMap attributesNodeMap = e.getAttributes();
        for (int i = 0; i < attributesNodeMap.getLength(); ++i) {
            attributes.add((Attr) attributesNodeMap.item(i));
        }
        return attributes;
    }

    private static List<Node> domList(final NodeList childNodes) {
        return new AbstractList<Node>() {

            @Override
            public Node get(int index) {
                return childNodes.item(index);
            }

            @Override
            public int size() {
                return childNodes.getLength();
            }
        };
    }

    @SuppressWarnings("unchecked")
	public static <T> T signJaxbObject(T object, X509CertificateWithPrivateKey certificateWithPrivateKey) {
        Xml xml = Xml.fromJaxbObject(object);
        Xml signedXml = xml.sign(certificateWithPrivateKey);
        return signedXml.toJaxbObject((Class<T>)object.getClass());
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory(boolean namespaceAware) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(namespaceAware);
            return dbf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static DocumentBuilder createDocumentBuilder(boolean namespaceAware) {
        try {
            return createDocumentBuilderFactory(namespaceAware).newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Charset getEncodingFromXmlHeader(XMLStreamReader xmlStreamReader) {
        String encoding = xmlStreamReader.getCharacterEncodingScheme();
        return null == encoding ? Xml.DEFAULT_ENCODING : Charset.forName(encoding);
    }

    static byte[] getXmlBytesFromString(String string) {
        return stringToBytes(string, getEncodingFromXmlHeader(createXmlStreamReader(string)));
    }

    private static XMLStreamReader createXmlStreamReader(String string) {
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(string));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return xmlStreamReader;
    }

    static String getXmlStringFromBytes(byte[] bytes) {
        return bytesToString(bytes, getEncodingFromXmlHeader(createXmlStreamReader(bytes)));
    }

    static XMLStreamReader createXmlStreamReader(byte[] bytes) {
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = XML_INPUT_FACTORY.createXMLStreamReader(new ByteArrayInputStream(bytes));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return xmlStreamReader;
    }

    static XMLInputFactory getXmlInputFactory() {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        Logger.getLogger(Xml.class.getName()).finest("Using XMLInputFactory implementation " + xmlInputFactory.getClass().getName());
        return xmlInputFactory;
    }

    public static String nodesToString(NodeList childNodes) {
        return nodesToString(asNodesList(childNodes));
    }

    private static String nodesToString(List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        for (Node node : nodes) {
            sb.append(nodetoString(node));
        }
        return sb.toString();
    }

    private static String nodetoString(Node node) {
        String nodeString;
        if (node instanceof Text) {
            nodeString = ((Text) node).getWholeText();
        } else {
            nodeString = Xml.fromDOM(node).toString();
        }
        return nodeString;
    }

    public static List<Element> getElementsByTagNameNS(Element root, String namespace, String localName) {
        return asElementsList(root.getElementsByTagNameNS(namespace, localName));
    }

    public static List<Element> getElementsByTagNameNS(Document root, String namespace, String localName) {
        return asElementsList(root.getElementsByTagNameNS(namespace, localName));
    }

    public static List<Node> asNodesList(final NodeList ns) {
        return new NodesList<Node>(ns);
    }

    public static List<Element> asElementsList(final NodeList ns) {
        return new NodesList<Element>(ns);
    }

    public static void transform(Source source, Result result) {
        transform(getTransformer(), source, result);
    }

    static void transform(InputStream inputStream, Writer writer) {
        transform(new StreamSource(inputStream), new StreamResult(writer));
    }

    static void transform(Reader reader, OutputStream outputStream) {
        transform(new StreamSource(reader), new StreamResult(outputStream));
    }

    public static Element getElementByTagNameNS(Element root, String namespace, String name) throws NoSuchElementException {
        return firstOf(asElementsList(root.getElementsByTagNameNS(namespace, name)));
    }

    public static Element renameElementTo(Element signatureElement, String name) {
        return (Element) signatureElement.getOwnerDocument().renameNode(signatureElement, signatureElement.getNamespaceURI(), name);
    }

    public static List<Node> getXPathNodes(Document document, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        return asNodesList((NodeList) xPath.evaluate(expression, document, XPathConstants.NODESET));
    }

    private static class NodesList<T extends Node> extends AbstractList<T> {
        private final NodeList ns;

        private NodesList(NodeList ns) {
            this.ns = ns;
        }

        @SuppressWarnings("unchecked")
		public T get(int index) {
            checkInRange(index);
            return (T) ns.item(index);
        }

        private void checkInRange(int index) {
            if (index < 0) {
                throw new NoSuchNodeException("index "+index +" < 0");
            }
            if (index >= size()) {
                throw new NoSuchNodeException("index "+index +" >= size "+size());
            }
        }

        @Override
        public int size() {
            return ns.getLength();
        }
    }

    public static class NoSuchNodeException extends NoSuchElementException {
		private static final long serialVersionUID = 8652545642611L;

		public NoSuchNodeException(String message) {
            super(message);
        }
    }

    public static <T> String toXmlString(T object) {
		
		Xml xml = Xml.fromJaxbObject(object);
		String xmlString = xml.toString();

		return xmlString;
	}

    public static <T> T toObject(String xmlString, Class<T> t) {
    	T object = Xml.fromString(xmlString).toJaxbObject(t);

    	return object;
    }
}
