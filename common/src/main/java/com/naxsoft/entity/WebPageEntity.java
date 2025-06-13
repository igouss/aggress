package com.naxsoft.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Immutable web page entity representing a crawled web page with compressed content.
 * Can represent both leaf pages (with product data) and parent pages (for navigation).
 * Uses Lombok for reduced boilerplate and improved maintainability.
 */
@Value
@Builder(toBuilder = true)
@Slf4j
@JsonDeserialize(builder = WebPageEntity.WebPageEntityBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebPageEntity {

    @NonNull
    String url;

    @NonNull
    String type;

    String content; // Stored compressed as Base64
    String category;

    @Builder.Default
    Instant createdAt = Instant.now();

    @Builder.Default
    boolean parsed = false;

    // Transient parent reference (not persisted)
    @Builder.Default
    WebPageEntity parent = null;

    /**
     * Create a WebPageEntity with content compression
     * @param parent Parent page entity (optional)
     * @param rawContent Raw HTML content to be compressed
     * @param type Page type (frontPage, productList, productPage, etc.)
     * @param url Page URL
     * @param category Page category (optional)
     * @return WebPageEntity with compressed content
     */
    public static WebPageEntity createWithContent(WebPageEntity parent, String rawContent,
                                                  String type, String url, String category) {
        return WebPageEntity.builder()
                .parent(parent)
                .content(rawContent != null ? compress(removeNonASCII(rawContent)) : null)
                .type(type)
                .url(url)
                .category(category)
                .build();
    }

    /**
     * Create a WebPageEntity without content (for URL placeholders)
     */
    public static WebPageEntity createPlaceholder(String type, String url, String category) {
        return WebPageEntity.builder()
                .type(type)
                .url(url)
                .category(category)
                .build();
    }

    /**
     * Backward compatibility factory method for legacy code migration.
     * TODO: Remove this method in Phase 2 when all legacy code is migrated.
     *
     * @param parent Parent page entity (can be null)
     * @param content Raw HTML content
     * @param type Page type
     * @param url Page URL
     * @param category Page category
     * @return WebPageEntity with compressed content
     */
    public static WebPageEntity legacyCreate(WebPageEntity parent, String content, String type, String url, String category) {
        return WebPageEntity.builder()
                .parent(parent)
                .content(content != null ? compress(removeNonASCII(content)) : null)
                .type(type)
                .url(url)
                .category(category)
                .build();
    }

    /**
     * ZIP the string and return Base64 representation
     * @param text Value to compress
     * @return Compressed Value as Base64 string
     */
    private static String compress(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {

            gzipOut.write(text.getBytes(StandardCharsets.UTF_8));
            gzipOut.finish();

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("Failed to compress content", e);
            return "";
        }
    }

    /**
     * Decompress a BASE64 string
     * @param zippedBase64Str value to decompress
     * @return Decompressed value
     * @throws IOException in case of decompression error
     */
    private static String decompress(String zippedBase64Str) throws IOException {
        if (zippedBase64Str == null || zippedBase64Str.isEmpty()) {
            return "";
        }

        byte[] bytes = Base64.getDecoder().decode(zippedBase64Str);

        try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return IOUtils.toString(gzipIn, StandardCharsets.UTF_8);
        }
    }

    /**
     * Remove all non-ASCII values from text for safer storage
     * @param text Value to sanitize
     * @return String with only ASCII values present
     */
    private static String removeNonASCII(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[^\\x00-\\x7F]", "");
    }

    /**
     * Get the decompressed HTML content of the page
     * @return Decompressed HTML content
     */
    public String getContent() {
        if (content == null || content.isEmpty()) {
            return "";
        }

        try {
            return decompress(content);
        } catch (IOException e) {
            log.error("Failed to decompress content for URL: {}", url, e);
            return "";
        }
    }

    /**
     * Check if this page has been parsed
     *
     * @return true if page has been processed
     */
    public boolean isParsed() {
        return parsed;
    }

    /**
     * Mark this page as parsed
     * @return new instance with parsed flag set to true
     */
    public WebPageEntity markAsParsed() {
        return this.toBuilder().parsed(true).build();
    }

    /**
     * Log processing information for this page
     */
    public void logProcessing() {
        log.info("Processing page: {} of type: {} (category: {})", url, type, category);
    }
}
