package Executor.Template;

import common.HttpResponse;

public class Template {
    public static HttpResponse generateStatusCode_200(String hint){
        HttpResponse response = new HttpResponse();
response.setHttpVersion("HTTP/1.1");
response.setStatusCode(200);
response.setStatusMessage("200 OK");

        String html_200 = "<html>\n" +
                "<head><title>200 OK</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>200 OK</h1><h2>" + hint +"</h2><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";
        response.setStringBody(html_200);

        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_200.length()));

        return new HttpResponse(response);
    }
    public static HttpResponse generateStatusCode_404(){
        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(404);
        response.setStatusMessage("404 Not Found");
        String html_404 = "<html>\n" +
                "<head><title>404 Not Found</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>404 Not Found</h1><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";
        response.setStringBody(html_404);

        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_404.length()));

        return new HttpResponse(response);



    }

    public static HttpResponse generateStatusCode_405(){
        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(405);
        response.setStatusMessage("405 Method Not Allowed");
        String html_405 = "<html>\n" +
                "<head><title>405 Not Allowed</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>405 Not Allowed</h1><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";
        response.setStringBody(html_405);

        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_405.length()));

        return new HttpResponse(response);
    }

    public static HttpResponse generateStatusCode_500(){
        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(500);
        response.setStatusMessage("500 Internal Server Error");
        String html_500 = "<html>\n" +
                "<head><title>500 Internal Server Error</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>500 Internal Server Error</h1><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";

        response.setStringBody(html_500);

        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_500.length()));

        return new HttpResponse(response);



    }
    public static HttpResponse generateStatusCode_400(){
        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(400);
        response.setStatusMessage("400 Bad Request");
        String html_400 = "<html>\n" +
                "<head><title>400 Bad Request</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>400 Bad Request</h1><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";

        response.setStringBody(html_400);

        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_400.length()));

        return new HttpResponse(response);




    }

    public static HttpResponse generateStatusCode_304(){

        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(304);
        response.setStatusMessage("304 Not Modified");

        return new HttpResponse(response);
    }

    public static HttpResponse generateStatusCode_301(String url){

        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(301);
        response.setStatusMessage("301 Moved Permanently");


        String html_301 = "<html>\n" +
                "<head><title>301 Moved Permanently</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>301 Moved Permanently</h1><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";
        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_301.length()));
        response.addHeader("Location", url);
        response.setStringBody(html_301);
        return new HttpResponse(response);

    }

    public static HttpResponse generateStatusCode_302(String url){
        HttpResponse response = new HttpResponse();
        response.setHttpVersion("HTTP/1.1");
        response.setStatusCode(30);
        response.setStatusMessage("302 Found");


        String html_302 = "<html>\n" +
                "<head><title>302 Found</title></head>\n" +
                "<body bgcolor=\"white\">\n" +
                "<center><h1>302 Found</h1><h6>simple http-server<h6></center>\n" +
                "</body>\n" +
                "</html>";
        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", Long.toString(html_302.length()));
        response.addHeader("Location", url);
        response.setStringBody(html_302);
        return new HttpResponse(response);




    }
}
