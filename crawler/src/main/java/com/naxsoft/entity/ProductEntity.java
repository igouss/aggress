//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.entity;

import javax.persistence.*;

/**
 *
 */
@Entity
@Table(
        name = "product",
        schema = "guns",
        catalog = "aggress",
        indexes = {
                @Index(name = "ProductEntity_indexed_idx", columnList = "indexed")
        }
)
public class ProductEntity {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String json;

    /**
     *
     */
    private Long webpageId;

    /**
     *
     */
    private boolean indexed;

    /**
     *
     */
    private String url;

    public ProductEntity() {
    }


    @Id
    @Column(
            name = "id"
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(
            name = "json",
            columnDefinition = "TEXT"
    )
    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductEntity that = (ProductEntity) o;

        if (!json.equals(that.json)) return false;
        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = json.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Basic
    @Column(
            name = "webpage_id"
    )
    public Long getWebpageId() {
        return this.webpageId;
    }

    public void setWebpageId(Long webpageId) {
        this.webpageId = webpageId;
    }

    @Basic
    @Column(name = "indexed")
    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    @Basic
    @Column(name = "url", columnDefinition = "TEXT")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "json='" + json + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
