//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.google.gson.Gson;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WolverinesuppliesProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductRawPageParser.class);
    private static Map<String, String> mapping = new ListOrderedMap<>();

    static {
        mapping.put("Rifles", "firearm");
        mapping.put("Surplus Rifles", "firearm");
        mapping.put("Rimfire Rifles", "firearm");
        mapping.put("Muzzleloaders", "firearm");
        mapping.put("Hunting Rifles", "firearm");
        mapping.put("Big Game Rifles", "firearm");
        mapping.put("Tactical Rifles", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Tactical Shotguns", "firearm");
        mapping.put("Hunting Shotguns", "firearm");
        mapping.put("Handguns", "firearm");
        mapping.put("Surplus Handguns", "firearm");
        mapping.put("Revolvers", "firearm");
        mapping.put("Antique & Misc. Handguns", "firearm");
        mapping.put("Semi Auto Handguns", "firearm");
        mapping.put("Special Purpose", "firearm");
        mapping.put("Airguns over 500FPS", "firearm");
        mapping.put("Airguns under 500FPS", "firearm");
        mapping.put("OEM Parts", "misc");


        mapping.put("Hunting Scopes", "optic");
        mapping.put("Tactical Scopes-sights", "optic");
        mapping.put("Rimfire Scopes", "optic");
        mapping.put("Nightvision", "optic");
        mapping.put("Optics Accessories", "optic");
        mapping.put("Observation", "optic");
        mapping.put("Trail Camera", "optic");
        mapping.put("Spotting Scopes", "optic");
        mapping.put("Rangefinders", "optic");
        mapping.put("Binoculars", "optic");
        mapping.put("Mounting", "optic");
        mapping.put("Scope Rings", "optic");
        mapping.put("Scope Bases", "optic");


        mapping.put("Muzzleloading", "ammo");
        mapping.put("Air Gun Pellets", "ammo");
        mapping.put("Handgun Ammo", "ammo");
        mapping.put("Practice Ammo", "ammo");
        mapping.put("Rimfire Ammo", "ammo");

        mapping.put("Reloading", "reload");
        mapping.put("Reloading Components", "reload");
        mapping.put("Reloading Equipment", "reload");

        mapping.put("Rifle Ammo", "ammo");
        mapping.put("Premium Rifle Ammo", "ammo");
        mapping.put("Hunting Rifle Ammo", "ammo");
        mapping.put("FMJ Rifle Ammo", "ammo");
        mapping.put("Big Game Rifle Ammo", "ammo");
        mapping.put("Surplus Rifle Ammo", "ammo");
        mapping.put("Shotgun Ammo", "ammo");
        mapping.put("Shotgun Ammo -Steel", "ammo");
        mapping.put("Shotgun Ammo -Lead", "ammo");
  }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();
        Gson gson = new Gson();
        RawProduct[] rawProducts = gson.fromJson(webPageEntity.getContent(), RawProduct[].class);

        for (RawProduct rp : rawProducts) {
            LOGGER.info("Parsing {}, page={}", rp.Title, webPageEntity.getUrl());
            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", "https://www.wolverinesupplies.com/ProductDetail/" + rp.ItemNumber);
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", rp.Title);
                jsonBuilder.field("productImage", "https://www.wolverinesupplies.com/images/items/Large/" + rp.ImageFile + rp.ImageExtension);
                jsonBuilder.field("regularPrice", rp.ListPrice);
                jsonBuilder.field("specialPrice", rp.Price);
                jsonBuilder.field("unitsAvailable", rp.StockAmount);
                jsonBuilder.field("description", rp.ExtendedDescription);
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));

                for (int j = 0; j < rp.Attributes.length; ++j) {
                    jsonBuilder.field(
                            rp.Attributes[j].AttributeName.toLowerCase(),
                            rp.Attributes[j].AttributeValue);
                }

                jsonBuilder.endObject();
                product.setUrl(webPageEntity.getUrl());
                product.setJson(jsonBuilder.string());
            }
            product.setWebpageId(webPageEntity.getId());
            result.add(product);
        }

        return result;
    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        for (String category : mapping.keySet()) {
            if (webPageEntity.getUrl().contains(category)) {
                return mapping.get(category).split(",");
            }
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productPageRaw");
    }
}

/**
 *
 */
class RawProduct {
    Attributes[] Attributes;
    String ItemNumber;
    int StockAmount;
    double ListPrice;
    String RenderedListPrice;
    double Price;
    String RenderedPrice;
    String ImageFile;
    String ImageExtension;
    String ImageSize;
    String ExtendedDescription;
    String Title;

    RawProduct() {
    }
}

/**
 *
 */
class Attributes {
    String SearchType;
    String AttributeName;
    String AttributeValue;

    Attributes() {
    }
}
