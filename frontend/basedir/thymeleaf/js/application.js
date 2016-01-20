requirejs.config({
    shim: {
        "bootstrap": {
            deps: ["jquery"]
        }
    },
    paths: {
        "jquery": "jquery-2.1.4.min",
        "bootstrap": "bootstrap.min"
    }
});

require(['jquery', 'bootstrap', 'mustache'], function ($, bootStrap, m) {

    // DOM ready
    $(function () {
        var getUrlParameter = function getUrlParameter(sParam) {
            var sURLVariables = window.location.search.substring(1).split('&'),
                sParameterName,
                i;

            for (i = 0; i < sURLVariables.length; i++) {
                sParameterName = sURLVariables[i].split('=');

                if (sParameterName[0] === sParam) {
                    return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
                }
            }
        };


        var searchData = {
            searchKey: null,
            categoryKey: null,
            startFrom: 0
        };

        var prev = function (e) {
            searchData.startFrom -= 30;
            if (searchData.startFrom < 0) {
                searchData.startFrom = 0;
            }
            e.preventDefault();
            search();
        };
        var next = function (e) {
            searchData.startFrom += 30;
            e.preventDefault();
            search(function () {
                searchData.startFrom -= 30;
            });
        };
        var itemTemplate = $('#itemTemplate').html();
        var navTemplate = $('#navTemplate').html();

        function toCapitalizedWords(name) {
            var words = name.match(/[A-Za-z][a-z]*/g);
            return words.map(capitalize).join(" ");
        }

        function capitalize(word) {
            return word.charAt(0).toUpperCase() + word.substring(1);
        }

        var inputField = $("input#searchInput");
        var categoryField = $("select#category");



        var parseData = function (data) {
            if (searchData.startFrom != 0 && data.length == 0) {
                return false;
            }
            $(".renderedItem").remove();

            if (0 != data.length) {
                if ($("nav").length == 0) {
                    $("#navigation").append(m.render(navTemplate, {}));
                    $("#prev").on("click", prev);
                    $("#next").on("click", next);
                }
            } else {
                searchData.startFrom = 0;
                $("#prev").off("click", prev);
                $("#next").off("click", next);
                $("nav").remove();
            }

            $.map(data, function (element) {
                //console.info(element);
                var rendered = $(m.render(itemTemplate, element));
                var tbody = rendered.find("tbody");

		if (typeof(element.productImage) == 'undefined') {
			element.productImage = "https://pbs.twimg.com/profile_images/600060188872155136/st4Sp6Aw.jpg";
		}

                $.each(element, function (key, value) {
                    if (key == "productImage" || key == "productName" || key == "url" || value == "") {

                    } else {
                        var rowHtml = "";
                        if (key == "specialPrice") {
                            rowHtml = "<tr class='info'><td>{{key}}</td><td>{{value}}</td></tr>";
                        } else {
                            rowHtml = "<tr><td>{{key}}</td><td>{{value}}</td></tr>";
                        }
                        var row = $(m.render(rowHtml, {
                            "key": toCapitalizedWords(key),
                            "value": value
                        }));
                        tbody.append(row);
                    }
                });

                $('#searchResults').append(rendered);
            });
            $("html, body").animate({
                scrollTop: 0
            }, "slow");
            return true;
        };


        var tmpData = [{"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16555-111727?wid=460&hei=460","regularPrice":"31.99","specialPrice":"","url":"http://www.cabelas.ca/product/29451/browning-1911-22-holster?CatId=2116","productName":"Browning 1911-22 Holster"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/41451-123364?wid=460&hei=460","regularPrice":"849.99","specialPrice":"","url":"http://www.cabelas.ca/product/44363/remington-1911-r1-pistol","productName":"Remington 1911 R1 Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16555-111727?wid=460&hei=460","regularPrice":"31.99","specialPrice":"","url":"http://www.cabelas.ca/product/29451/browning-1911-22-holster","productName":"Browning 1911-22 Holster"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/21616-132174?wid=460&hei=460","regularPrice":"1099.99","specialPrice":"999.99","url":"http://www.cabelas.ca/product/33607/sig-sauer-1911-xo-pistol","productName":"SIG Sauer 1911 XO Pistol"},
            {"regularPrice":"1279.99","specialPrice":"","url":"http://www.cabelas.ca/product/3392/kimber-1911-custom-ii-pistol","productName":"Kimber 1911 Custom II Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/37999-120467?wid=460&hei=460","regularPrice":"39.99","specialPrice":"","url":"http://www.cabelas.ca/product/41342/gsg-22-lr-1911-10-round-magazine","productName":"GSG .22 LR 1911 10 Round Magazine"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/21617-146759?wid=460&hei=460","regularPrice":"1199.99","specialPrice":"","url":"http://www.cabelas.ca/product/33608/sig-sauer-1911-stx-2-tone-pistol","productName":"SIG Sauer 1911 STX 2-Tone Pistol"},
            {"regularPrice":"434.99","url":"https://www.canadaammo.com/product/detail/gsg-1911-standard-desert-tan-22lr-pistol/","productName":"GSG 1911 Standard Desert Tan .22LR Pistol"},
            {"regularPrice":"21.99","specialPrice":"","url":"http://www.cabelas.ca/product/40644/hogue-1911-govt-rubber-pistol-grip","productName":"Hogue 1911 Govt Rubber Pistol Grip"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/21618-160801?wid=460&hei=460","regularPrice":"1199.99","specialPrice":"","url":"http://www.cabelas.ca/product/33609/sig-sauer-1911-tactical-operations-pistol","productName":"SIG Sauer 1911 Tactical Operations Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/37995-151654?wid=460&hei=460","regularPrice":"419.99","specialPrice":"","url":"http://www.cabelas.ca/product/41339/gsg-1911-semi-auto-rimfire-pistol","productName":"GSG 1911 Semi-Auto Rimfire Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16542-111723?wid=460&hei=460","regularPrice":"39.99","specialPrice":"","url":"http://www.cabelas.ca/product/29442/browning-1911-a1-22lr-pistol-magazine?CatId=2117","productName":"Browning® 1911 A1 .22LR Pistol Magazine"},
            {"productImage":"https://www.canadaammo.com/uploads/image-cache/600x280x2x0/product-images/img_5319-1.jpg","regularPrice":"349.99","url":"https://www.canadaammo.com/product/detail/dominion-arms-1911-chrome/","productName":"1911 Chrome .45 ACP 5\" Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16157-132560?wid=460&hei=460","regularPrice":"1249.99","specialPrice":"","url":"http://www.cabelas.ca/product/29149/smith-wesson-1911-stainless-steel-pistol","productName":"Smith & Wesson 1911 Stainless Steel Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16542-111723?wid=460&hei=460","regularPrice":"39.99","specialPrice":"","url":"http://www.cabelas.ca/product/29442/browning-1911-a1-22lr-pistol-magazine","productName":"Browning® 1911 A1 .22LR Pistol Magazine"},
            {"productImage":"https://www.canadaammo.com/uploads/image-cache/600x280x2x0/product-images/img_5334-1.jpg","regularPrice":"349.99","url":"https://www.canadaammo.com/product/detail/dominion-arms-1911-two-tone/","productName":"1911 Two Tone .45 ACP 5\" Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/23062-162820?wid=460&hei=460","regularPrice":"1679.99","specialPrice":"1579.99","url":"http://www.cabelas.ca/product/34588/kimber-1911-stainless-target-ii-pistol","productName":"Kimber 1911 Stainless Target II Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/98515-159584?wid=460&hei=460","regularPrice":"1719.99","specialPrice":"","url":"http://www.cabelas.ca/product/76816/sig-sauer-1911-stainless-super-target-semi-auto-pistol","productName":"SIG Sauer 1911 Stainless Super Target Semi-Auto Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16479-152778?wid=460&hei=460","regularPrice":"649.99","specialPrice":"","url":"http://www.cabelas.ca/product/29387/browning-1911-a1-22-lr-semi-auto-pistol","productName":"Browning® 1911 A1 .22 LR Semi-Auto Pistol"},
            {"productImage":"https://www.canadaammo.com/uploads/image-cache/600x280x2x0/product-images/gsg_1911_od_us_version-thumb.png","regularPrice":"434.99","url":"https://www.canadaammo.com/product/detail/gsg-1911-standard-olive-drab-green-22lr-pistol/","productName":"GSG 1911 Standard Olive Drab Green .22LR Pistol"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/16167-111590?wid=460&hei=460","regularPrice":"47.99","specialPrice":"","url":"http://www.cabelas.ca/product/29154/smith-wesson-1911-45-acp-magazine-8-rds","productName":"Smith & Wesson 1911 .45 ACP Magazine - 8 Rds"},
            {"productImage":"http://s7d2.scene7.com/is/image/CabelasCA/66119-140588?wid=460&hei=460","regularPrice":"619.99","specialPrice":"","url":"http://www.cabelas.ca/product/66007/browning-1911-a1-22-lr-semi-auto-pistol-desert-tan","productName":"Browning 1911 A1 .22 LR Semi-Auto Pistol - Desert Tan"}];
        parseData(tmpData);

        var search = function () {
            if (inputField.val() != searchData.searchKey) {
                searchData.startFrom = 0;
            }

            searchData.searchKey = inputField.val();
            if (searchData.categoryKey != categoryField.val()) {
                searchData.startFrom = 0;
            }
            searchData.categoryKey = categoryField.val();

            var pageName = "?search=" + encodeURIComponent(searchData.searchKey) + "&category=" + searchData.categoryKey + "&startFrom=" + searchData.startFrom;
            var title = searchData.categoryKey + ": " + searchData.searchKey;
            window.history.pushState(searchData, title, pageName);
            $.getJSON("/search", searchData, parseData);
        };

        m.parse(itemTemplate);
        $("#searchBtn").on("click", function () {
            searchData.startFrom = 0;
            search();
        });
        inputField.keypress(function (e) {
            if (e.which == 13) {
                searchData.startFrom = 0;
                search();
            }
        });

        $("#category").select();

        searchData.searchKey = getUrlParameter("search");
        searchData.categoryKey = getUrlParameter("category");
        if (searchData.searchKey) {
            inputField.val(searchData.searchKey);
        }
        if (searchData.categoryKey) {
            categoryField.val(searchData.categoryKey);
        }
        searchData.startFrom = parseInt(getUrlParameter("startFrom"), 10);
        if (searchData.searchKey) {
            if (!searchData.startFrom) {
                searchData.startFrom = 0;
            }
            search();
        }
    });
});
