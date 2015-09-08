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
            name = "json"
    )
    public String getJson() {
        return this.json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            ProductEntity that = (ProductEntity)o;
            if(this.id != that.id) {
                return false;
            } else {
                if(this.json != null) {
                    if(!this.json.equals(that.json)) {
                        return false;
                    }
                } else if(that.json != null) {
                    return false;
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.id;
        result = 31 * result + (this.json != null?this.json.hashCode():0);
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
}
