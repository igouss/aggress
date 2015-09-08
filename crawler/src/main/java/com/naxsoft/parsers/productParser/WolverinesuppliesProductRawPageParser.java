//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.google.gson.Gson;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.RawProduct;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WolverinesuppliesProductRawPageParser implements ProductParser {
    public WolverinesuppliesProductRawPageParser() {
    }

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductRawPageParser.class);
        HashSet result = new HashSet();
        Gson gson = new Gson();
        RawProduct[] rawProducts = (RawProduct[])gson.fromJson(webPageEntity.getContent(), RawProduct[].class);
        RawProduct[] var6 = rawProducts;
        int var7 = rawProducts.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            RawProduct rp = var6[var8];
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
            jsonBuilder.field("description1", rp.ExtendedDescription);

            for(int i = 0; i < rp.Attributes.length; ++i) {
                jsonBuilder.field(rp.Attributes[i].AttributeName, rp.Attributes[i].AttributeValue);
            }

            jsonBuilder.endObject();
            product.setJson(jsonBuilder.string());
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
        }

        return result;
    }

    public boolean canParse(String url, String action) {
        return url.startsWith("https://www.wolverinesupplies.com/") && action.equals("productPageRaw");
    }
}
