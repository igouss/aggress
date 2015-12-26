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
        catalog = "aggress",
        indexes = {
                @Index(name = "WebPageEntity_type_idx", columnList = "type"),
                @Index(name = "WebPageEntity_parsed_idx", columnList = "parsed")
        }
)
public class WebPageEntity {
    private int id;
    private String content;
    private Timestamp modificationDate;
    private Integer statusCode;
    private String type;
    private boolean parsed;
    private String url;
    private WebPageEntity parent;

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
            name = "content",
            columnDefinition = "TEXT"
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


    @Basic
    @Column(
            name = "url",
            columnDefinition = "TEXT"
    )
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebPageEntity that = (WebPageEntity) o;

        if (!type.equals(that.type)) return false;
        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent", referencedColumnName = "id")
    public WebPageEntity getParent() {
        return parent;
    }

    public void setParent(WebPageEntity parent) {
        this.parent = parent;
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
