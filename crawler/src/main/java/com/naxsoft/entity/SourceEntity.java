package com.naxsoft.entity;

import javax.persistence.*;
import java.util.Collection;

/**
 * Copyright NAXSoft 2015
 */
@Entity
@Table(name = "source", schema = "guns", catalog = "aggress")
public class SourceEntity {
    private int id;
    private String url;
    private Collection<WebPageEntity> webPagesById;

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
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @OneToMany(mappedBy = "sourceBySourceId")
    public Collection<WebPageEntity> getWebPagesById() {
        return webPagesById;
    }

    public void setWebPagesById(Collection<WebPageEntity> webPagesById) {
        this.webPagesById = webPagesById;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceEntity that = (SourceEntity) o;

        return url.equals(that.url);

    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
