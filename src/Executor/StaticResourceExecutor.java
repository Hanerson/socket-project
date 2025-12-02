package Executor;

import Executor.Template.Template;
import common.HttpRequest;
import common.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class StaticResourceExecutor extends AbstractExecutor {
    public static HashMap<String, String> MovedPermanentlyResource = new HashMap<>();
    public static HashMap<String, String> MovedTemporarilyResource = new HashMap<>();
    // todo:304状态码
    public static HashMap<String, String> ModifiedTime = new HashMap<>();

    public StaticResourceExecutor() {
        MovedPermanentlyResource.put("/movedPic.png", "/pic.png");
        MovedPermanentlyResource.put("/movedIndex.html", "/index.html");
        MovedTemporarilyResource.put("/movedPic2.png", "/pic.png");
        MovedTemporarilyResource.put("/movedIndex2.html", "/index.html");
    }

    public static boolean isStaticTarget(String target) {
        target = target.substring(target.lastIndexOf("/") + 1);
        return target.contains(".");
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception {
        HttpResponse response = new HttpResponse();
        HashMap<String, String> headers = new HashMap<>(request.getHeaders());
        String target = request.getUri();
        byte[] body = null;

        if (MovedPermanentlyResource.containsKey(target)) {
            return Template.generateStatusCode_301(MovedPermanentlyResource.get(target));
        } else if (MovedTemporarilyResource.containsKey(target)) {
            return Template.generateStatusCode_302(MovedTemporarilyResource.get(target));
        } else {
            response.setHttpVersion("HTTP/1.1");
            response.setStatusCode(200);
            response.setStatusMessage("OK");
        }

        if (target.endsWith(".html")) {
            response.addHeader("Content-Type", "text/html");
        } else if (target.endsWith(".png")) {
            response.addHeader("Content-Type", "image/png");
        } else if (target.endsWith(".js")) {
            response.addHeader("Content-Type", " text/javascript");
        }

        String path = target.substring(target.lastIndexOf("/") + 1);
        File f = new File(path);
        response.addHeader("Content-Length", Long.toString(f.length()));

        Date fileLastModifiedTime = new Date(f.lastModified());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 为时间格式化器 sdf 设置时区为 GMT（格林尼治标准时间）。(HTTP要求)
        System.out.println(sdf.format(fileLastModifiedTime));
        response.addHeader("Last-Modified", sdf.format(fileLastModifiedTime));

        String time = headers.get("If-Modified-Since");
        if (time != null) {
            Date Limit = sdf.parse(time);
            if (Limit.compareTo(fileLastModifiedTime) > 0) {
                return Template.generateStatusCode_304();
            }
        }

        byte[] bytesArray = new byte[(int) f.length()];
        try {
            FileInputStream fis = new FileInputStream(f);
            fis.read(bytesArray); // read file into bytes[]
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpResponse("HTTP/1.1", 404, "Not Found", new HashMap<>(), new byte[0]);
        }

        body = bytesArray;
        response.setBody(body);
        return new HttpResponse(response);
    }
}