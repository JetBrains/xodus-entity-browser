package com.lehvolk.xodus.web.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.validation.constraints.NotNull;

import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Xodus persistent store requisites
 * @author Alexey Volkov
 * @since 15.11.2015
 */
@Getter
@AllArgsConstructor
public class XodusStoreRequisites {

    private static final String LOCATION_KEY = "xodus.store.location";
    private static final String STORE_ACCESS_KEY = "xodus.store.key";
    private static final String FILE_KEY = "xodus.store.file.config";

    private final String location;
    private final String key;

    public static Optional<XodusStoreRequisites> from(String pathToFile) {
        if (pathToFile == null) {
            return Optional.absent();
        }
        try {
            return from(new FileInputStream(pathToFile));
        } catch (IOException e) {
            //ignore parsing or loading file exceptions
        }
        return Optional.absent();
    }

    public static Optional<XodusStoreRequisites> from(InputStream is) {
        try {
            Properties properties = new Properties();
            properties.load(is);
            String location = properties.getProperty(LOCATION_KEY);
            String key = properties.getProperty(STORE_ACCESS_KEY);
            if (location != null && key != null) {
                return Optional.of(new XodusStoreRequisites(location, key));
            }
        } catch (IOException e) {
            //ignore parsing exceptions
        } finally {
            closeQuietly(is);
        }
        return Optional.absent();
    }

    public static Optional<XodusStoreRequisites> fromSystem() {
        String location = System.getProperty(LOCATION_KEY);
        String key = System.getProperty(STORE_ACCESS_KEY);
        if (location != null && key != null) {
            return Optional.of(new XodusStoreRequisites(location, key));
        }
        return Optional.absent();
    }

    @NotNull
    public static XodusStoreRequisites get() {
        String file = System.getProperty(FILE_KEY);
        InputStream defaultConfig = XodusStoreRequisites.class.getResourceAsStream("/xodus-store.properties");
        return fromSystem().or(from(file)).or(from(defaultConfig)).get();
    }
}
