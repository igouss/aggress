package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class WolverinesuppliesProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinesuppliesProductRawPageParser.class);
    private static final Map<String, String> mapping = ImmutableMap.<String, String>builder()
            .put("rifles", "firearm")
            .put("surplus rifles", "firearm")
            .put("rimfire rifles", "firearm")
            .put("muzzleloaders", "firearm")
            .put("hunting rifles", "firearm")
            .put("big game rifles", "firearm")
            .put("tactical rifles", "firearm")
            .put("shotguns", "firearm")
            .put("tactical shotguns", "firearm")
            .put("hunting shotguns", "firearm")
            .put("handguns", "firearm")
            .put("surplus handguns", "firearm")
            .put("revolvers", "firearm")
            .put("antique & misc. handguns", "firearm")
            .put("semi auto handguns", "firearm")
            .put("special purpose", "firearm")
            .put("airguns over 500fps", "firearm")
            .put("airguns under 500fps", "firearm")
            .put("oem parts", "misc")
            .put("semi-auto handguns", "firearm")
            .put("commemorative", "firearm")
            .put("antique handguns", "firearm")
            .put("hunting scopes", "optic")
            .put("tactical scopes-sights", "optic")
            .put("rimfire scopes", "optic")
            .put("nightvision", "optic")
            .put("optics accessories", "optic")
            .put("observation", "optic")
            .put("trail camera", "optic")
            .put("spotting scopes", "optic")
            .put("rangefinders", "optic")
            .put("binoculars", "optic")
            .put("mounting", "optic")
            .put("scope rings", "optic")
            .put("scope bases", "optic")
            .put("muzzleloading", "ammo")
            .put("air gun pellets", "ammo")
            .put("handgun ammo", "ammo")
            .put("practice ammo", "ammo")
            .put("rimfire ammo", "ammo")
            .put("reloading", "reload")
            .put("reloading components", "reload")
            .put("reloading equipment", "reload")
            .put("rifle ammo", "ammo")
            .put("premium rifle ammo", "ammo")
            .put("hunting rifle ammo", "ammo")
            .put("fmj rifle ammo", "ammo")
            .put("big game rifle ammo", "ammo")
            .put("surplus rifle ammo", "ammo")
            .put("shotgun ammo", "ammo")
            .put("shotgun ammo -steel", "ammo")
            .put("shotgun ammo -lead", "ammo")
            .put("rings and mounts", "misc")
            .put("entry eools", "misc")
            .put("rifle accessories", "misc")
            .build();


    public WolverinesuppliesProductRawPageParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Collection<ProductEntity> parse(WebPageEntity webPageEntity) {
        ImmutableSet.Builder<ProductEntity> result = ImmutableSet.builder();

        try {
            Gson gson = new Gson();
            RawProduct[] rawProducts = gson.fromJson(webPageEntity.getContent(), RawProduct[].class);

            for (RawProduct rp : rawProducts) {
                ProductEntity product;
                String productName;
                String url;
                String regularPrice;
                String specialPrice = null;
                String productImage;
                String description;
                Map<String, String> attr = new HashMap<>();
                String[] category;

                LOGGER.trace("Parsing {}, page={}", rp.Title, webPageEntity.getUrl());

                url = "https://www.wolverinesupplies.com/ProductDetail/" + rp.ItemNumber;

                productName = rp.Title;
                productImage = "https://www.wolverinesupplies.com/images/items/Thumbnail/" + rp.ImageFile + rp.ImageExtension;
                regularPrice = "" + rp.ListPrice;
                if (rp.Price != rp.ListPrice) {
                    specialPrice = "" + rp.Price;
                }
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
        return result.build();
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

