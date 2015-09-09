package com.naxsoft.ui;

import com.naxsoft.crawler.FetchClient;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Copyright NAXSoft 2015
 */
public class Designer extends JFrame {
    private JEditorPane html;
    private JButton execute;
    private JTextArea script;
    private JTextArea log;
    private JTextField url;
    private JButton fetch;
    private JPanel rootPanel;


    public Designer() throws HeadlessException {
        setTitle("Designer");
        setContentPane(rootPanel);
//        html.setContentType( "text/html" );
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        execute.addActionListener(e -> {
            try {
                String htmlString = html.getText();
                Highlighter hl = new Highlighter(script.getText(), htmlString);
                Elements elements = hl.getHighlightedHtml();
                String test = "";
                for (Element element : elements) {
                    test += element.toString();
                }
                log.setText(test);
            } catch (Exception ex) {
                log.setText(ex.toString());
            }
        });

        fetch.addActionListener(e -> {
            String url = this.url.getText();
            SwingUtilities.invokeLater(() -> {
                FetchClient fetchClient = new FetchClient();
                try {
                    Connection.Response response = fetchClient.get(url);
                    html.setText(response.body());
                } catch (IOException e1) {
                    log.setText(e1.getMessage());
                }
            });
        });
    }
}
