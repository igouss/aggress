//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.entity;

import com.naxsoft.entity.WebPageEntity;
import java.sql.Timestamp;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(
        name = "source",
        schema = "guns",
        catalog = "aggress"
)
public class SourceEntity {
    private int id;
    private String url;
    private Collection<WebPageEntity> webPagesById;
    private boolean enabled;
    private Timestamp modificationDate;

    public SourceEntity() {
    }

    @Basic
    @Column(
            name = "enabled"
    )
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
            name = "url"
    )
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @OneToMany(
            mappedBy = "sourceBySourceId"
    )
    public Collection<WebPageEntity> getWebPagesById() {
        return this.webPagesById;
    }

    public void setWebPagesById(Collection<WebPageEntity> webPagesById) {
        this.webPagesById = webPagesById;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            SourceEntity that = (SourceEntity)o;
            return this.url.equals(that.url);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.url.hashCode();
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
}
