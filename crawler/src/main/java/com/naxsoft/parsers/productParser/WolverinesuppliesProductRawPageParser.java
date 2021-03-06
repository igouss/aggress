package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.gson.Gson;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class WolverinesuppliesProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductRawPageParser.class);
    private static final Map<String, String> mapping = new ListOrderedMap<>();

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

        mapping.put("rings and mounts", "misc");
        mapping.put("entry eools", "misc");
        mapping.put("rifle accessories", "misc");
    }

    public WolverinesuppliesProductRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Gson gson = new Gson();
            RawProduct[] rawProducts = gson.fromJson(webPageEntity.getContent(), RawProduct[].class);

            for (RawProduct rp : rawProducts) {
                ProductEntity product;
                String productName = null;
                String url = null;
                String regularPrice = null;
                String specialPrice = null;
                String productImage = null;
                String description = null;
                Map<String, String> attr = new HashMap<>();
                String[] category = null;

                LOGGER.trace("Parsing {}, page={}", rp.Title, webPageEntity.getUrl());

                url = "https://www.wolverinesupplies.com/ProductDetail/" + rp.ItemNumber;

                productName = rp.Title;
                productImage = "https://www.wolverinesupplies.com/images/items/Thumbnail/" + rp.ImageFile + rp.ImageExtension;
                regularPrice = "" + rp.ListPrice;
                specialPrice = "" + rp.Price;
                attr.put("unitsAvailable", "" + rp.StockAmount);
                description = rp.ExtendedDescription;
                category = getNormalizedCategories(webPageEntity);

                for (int j = 0; j < rp.Attributes.length; ++j) {
                    attr.put(
                            rp.Attributes[j].AttributeName.toLowerCase(),
                            rp.Attributes[j].AttributeValue);
                }


                product = new ProductEntity(productName, url, regularPrice, specialPrice, productImage, description, attr, category);
                result.add(product);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result)
                .doOnNext(e -> parseResultCounter.inc());
    }

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        if (mapping.containsKey(webPageEntity.getCategory().toLowerCase())) {
            return mapping.get(webPageEntity.getCategory().toLowerCase()).split(",");
        }
        LOGGER.warn("Unknown category: {}", webPageEntity.getCategory());
        return new String[]{"misc"};
    }

    @Override
    String getSite() {
        return "wolverinesupplies.com";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
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
}

