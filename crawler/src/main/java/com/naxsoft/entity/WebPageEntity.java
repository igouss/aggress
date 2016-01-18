//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.entity;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Web page that can either be leaf (with produce data) or be used to find subpages
 */
@Entity
@Table(
        name = "web_page",
        schema = "guns",
        catalog = "aggress",
        indexes = {
                @Index(columnList = "type"),
                @Index(columnList = "parsed"),
                @Index(columnList = "url,type", unique = true)

        }
)
public class WebPageEntity {
    private static final Logger logger = LoggerFactory.getLogger(WebPageEntity.class);

    /**
     *
     */
    private int id;

    /**
     *
     */
    private String content;

    /**
     *
     */
    private Timestamp modificationDate;

    /**
     *
     */
    private Integer statusCode;

    /**
     *
     */
    private String type;

    /**
     *
     */
    private boolean parsed;

    /**
     *
     */
    private String url;

    /**
     *
     */
    private String category;

    public WebPageEntity() {
    }

    /**
     * ZIP the string and return Base64 representation
     *
     * @param text
     * @return
     * @throws IOException
     */
    private static String compress(String text) {
        ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
        GZIPOutputStream zos;
        try {
            zos = new GZIPOutputStream(rstBao);
            zos.write(text.getBytes());
            IOUtils.closeQuietly(zos);

            byte[] bytes = rstBao.toByteArray();
            // In my solr project, I use org.apache.solr.co mmon.util.Base64.
            // return = org.apache.solr.common.util.Base64.byteArrayToBase64(bytes, 0,
            // bytes.length);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            logger.error("Failed to compress", e);
        }
        return "";
    }

    /**
     * Unzip a BASE64 string
     *
     * @param zippedBase64Str
     * @return
     * @throws IOException
     */
    private static String decompress(String zippedBase64Str) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(zippedBase64Str);
        GZIPInputStream zi = null;
        try {
            zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
            return IOUtils.toString(zi);
        } finally {
            IOUtils.closeQuietly(zi);
        }
    }

    private static String removeNonASCII(String text) {
        return text.replaceAll("[^\\x00-\\x7F]", "");
    }

    @Id
    @Column(
            name = "id"
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(
            name = "content",
            columnDefinition = "TEXT"
    )

    /**
     * Get pages's HTML
     */
    public String getContent() {
        if (null != this.content) {
            try {
                return decompress(this.content);
            } catch (IOException e) {
                logger.error("Failed to decompress", e);
            }
        } else {
            return null;
        }
        return "";
    }

    /**
     * Set page HTML
     *
     * @param content HTML content
     */
    public void setContent(String content) {
        if (null != content) {
            this.content = compress(removeNonASCII(content));
        } else {
            this.content = null;
        }

    }

    @Basic
    @Column(
            name = "modification_date"
    )
    public Timestamp getModificationDate() {
        return this.modificationDate;
    }

    public void setModificationDate(Timestamp modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Basic
    @Column(
            name = "type"
    )
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(
            name = "parsed"
    )
    public boolean isParsed() {
        return this.parsed;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    @Basic
    @Column(
            name = "url"
            , length = 2048
//            , columnDefinition = "TEXT"
    )
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(
            name = "category"
            , length = 128
    )
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebPageEntity that = (WebPageEntity) o;

        return type.equals(that.type) && url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WebPageEntity{" +
                "parsed = '" + parsed + '\'' +
                ", type = " + type +
                ", url = '" + url + '\'' +
                '}';
    }
}
