//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.google.gson.Gson;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

public class WolverinesuppliesProductRawPageParser implements ProductParser {
    private final Logger logger;

    public WolverinesuppliesProductRawPageParser() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {

        HashSet result = new HashSet();
        Gson gson = new Gson();
        RawProduct[] rawProducts = gson.fromJson(webPageEntity.getContent(), RawProduct[].class);

        for(int i = 0; i < rawProducts.length; ++i) {
            RawProduct rp = rawProducts[i];
            logger.info("Parsing " + rp.Title + ", page=" + webPageEntity.getUrl());
            ProductEntity product = new ProductEntity();
            XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
            jsonBuilder.startObject();
            jsonBuilder.field("url", "https://www.wolverinesupplies.com/ProductDetail/" + rp.ItemNumber);
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", rp.Title);
            jsonBuilder.field("category", "");
            jsonBuilder.field("productImage", "https://www.wolverinesupplies.com/images/items/Large/" + rp.ImageFile + rp.ImageExtension);
            jsonBuilder.field("regularPrice", rp.ListPrice);
            jsonBuilder.field("specialPrice", rp.Price);
            jsonBuilder.field("unitsAvailable", rp.StockAmount);
            jsonBuilder.field("description", rp.ExtendedDescription);

            for(int j = 0; j < rp.Attributes.length; ++j) {
                jsonBuilder.field(
                        rp.Attributes[j].AttributeName.toLowerCase(),
                        rp.Attributes[j].AttributeValue);
            }

            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
        }

        return result;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productPageRaw");
    }
}
