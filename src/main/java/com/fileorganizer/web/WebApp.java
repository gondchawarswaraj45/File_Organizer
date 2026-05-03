package com.fileorganizer.web;

import com.fileorganizer.core.OrganizerEngine;
import com.fileorganizer.core.UndoManager;
import com.fileorganizer.util.ConfigManager;
import com.fileorganizer.util.LoggerUtility;
import com.fileorganizer.util.ZipUtil;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebApp {
    private final ConfigManager config;
    private final OrganizerEngine engine;

    public WebApp(ConfigManager config, UndoManager undoManager) {
        this.config = config;
        this.engine = new OrganizerEngine(config, undoManager);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/organize", new OrganizeHandler(engine));

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("=================================================");
        System.out.println("  Web UI is running at http://localhost:" + port);
        System.out.println("=================================================");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String path = exchange.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }
                
                // Read from classpath resources/public
                InputStream is = getClass().getResourceAsStream("/public" + path);
                if (is == null) {
                    String response = "404 (Not Found)\n";
                    exchange.sendResponseHeaders(404, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    byte[] bytes = is.readAllBytes();
                    exchange.getResponseHeaders().set("Content-Type", getMimeType(path));
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
        
        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            return "application/octet-stream";
        }
    }

    static class OrganizeHandler implements HttpHandler {
        private final OrganizerEngine engine;

        public OrganizeHandler(OrganizerEngine engine) {
            this.engine = engine;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Path tempDir = Files.createTempDirectory("file-organizer-web-");
            Path zipFilePath = tempDir.resolve("uploaded.zip");
            Path extractDir = tempDir.resolve("extracted");
            Path resultZipPath = tempDir.resolve("organized.zip");

            try {
                // 1. Read entire body (raw file bytes) into uploaded.zip
                try (InputStream is = exchange.getRequestBody();
                     OutputStream os = Files.newOutputStream(zipFilePath)) {
                    is.transferTo(os);
                }

                // 2. Extract
                ZipUtil.unzip(zipFilePath, extractDir);

                // 3. Organize
                engine.organize(extractDir, false);

                // 4. Zip the organized folder
                ZipUtil.zipFolder(extractDir, resultZipPath);

                // 5. Send back
                byte[] responseBytes = Files.readAllBytes(resultZipPath);
                
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"organized.zip\"");
                exchange.sendResponseHeaders(200, responseBytes.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }

            } catch (Exception e) {
                LoggerUtility.getInstance().error("Web error: " + e.getMessage());
                e.printStackTrace();
                String response = "Error: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } finally {
                deleteDirectory(tempDir.toFile());
            }
        }
        
        private void deleteDirectory(File directoryToBeDeleted) {
            File[] allContents = directoryToBeDeleted.listFiles();
            if (allContents != null) {
                for (File file : allContents) {
                    deleteDirectory(file);
                }
            }
            directoryToBeDeleted.delete();
        }
    }
}
