package com.naxsoft.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
@Entity
@Table(name = "web_page", schema = "guns", catalog = "aggress")
public class WebPageEntity {
    private int id;
    private String content;
    private Timestamp modificationDate;
    private Integer statusCode;
    private String type;
    private boolean parsed;
    private SourceEntity sourceBySourceId;
    private String url;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "modification_date")
    public Timestamp getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(Timestamp modificationDate) {
        this.modificationDate = modificationDate;
    }

    @Basic
    @Column(name = "status_code")
    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "parsed")
    public boolean isParsed() {
        return parsed;
    }

    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "id", nullable = false)
    public SourceEntity getSourceBySourceId() {
        return sourceBySourceId;
    }

    public void setSourceBySourceId(SourceEntity sourceBySourceId) {
        this.sourceBySourceId = sourceBySourceId;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebPageEntity that = (WebPageEntity) o;

        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
