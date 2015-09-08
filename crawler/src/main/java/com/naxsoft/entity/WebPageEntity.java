//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(
        name = "web_page",
        schema = "guns",
        catalog = "aggress"
)
public class WebPageEntity {
    private int id;
    private String content;
    private Timestamp modificationDate;
    private Integer statusCode;
    private String type;
    private boolean parsed;
    private SourceEntity sourceBySourceId;
    private String url;

    public WebPageEntity() {
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
            name = "content"
    )
    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
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
            name = "status_code"
    )
    public Integer getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
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

    @ManyToOne
    @JoinColumn(
            name = "source_id",
            referencedColumnName = "id",
            nullable = false
    )
    public SourceEntity getSourceBySourceId() {
        return this.sourceBySourceId;
    }

    public void setSourceBySourceId(SourceEntity sourceBySourceId) {
        this.sourceBySourceId = sourceBySourceId;
    }

    @Basic
    @Column(
            name = "url"
    )
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            WebPageEntity that = (WebPageEntity)o;
            return this.url.equals(that.url);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.url.hashCode();
    }
}
