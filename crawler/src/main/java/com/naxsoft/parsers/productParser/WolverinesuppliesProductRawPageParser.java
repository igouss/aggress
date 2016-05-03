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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WolverinesuppliesProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductRawPageParser.class);
    private static Map<String, String> mapping = new ListOrderedMap<>();

    static {
        mapping.put("rifles", "firearm");
        mapping.put("surplus rifles", "firearm");
        mapping.put("rimfire rifles", "firearm");
        mapping.put("muzzleloaders", "firearm");
        mapping.put("hunting rifles", "firearm");
        mapping.put("big game rifles", "firearm");
        mapping.put("tactical rifles", "firearm");
        mapping.put("shotguns", "firearm");
        mapping.put("tactical shotguns", "firearm");
        mapping.put("hunting shotguns", "firearm");
        mapping.put("handguns", "firearm");
        mapping.put("surplus handguns", "firearm");
        mapping.put("revolvers", "firearm");
        mapping.put("antique & misc. handguns", "firearm");
        mapping.put("semi auto handguns", "firearm");
        mapping.put("special purpose", "firearm");
        mapping.put("airguns over 500fps", "firearm");
        mapping.put("airguns under 500fps", "firearm");
        mapping.put("oem parts", "misc");

        mapping.put("semi-auto handguns", "firearm");
        mapping.put("commemorative", "firearm");
        mapping.put("antique handguns", "firearm");


        mapping.put("hunting scopes", "optic");
        mapping.put("tactical scopes-sights", "optic");
        mapping.put("rimfire scopes", "optic");
        mapping.put("nightvision", "optic");
        mapping.put("optics accessories", "optic");
        mapping.put("observation", "optic");
        mapping.put("trail camera", "optic");
        mapping.put("spotting scopes", "optic");
        mapping.put("rangefinders", "optic");
        mapping.put("binoculars", "optic");
        mapping.put("mounting", "optic");
        mapping.put("scope rings", "optic");
        mapping.put("scope bases", "optic");


        mapping.put("muzzleloading", "ammo");
        mapping.put("air gun pellets", "ammo");
        mapping.put("handgun ammo", "ammo");
        mapping.put("practice ammo", "ammo");
        mapping.put("rimfire ammo", "ammo");

        mapping.put("reloading", "reload");
        mapping.put("reloading components", "reload");
        mapping.put("reloading equipment", "reload");

        mapping.put("rifle ammo", "ammo");
        mapping.put("premium rifle ammo", "ammo");
        mapping.put("hunting rifle ammo", "ammo");
        mapping.put("fmj rifle ammo", "ammo");
        mapping.put("big game rifle ammo", "ammo");
        mapping.put("surplus rifle ammo", "ammo");
        mapping.put("shotgun ammo", "ammo");
        mapping.put("shotgun ammo -steel", "ammo");
        mapping.put("shotgun ammo -lead", "ammo");
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

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        if (mapping.containsKey(webPageEntity.getCategory().toLowerCase())) {
            return mapping.get(webPageEntity.getCategory().toLowerCase()).split(",");
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
