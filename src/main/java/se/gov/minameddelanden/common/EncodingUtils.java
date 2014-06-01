package se.gov.minameddelanden.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.Arrays;

import static java.lang.Math.max;
import static java.lang.Math.min;

public final class EncodingUtils {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset ASCII = Charset.forName("ASCII");

    private EncodingUtils() {
    }

    public static byte[] stringToBytes(String string, Charset encoding) throws EncodingException {
        try {
            ByteBuffer bb = encoding.newEncoder().encode(CharBuffer.wrap(string));
            return Arrays.copyOf(bb.array(), bb.remaining());
        } catch (CharacterCodingException e) {
            throw new EncodingException(e);
        }
    }

    public static String bytesToString(byte[] bytes, Charset encoding) throws EncodingException {
        try {
            return tryBytesToString(bytes, encoding);
        } catch (MalformedInputException e) {
            throw wrapCharacterCodingException(bytes, encoding, e, e.getInputLength());
        } catch (UnmappableCharacterException e) {
            throw wrapCharacterCodingException(bytes, encoding, e, e.getInputLength());
        } catch (CharacterCodingException e) {
            throw new EncodingException(e);
        }
    }

    private static EncodingException wrapCharacterCodingException(byte[] bytes, Charset encoding, CharacterCodingException e, int offset) {
        String message = "Illegal byte " + bytesToHexAroundOffset(bytes, offset, 3) + " at input offset " + offset + " for encoding " + encoding;
        if (!encoding.equals(ISO_8859_1)) {
            message += "\nFor encoding ISO-8859-1 the result would be:\n"+new String(bytes, ISO_8859_1);
        }
        return new EncodingException(message, e);
    }

    private static String tryBytesToString(byte[] bytes, Charset encoding) throws CharacterCodingException {
        CharBuffer cb = encoding.newDecoder().decode(ByteBuffer.wrap(bytes));
        return new String(cb.array(), 0, cb.length());
    }

    public static String bytesToHexAroundOffset(byte[] bytes, int offset, int around) {
        StringBuilder sb = new StringBuilder();
        for (int i = max(offset - around, 0); i < offset; ++i) {
            sb.append(byteToHex(bytes[i])).append(' ');
        }
        sb.append("->").append(byteToHex(bytes[offset])).append("<-");
        for (int i = offset+1; i < min(offset+1+ around, bytes.length); ++i) {
            sb.append(' ').append(byteToHex(bytes[i]));
        }
        return sb.toString();
    }

    public static String byteToHex(byte aByte) {
        return String.format("%02X", aByte);
    }

    public static String bytesToStringWithFallback(byte[] bytes, Charset encoding, Charset fallbackEncoding) {
        try {
            return tryBytesToString(bytes, encoding);
        } catch (CharacterCodingException cce) {
            try {
                return tryBytesToString(bytes, fallbackEncoding);
            } catch (CharacterCodingException e) {
                throw new EncodingException(e);
            }
        }
    }

    public static String utf8BytesToString(byte[] utf8Bytes) {
        return bytesToString(utf8Bytes, UTF_8);
    }

    public static byte[] stringToUtf8Bytes(String s) {
        return stringToBytes(s, UTF_8);
    }

    @SuppressWarnings("serial")
	public static class EncodingException extends RuntimeException {
        private EncodingException(String message, CharacterCodingException e) {
            super(message,e);
        }

        private EncodingException(CharacterCodingException e) {
            super(e);
        }
    }
}
