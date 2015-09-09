package com.naxsoft.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Highlighter {
    private String script;
    private String htmlContent;


    public Highlighter(String script, String html) {
        this.script = script;// buildRegexFromQuery(searchString);
        htmlContent = html;
    }

    public Elements getHighlightedHtml() {

        Document doc = Jsoup.parse(htmlContent);
        groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell();
        shell.setVariable("document", doc);
        Elements rc = (Elements) shell.evaluate(script);
        return rc;

//        final List<TextNode> nodesToChange = new ArrayList<TextNode>();
//
//        NodeTraversor nd  = new NodeTraversor(new NodeVisitor() {
//
//            @Override
//            public void tail(Node node, int depth) {
//                if (node instanceof TextNode) {
//                    TextNode textNode = (TextNode) node;
//                    String text = textNode.getWholeText();
//
//                    mat = pat.matcher(text);
//
//                    if(mat.find()) {
//                        nodesToChange.add(textNode);
//                    }
//                }
//            }
//
//            @Override
//            public void head(Node node, int depth) {
//            }
//        });
//
//        nd.traverse(doc.body());
//
//        for (TextNode textNode : nodesToChange) {
//            Node newNode = buildElementForText(textNode);
//            textNode.replaceWith(newNode);
//        }
//        return doc.toString();
    }

}
