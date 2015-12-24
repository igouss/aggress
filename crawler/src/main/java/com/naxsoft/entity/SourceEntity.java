//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(
        name = "source",
        schema = "guns",
        catalog = "aggress"
)
public class SourceEntity {
    private int id;
    private String url;
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
            name = "url",
            columnDefinition = "TEXT"
    )
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            SourceEntity that = (SourceEntity) o;
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
