package com.wcare.android.gocoro.http;

/**
 * Created by ttonway on 2017/2/23.
 */
public class KnowledgeMessage {

    public String title;
    public String posterUrl;
    public String description;
    public String url;
    public long createdAt;

    @Override
    public String toString() {
        return "KnowledgeMessage{" +
                "title='" + title + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
