package com.naxsoft.http;

import com.naxsoft.common.entity.WebPageEntity;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class JsonResult {
    /**
     * Requested page
     */
    WebPageEntity sourcePage;
    /**
     * Parsed result
     */
    Map<String, List<Map<String, String>>> json;
}