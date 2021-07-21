package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Instant;

public class News {
    private Integer id;
    private String title;
    private String content;
    private String url;
    private Instant createdAt;
    private Instant modifiedAt;

    public News() {
    }

    public News(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public News(News original) {
        this.id = original.id;
        this.title = original.title;
        this.content = original.content;
        this.url = original.url;
        this.createdAt = original.createdAt;
        this.modifiedAt = original.modifiedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCreatedAt() {
        return Instant.ofEpochSecond(createdAt.getEpochSecond());
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return Instant.ofEpochSecond(modifiedAt.getEpochSecond());
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                '}';
    }
}
