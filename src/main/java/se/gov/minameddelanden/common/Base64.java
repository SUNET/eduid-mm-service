package se.gov.minameddelanden.common;

//import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static se.gov.minameddelanden.common.EncodingUtils.bytesToString;
import static se.gov.minameddelanden.common.EncodingUtils.stringToBytes;

public class Base64 {
    // Mapping table from 6-bit nibbles to Base64 characters.
    public static char[] map1 = new char[64];
    // Mapping table from Base64 characters to 6-bit nibbles.
    public static byte[] map2 = new byte[128];

    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++)
            map1[i++] = c;
        for (char c = 'a'; c <= 'z'; c++)
            map1[i++] = c;
        for (char c = '0'; c <= '9'; c++)
            map1[i++] = c;
        map1[i++] = '+';
        map1[i++] = '/';
    }

    static {
        for (int i = 0; i < map2.length; i++)
            map2[i] = -1;
        for (int i = 0; i < 64; i++)
            map2[map1[i]] = (byte) i;
    }

    private CharSequence base64;
    private byte[] bytes;

    private Base64(CharSequence base64) {
        this.base64 = base64;
    }

    private Base64(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @param s String to convert to bytes via UTF-8 and then base64
     * @return String converted to UTF-8 bytes and base64 encoded.
     * @deprecated Use {@link #fromStringBytes(String, java.nio.charset.Charset)} or {@link #fromStringBytes(String, String)}
     */
    @Deprecated
    public static Base64 fromStringUTF8Bytes(String s) {
        return fromStringBytes(s, EncodingUtils.UTF_8);
    }

    public static Base64 fromStringBytes(String s, String encoding) {
        return fromStringBytes(s, Charset.forName(encoding));
    }

    public static Base64 fromStringBytes(String s, Charset charset) {
        return fromBytes(stringToBytes(s, charset));
    }

    public static Base64 fromBytes(byte[] bytes) {
        return new Base64(bytes);
    }

    /*public static Base64 fromInputStream(InputStream inputStream) {
        try {
            return new Base64(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    } */

    public static Base64 fromBase64String(CharSequence string) {
        return new Base64(string);
    }

    @Override
    public String toString() {
        return toBase64String();
    }

    public String toBase64String() {
        initBase64();
        return base64.toString();
    }

    public byte[] toBytes() {
        initBytes();
        return bytes;
    }

    public String toBytesString(String encodingName) {
        return toBytesString(Charset.forName(encodingName));
    }

    public String toBytesString(Charset encoding) {
        return bytesToString(toBytes(), encoding);
    }

    private void initBase64() {
        if (null == base64) {
            base64 = encode(bytes);
        }
    }

    private void initBytes() {
        if (null == bytes) {
            bytes = decode(base64);
        }
    }

    private String encode(byte[] bytes) {
        return encode(bytes, bytes.length);
    }

    /**
     * Encodes a byte array into Base64 format. No blanks or line breaks are
     * inserted.
     *
     * @param in   an array containing the data bytes to be encoded.
     * @param iLen number of bytes to process in <code>in</code>.
     * @return A character array with the Base64 encoded data.
     */
    private static String encode(byte[] in, int iLen) {
        int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
        int oLen = ((iLen + 2) / 3) * 4; // output length including padding
        char[] out = new char[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iLen ? in[ip++] & 0xff : 0;
            int i2 = ip < iLen ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return new String(out);
    }

    /**
     * Decodes a byte array from Base64 format. No blanks or line breaks are
     * allowed within the Base64 encoded data.
     *
     * @param in a character array containing the Base64 encoded data.
     * @return An array containing the decoded data bytes.
     * @throws IllegalArgumentException if the input is not valid Base64 encoded
     *                                  data.
     */
    private static byte[] decode(CharSequence in) {
        in = in.toString().replaceAll("[^A-Za-z0-9+/=]", "");
        int iLen = in.length();
        if (iLen % 4 != 0)
            throw new IllegalArgumentException("Length of Base64 encoded input string "+iLen+" is not a multiple of 4.\n"+in);
        while (iLen > 0 && in.charAt(iLen - 1) == '=')
            iLen--;
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = in.charAt(ip++);
            int i1 = in.charAt(ip++);
            int i2 = ip < iLen ? in.charAt(ip++) : 'A';
            int i3 = ip < iLen ? in.charAt(ip++) : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            int b0 = map2[i0];
            int b1 = map2[i1];
            int b2 = map2[i2];
            int b3 = map2[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            int o0 = (b0 << 2) | (b1 >>> 4);
            int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            out[op++] = (byte) o0;
            if (op < oLen)
                out[op++] = (byte) o1;
            if (op < oLen)
                out[op++] = (byte) o2;
        }
        return out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Base64 base641 = (Base64) o;
        return toBase64String().equals(base641.toBase64String());
    }

    @Override
    public int hashCode() {
        return toBase64String().hashCode();
    }
}
