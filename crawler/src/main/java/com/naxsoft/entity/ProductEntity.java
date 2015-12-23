//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
        name = "product",
        schema = "guns",
        catalog = "aggress"
)
public class ProductEntity {
    private int id;
    private String json;
    private int webpageId;
    private boolean indexed;
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
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(
            name = "json",
            columnDefinition="TEXT"
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

        if (webpageId != that.webpageId) return false;
        return json.equals(that.json);

    }

    @Override
    public int hashCode() {
        int result = json.hashCode();
        result = 31 * result + webpageId;
        return result;
    }

    @Basic
    @Column(
            name = "webpage_id"
    )
    public int getWebpageId() {
        return this.webpageId;
    }

    public void setWebpageId(int webpageId) {
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
    @Column(name = "url", columnDefinition="TEXT")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
