package com.naxsoft.http;

import com.naxsoft.common.entity.WebPageEntity;
import lombok.Value;
import org.jsoup.nodes.Document;

@Value
public class DownloadResult {
    /**
     * Requested page
     */

    WebPageEntity sourcePage;
    /**
     * Parsed result
     */
    Document document;
}