package se.gov.minameddelanden.common;

import static com.google.common.collect.Iterables.concat;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.activation.DataSource;
import javax.activation.URLDataSource;
import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

public final class MiscUtils {

    private static final String CLASSPATH_URL_SCHEME = "classpath";
    private static final Logger LOGGER = Logger.getLogger(MiscUtils.class.getName());

    private MiscUtils() {
    }

    public static <T> T firstOf(@Nonnull Iterable<T> iterable) throws NoSuchElementException {
        return iterable.iterator().next();
    }

    public static <T> T lastOf(@Nonnull List<T> list) throws NoSuchElementException {
        return list.get(list.size() - 1);
    }


    public static DataSource getDataSourceForUrl(URI uri) {
        try {
            if (CLASSPATH_URL_SCHEME.equalsIgnoreCase(uri.getScheme())) {
                String path = uri.getPath();
                if (!path.startsWith("/")) {
                    throw new RuntimeException("classpath url path needs to start with \"/\": " + uri);
                }
                path = path.substring(1);
                return getClassPathDataSource(path);
            }
            URL url;
            try {
                url = uri.toURL();
            } catch (MalformedURLException ignored) {
                url = new URL("file:" + uri.toString());
            }
            return new URLDataSource(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DataSource getClassPathDataSource(String path) throws IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Iterable<URL> allResources = getResources(path, "context", contextClassLoader);

        Class<MiscUtils> classLoaderClass = MiscUtils.class;
        ClassLoader classLoader = classLoaderClass.getClassLoader();
        if (classLoader != contextClassLoader) {
            allResources = concat(allResources, getResources(path, classLoaderClass.toString(), classLoader));
        }

        URL firstResource = firstOf(allResources);
        LOGGER.fine("Using first resource " + firstResource + " for " + path + " out of " + allResources);
        return new URLDataSource(firstResource);
    }

    private static List<URL> getResources(String path, String loggingName, ClassLoader classLoader) throws IOException {
        List<URL> resources = Collections.list(classLoader.getResources(path));
        LOGGER.finer("Found resources for " + path + " in " + loggingName + " classloader " + getClassLoaderPath(classLoader) + ": " + resources);
        return resources;
    }

    static String getClassLoaderPath(ClassLoader classLoader) {
        StringBuilder s = new StringBuilder();
        s.append(stringify(classLoader));
        if (null != classLoader.getParent()) {
            s.append(" in ");
            s.append(getClassLoaderPath(classLoader.getParent()));
        }
        return s.toString();
    }

    private static String stringify(ClassLoader classLoader) {
        String toString = classLoader.toString();
        String result = defaultObjectToString(classLoader);
        if (!toString.equals(result)) {
            result += "(" + toString + ")";
        }
        return result;
    }

    private static String defaultObjectToString(Object o) {
        return o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }

    public static byte[] getBytesFromUri(URI uri) {
        try {
            return IOUtils.toByteArray(getDataSourceForUrl(uri).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean anyNull(Object... objects) {
        return asList(objects).contains(null);
    }

    public static String joinMap(String entrySeparator, String keyValueSeparator, Map<String, String> fieldValues) {
        StringBuilder result = new StringBuilder();
        for (Iterator<Map.Entry<String, String>> iterator = fieldValues.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> entry = iterator.next();
            result.append(entry.getKey());
            result.append(keyValueSeparator);
            result.append(entry.getValue());
            if (iterator.hasNext()) {
                result.append(entrySeparator);
            }
        }
        return result.toString();
    }

    public static <E> Set<E> asSet(final E... members) {
        return new AbstractSet<E>() {

            @Override
            @Nonnull
            public Iterator<E> iterator() {
                return asList(members).iterator();
            }

            @Override
            public int size() {
                return members.length;
            }
        };
    }

    public static <E> Iterator<E> skip(int i, Iterator<E> iterator) {
        for (int j = 0; j < i && iterator.hasNext(); j++) {
            iterator.next();
        }
        return iterator;
    }

    public static <E> Iterable<E> skipFirst(final int i, final Iterable<E> iterable) {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return skip(i, iterable.iterator());
            }
        };
    }
}
