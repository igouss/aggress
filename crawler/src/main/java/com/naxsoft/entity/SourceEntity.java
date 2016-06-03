package com.naxsoft.entity;

import com.google.gson.Gson;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *
 */
@Entity
@Table(
        name = "source",
        schema = "guns",
        catalog = "aggress"
)
public class SourceEntity {
    /**
     *
     */
    private Long id;

    /**
     *
     */
    private String url;

    /**
     *
     */
    private boolean enabled;

    /**
     *
     */
    private Timestamp modificationDate;

    public SourceEntity() {
    }

    /**
     * Convert from JSON representation
     *
     * @param json SourceEntity in JSON format
     * @return SourceEntity object
     */
    public static SourceEntity fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, SourceEntity.class);
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
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
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

    /**
     * Get JSON representation
     *
     * @return JSON representation
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
