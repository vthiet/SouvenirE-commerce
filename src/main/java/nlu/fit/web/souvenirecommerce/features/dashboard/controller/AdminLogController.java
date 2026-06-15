package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.AdminLogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@WebServlet("/admin/logs")
public class AdminLogController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminLogController.class);
    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(?<timestamp>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[(?<thread>[^\\]]*)\\] (?<level>\\w+) \\[(?<logger>[^\\]]*)\\] - \\[(?<requestId>[^\\]]*)\\] \\[(?<user>[^\\]]*)\\] - (?<message>.*)$"
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String level = normalize(request.getParameter("level"));
        String query = normalize(request.getParameter("q"));
        int limit = parseLimit(request.getParameter("limit"));

        List<Path> logFiles = resolveLogFiles();
        List<AdminLogEntry> entries = new ArrayList<>();

        for (Path logFile : logFiles) {
            readLogFile(logFile, level, query, entries, limit);
            if (entries.size() >= limit) {
                break;
            }
        }

        request.setAttribute("entries", entries);
        request.setAttribute("selectedLevel", level);
        request.setAttribute("query", query);
        request.setAttribute("limit", limit);
        request.setAttribute("logFiles", logFiles);
        request.setAttribute("logCount", entries.size());
        request.getRequestDispatcher("/admin/logs.jsp").forward(request, response);
    }

    private void readLogFile(Path file, String level, String query, List<AdminLogEntry> entries, int limit) {
        if (!Files.exists(file)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = lines.size() - 1; i >= 0; i--) {
                AdminLogEntry entry = parseLine(lines.get(i));
                if (entry == null) {
                    continue;
                }
                if (!matchesLevel(entry, level) || !matchesQuery(entry, query)) {
                    continue;
                }
                entries.add(entry);
                if (entries.size() >= limit) {
                    return;
                }
            }
        } catch (IOException e) {
            log.warn("Unable to read admin log file: {}", file, e);
        }
    }

    private boolean matchesLevel(AdminLogEntry entry, String level) {
        return level.isEmpty() || level.equalsIgnoreCase(entry.getLevel());
    }

    private boolean matchesQuery(AdminLogEntry entry, String query) {
        if (query.isEmpty()) {
            return true;
        }
        String needle = query.toLowerCase();
        return contains(entry.getTimestamp(), needle)
                || contains(entry.getThread(), needle)
                || contains(entry.getLevel(), needle)
                || contains(entry.getLogger(), needle)
                || contains(entry.getRequestId(), needle)
                || contains(entry.getUser(), needle)
                || contains(entry.getMessage(), needle)
                || contains(entry.getRawLine(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private AdminLogEntry parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        return new AdminLogEntry(
                matcher.group("timestamp"),
                matcher.group("thread"),
                matcher.group("level"),
                matcher.group("logger"),
                matcher.group("requestId"),
                matcher.group("user"),
                matcher.group("message"),
                line
        );
    }

    private List<Path> resolveLogFiles() {
        Path logDir = resolveLogDir();
        List<Path> files = new ArrayList<>();

        if (Files.isDirectory(logDir)) {
            try (Stream<Path> stream = Files.list(logDir)) {
                stream.filter(path -> {
                            String name = path.getFileName().toString();
                            return name.equals("app.log") || name.matches("app\\.\\d{4}-\\d{2}-\\d{2}\\.log");
                        })
                        .sorted(Comparator.comparing(this::safeLastModified).reversed())
                        .forEach(files::add);
            } catch (IOException e) {
                log.warn("Unable to list admin log directory: {}", logDir, e);
            }
        }

        return files;
    }

    private long safeLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }

    private Path resolveLogDir() {
        String explicit = System.getProperty("LOG_DIR");
        if (explicit == null || explicit.isBlank()) {
            explicit = System.getenv("LOG_DIR");
        }
        if (explicit != null && !explicit.isBlank()) {
            return Paths.get(explicit);
        }
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null && !catalinaBase.isBlank()) {
            return Paths.get(catalinaBase, "logs");
        }
        return Paths.get("logs");
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private int parseLimit(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return Math.max(25, Math.min(parsed, 500));
        } catch (Exception e) {
            return 100;
        }
    }
}
