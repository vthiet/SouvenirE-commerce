package nlu.fit.web.souvenirecommerce.core.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class ProjectLogPaths {

    private static final String PROJECT_ROOT_RESOURCE = "logging-path.txt";
    private static final String PROJECT_LOG_DIR_PROPERTY = "PROJECT_LOG_DIR";
    private static final String PROJECT_ROOT_PROPERTY = "PROJECT_ROOT";
    private static final Path FALLBACK_BASE_DIR = Paths.get(System.getProperty("catalina.base", ".")).toAbsolutePath().normalize();

    private ProjectLogPaths() {
    }

    public static Path resolveProjectRoot() {
        String override = firstNonBlank(
                System.getProperty(PROJECT_LOG_DIR_PROPERTY),
                System.getenv(PROJECT_LOG_DIR_PROPERTY),
                System.getProperty(PROJECT_ROOT_PROPERTY),
                System.getenv(PROJECT_ROOT_PROPERTY)
        );

        if (override != null) {
            return Paths.get(override).toAbsolutePath().normalize();
        }

        String filteredProjectRoot = readResource(PROJECT_ROOT_RESOURCE);
        if (filteredProjectRoot != null) {
            return Paths.get(filteredProjectRoot).toAbsolutePath().normalize();
        }

        return FALLBACK_BASE_DIR;
    }

    public static Path resolveLogDir() {
        return resolveProjectRoot().resolve("logs").toAbsolutePath().normalize();
    }

    public static Path resolveActivityLogFile() {
        return resolveLogDir().resolve("activity.log").toAbsolutePath().normalize();
    }

    public static void ensureLogDirExists() {
        try {
            java.nio.file.Files.createDirectories(resolveLogDir());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create log directory: " + resolveLogDir(), e);
        }
    }

    private static String readResource(String resourceName) {
        ClassLoader classLoader = ProjectLogPaths.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String value = reader.readLine();
                String normalized = normalize(value);
                return normalized == null ? null : normalized.replace("\\\\", "\\");
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
