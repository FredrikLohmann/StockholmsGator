package com.stockholmsgator.stockholmsgator.Classes;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class WikipediaSearcher {

    private String user = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    private String wikiSearch = "https://www.google.com/search?q=";
    private String wiki = "+wikipedia";
    private String encoding = "&ie=UTF-8";

    private Document document;
    private Bitmap image;

    public WikipediaSearcher (String search, String userAgent) throws IOException, ParserConfigurationException, SAXException {

        String firstSearchUrl = getFirstWikipediaUrl(search, userAgent);
        System.out.println(firstSearchUrl);

        document = getFirstWikipediaSearchAsDocument(firstSearchUrl);

        try{
            URL imageURL = new URL(getImageUrl());
            image = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        }
        catch (Exception e){
            image = null;
            e.printStackTrace();
        }
    }

    private String getFirstWikipediaUrl(String search, String userAgent) throws IOException {
        org.jsoup.nodes.Document google = Jsoup.connect(wikiSearch + URLEncoder.encode(search + wiki,"UTF8")).userAgent(userAgent).get();
        Elements elements = google.select("a[href]");
        ArrayList<String> urls = new ArrayList<>();
        for (Element e: elements) {
            String tmp = e.attr("href");
            if (tmp.startsWith("/url?q=https://sv.wikipedia.org/wiki/")){
                urls.add(tmp.split("&sa=U&ved")[0].substring(7));
                System.out.println(tmp.split("&sa=U&ved")[0].substring(7));
            }
        }
        return URLDecoder.decode(urls.get(0),"UTF8");
    }

    private Document getFirstWikipediaSearchAsDocument(String firstSearchUrl) throws IOException, SAXException, ParserConfigurationException {
        String wikipediaArticle = "https://sv.wikipedia.org/w/api.php?format=xml&action=query&prop=pageimages|extracts&pithumbsize=500&exlimit=1&titles=" +
                (firstSearchUrl.replace("https://sv.wikipedia.org/wiki/", ""));
        System.out.println(wikipediaArticle);

        URL wikipediaSearch = new URL(wikipediaArticle);
        URLConnection connection = wikipediaSearch.openConnection();
        connection.addRequestProperty("User-Agent", user);

        return buildDocumentFromStream(connection);
    }

    private Document buildDocumentFromStream(URLConnection connection) throws IOException, ParserConfigurationException, SAXException {
        InputStream in = connection.getInputStream();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(in);
        in.close();
        return doc;
    }

    public String getTitle(){
        return document.getElementsByTagName("page").item(0).getAttributes().getNamedItem("title").getTextContent();
    }

    public String getExtract(){
        return document.getElementsByTagName("extract").item(0).getTextContent();
    }

    private String getImageUrl(){
        return document.getElementsByTagName("thumbnail").item(0).getAttributes().getNamedItem("source").getTextContent();
    }

    public Bitmap getImage(){
        return image;
    }

    private String getFirstUrl(String str) {
        String[] strings = str.split("url?");

        for (String s : strings) {
            if (s.contains("https://sv.wikipedia.org/wiki/")) {
                str = s.split(";")[0];
                return str.substring(3,str.length()-4);
            }
        }
        return "No match";
    }
}