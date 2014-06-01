package se.gov.minameddelanden.common;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import se.gov.minameddelanden.common.sign.SignatureUtils;

import javax.xml.bind.JAXBException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static se.gov.minameddelanden.common.XmlUtil.asElementsList;

public class Xml {
    static final Charset DEFAULT_ENCODING = EncodingUtils.UTF_8;

    private byte[] bytes;
    private Base64 base64;
    private String string;
    private Source source;

    Xml(byte[] bytes) {
        if (null == bytes) {
            throw new NullArgumentException("bytes");
        }
        this.bytes = bytes;
    }

    private Xml(Base64 base64) {
        if (null == base64) {
            throw new NullArgumentException("base64");
        }
        this.base64 = base64;
    }

    private Xml(String string) {
        if (null == string) {
            throw new NullArgumentException("string");
        }
        this.string = string;
    }

    private Xml(Source source) {
        if (null == source) {
            throw new NullArgumentException("source");
        }
        this.source = source;
    }

    public static Xml fromString(String string) {
        return new Xml(string);
    }

    public static Xml fromJaxbObject(Object obj) {
        try {
            return Xml.fromBytes(JAXBMarshal.serialize(obj));
        } catch (JAXBException e) {
            throw new RuntimeException(obj.toString(), e);
        }
    }

    public static Xml fromBase64String(String base64String) {
        return fromBase64(Base64.fromBase64String(base64String));
    }

    public static Xml fromBase64(Base64 base64) {
        return new Xml(base64);
    }

    public static Xml fromBytes(byte[] bytes) {
        return new Xml(bytes);
    }

    public static Xml fromDOM(Document document) {
        return new Xml(XmlUtil.domToBytes(document));
    }

    public static Xml fromInputStream(InputStream inputStream) {
        try {
            return new Xml(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SignedXml sign(X509CertificateWithPrivateKey certificateWithPrivateKey) {
        Document xmlDocument = toDOM();
        XMLSignature signature = SignatureUtils.signXml(xmlDocument, certificateWithPrivateKey);
        return new SignedXml(XmlUtil.domToBytes(xmlDocument), signature);
    }

    public SignedXml sign(String uriRef, X509CertificateWithPrivateKey certificateWithPrivateKey) {
        Document xmlDocument = toDOM();
        XMLSignature signature = SignatureUtils.signXml(xmlDocument, uriRef, certificateWithPrivateKey);
        return new SignedXml(XmlUtil.domToBytes(xmlDocument), signature);
    }

    public boolean isValid(Schema schema) {
        try {
            validate(schema);
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

    public Xml validate(Schema schema) throws SAXException {
        try {
            schema.newValidator().validate(toSource());
            return this;
        } catch (SAXException e) {
            throw new SAXException(toString(), e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Reader toReader() {
        return new StringReader(toString());
    }

    public byte[] toBytes() {
        initBytes();
        return bytes;
    }

    public String toString() {
        initString();
        return string;
    }

    public Base64 toBase64() {
        initBase64();
        return base64;
    }

    public String toBase64String() {
        return toBase64().toString();
    }

    public <T> T toJaxbObject(Class<T> resultType) {
        try {
            return JAXBMarshal.deserialize(toBytes(), resultType);
        } catch (JAXBException e) {
            throw new RuntimeException(toString(), e);
        }
    }

    public Document toDOM() {
        return toDOM(true);
    }

    public Document toNamespaceUnawareDOM() {
        return toDOM(false);
    }

    private Document toDOM(boolean namespaceAware) {
        return toDOM(XmlUtil.createDocumentBuilder(namespaceAware));
    }

    private InputStream toInputStream() {
        return new ByteArrayInputStream(toBytes());
    }

    private Document toDOM(DocumentBuilder documentBuilder) {
        try {
            return documentBuilder.parse(new ByteArrayInputStream(toBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeTo(OutputStream outputStream) {
        try {
            IOUtils.write(toBytes(), outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initBytes() {
        if (null != bytes) {
            return;
        }
        if (null != source) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            transformSource(new StreamResult(outputStream));
            bytes = outputStream.toByteArray();
        } else if (null != base64) {
            bytes = base64.toBytes();
        } else {
            bytes = XmlUtil.getXmlBytesFromString(string);
        }
    }

    private void transformSource(StreamResult result) {
        XmlUtil.transform(source, result);
        source = null;
    }

    private void initString() {
        if (null != string) {
            return;
        }
        if (null != source) {
            StringWriter writer = new StringWriter();
            transformSource(new StreamResult(writer));
            string = writer.toString();
        } else {
            string = XmlUtil.getXmlStringFromBytes(toBytes());
        }
    }

    private void initBase64() {
        if (null == base64) {
            base64 = Base64.fromBytes(toBytes());
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Xml xml = (Xml) o;

        return Arrays.equals(toBytes(), xml.toBytes());
    }

    public String toIndentedString() {
        return toIndentedString(2);
    }

    private String toIndentedString(int indent) {
        if (indent < 0) {
            throw new IllegalArgumentException("indent < 0: "+indent);
        }
        StringWriter writer = new StringWriter();
        Transformer transformer = XmlUtil.getTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", ""+indent);
        if (toBytes().length == 0) {
            return "";
        }
        XmlUtil.transform(transformer, toSource(), new StreamResult(writer));
        return writer.toString();
    }

    private StreamSource toSource() {
        if (null != bytes || null != source) {
            return new StreamSource(toInputStream());
        } else {
            return new StreamSource(toReader());
        }
    }

    public static Xml fromDOM(Node node) {
        Document document = XmlUtil.createDocumentBuilder(true).newDocument();
        Node clonedNode = node.cloneNode(true);
        document.adoptNode(clonedNode);
        document.appendChild(clonedNode);
        return Xml.fromDOM(document);
    }

    public Xml toIndentedXml() {
        return fromString(toIndentedString());
    }

    public static Xml fromSource(Source source) {
        return new Xml(source);
    }

    public List<Element> getElementsByTagName(String elementTagName) {
        return asElementsList(toDOM().getElementsByTagName(elementTagName));
    }

    public List<Node> getNodesByXPath(String xpathExpression) {
        try {
            return XmlUtil.getXPathNodes(toNamespaceUnawareDOM(), xpathExpression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Node> getNodesByXPathNS(String xpathExpression) {
        try {
            return XmlUtil.getXPathNodes(toDOM(), xpathExpression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}