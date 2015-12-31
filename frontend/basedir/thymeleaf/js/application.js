require(['jquery', 'bootstrap', "mustache"], function($, bootStrap, m){

    // DOM ready
    $(function(){
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
            searchKey: null
            , startFrom: 0
        };
        var prev = function(e) {
            searchData.startFrom -= 30;
            if (searchData.startFrom < 0) {
                searchData.startFrom = 0;
            }
            e.preventDefault();
            search();
        };
        var next = function(e) {
            searchData.startFrom += 30;
            e.preventDefault();
            search(function() { searchData.startFrom -= 30;});
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

        var inputField = $("input");
        var search = function(onFailure) {
            if (inputField.val() != searchData.searchKey) {
                searchData.startFrom = 0;
            }
            var searchField = $("#searchInput");
            var pageName = "?search=" + encodeURIComponent(searchField.val()) + "&startFrom=" + searchData.startFrom;
            var title = searchField.val() + " " + searchData.startFrom;
            window.history.pushState(searchData, title, pageName);

            searchData.searchKey = inputField.val();
            $.getJSON("/search", searchData, function(data) {
                if (searchData.startFrom != 0 && data.length == 0) {
                    if (onFailure) {
                        onFailure();
                    }
                    return false;
                }
                $(".renderedItem").remove();

                if(0 != data.length) {
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

                $.map(data, function(element) {
                    //console.info(element);
                    var rendered = $(m.render(itemTemplate, element));
                    var tbody = rendered.find("tbody");

                    $.each(element, function(key, value) {
                        if (key == "productImage" || key == "url" || value=="") {

                        } else {
                            if (key != "productName") {
                                var rowHtml = "";
                                if (key == "specialPrice") {
                                    rowHtml = "<tr class='info'><td>{{key}}</td><td>{{value}}</td></tr>";
                                } else {
                                    rowHtml = "<tr><td>{{key}}</td><td>{{value}}</td></tr>";
                                }
                                var row = $(m.render("<tr><td>{{key}}</td><td>{{value}}</td></tr>", {
                                    "key": toCapitalizedWords(key),
                                    "value": value
                                }));
                                tbody.append(row);
                            }
                        }
                    });

                    $('#searchResults').append(rendered);
                });
                $("html, body").animate({ scrollTop: 0 }, "slow");
                return true;
            })
        };

        m.parse(itemTemplate);
        $("#searchBtn").on("click", function() {
            searchData.startFrom = 0;
            search();
        });
        inputField.keypress(function(e) {
            if(e.which == 13) {
                searchData.startFrom = 0;
                search();
            }
        });
        searchData.searchKey = getUrlParameter("search");
        inputField.val(searchData.searchKey);
        searchData.startFrom = parseInt(getUrlParameter("startFrom"), 30);
        if (searchData.searchKey) {
            if (!searchData.startFrom) {
                searchData.startFrom = 0;
            }
            search();
        }
    });
});
