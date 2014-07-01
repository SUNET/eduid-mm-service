package se.gov.minameddelanden.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public final class JAXBMarshal {

    private static final Logger LOGGER = Logger.getLogger(JAXBMarshal.class.getName());

    private JAXBMarshal()
    {
    }

    static byte[] serialize(Object obj) throws JAXBException {
        return marshalToBytes(createMarshaller(obj), createJaxbElement(obj));
    }

    static Document serializeToDom(Object obj) {
        try {
            return marshalToDom(createMarshaller(obj), createJaxbElement(obj));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static JAXBElement<?> createJaxbElement(Object obj) {
        return createJaxbElement(obj, obj.getClass().getPackage().getAnnotation(XmlSchema.class).namespace());
    }

    private static Document marshalToDom(Marshaller marshaller, JAXBElement<?> jaxbElem) throws JAXBException {
        DOMResult result = new DOMResult();
        marshaller.marshal(jaxbElem, result);
        return (Document)result.getNode();
    }

    private static byte[] marshalToBytes(Marshaller marshaller, JAXBElement<?> jaxbElem) throws JAXBException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        marshaller.marshal(jaxbElem, byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static JAXBElement<?> createJaxbElement(Object obj, String ns) {
        return new JAXBElement(new QName(ns, getName(obj)), obj.getClass(), obj);
    }

    private static String getName(Object obj) {
        Class<?> clazz = obj.getClass();
        try {
            return clazz.getAnnotation(XmlRootElement.class).name();
        } catch (NullPointerException e) {
            LOGGER.warning("Missing annotation @XmlRootElement for "+clazz);
            return clazz.getSimpleName();
        }
    }

    private static Marshaller createMarshaller(Object obj) throws JAXBException {
        JAXBContext jaxbCtx = JAXBContext.newInstance(obj.getClass().getPackage().getName());
        return jaxbCtx.createMarshaller();
    }

	public static <T> T deserialize(byte[] bytes, Class<T> objectClass) throws JAXBException {
		JAXBContext jaxbCtx = JAXBContext.newInstance(objectClass.getPackage().getName());
		Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
		InputStream is = new ByteArrayInputStream(bytes);
		JAXBElement<T> elem = unmarshaller.unmarshal(new StreamSource(is), objectClass);

		return objectClass.cast(elem.getValue());
	}
}
