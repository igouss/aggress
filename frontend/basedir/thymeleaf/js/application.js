require(['jquery', 'bootstrap', "mustache"], function($, bootStrap, m){

    // DOM ready
    $(function(){
        var searchData = {
            searchKey: null
            , startFrom: 0
        };
        var prev = function() {
            searchData.startFrom -= 10;
            if (searchData.startFrom < 0) {
                searchData.startFrom = 0;
            }
            search();
        };
        var next = function() {
            searchData.startFrom += 10;
            search(function() { searchData.startFrom -= 10;});
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

        var search = function(onFailure) {
            if ($("input").val() != searchData.searchKey) {
                searchData.startFrom = 0;
            }
            searchData.searchKey = $("input").val();
            $.getJSON("http://localhost:8080/search", searchData, function(data) {
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

                $.map(data, function(element, index) {
                    //console.info(element);
                    var rendered = $(m.render(itemTemplate, element));
                    var tbody = rendered.find("tbody");

                    $.each(element, function(key, value) {
                        if (key == "productImage" || key == "url" || value=="") {

                        } else {
                            var row = $(m.render("<tr><td>{{key}}</td><td>{{value}}</td></tr>", {
                                "key": toCapitalizedWords(key),
                                "value": value
                            }));
                            tbody.append(row);
                        }
                    });

                    $('#searchResults').append(rendered);
                });
                return true;
            })
        };

        m.parse(itemTemplate);
        $("#searchBtn").on("click", search);
        $("input").keypress(function(e) {
            if(e.which == 13) {
                search();
            }
        });
    });
});
