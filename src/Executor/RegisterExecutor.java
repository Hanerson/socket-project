package Executor;

import Executor.Template.Template;
import common.HttpRequest;
import common.HttpResponse;

import java.util.HashMap;

public class RegisterExecutor extends AbstractExecutor {
    public static HashMap<String, String> map = new HashMap<>();

    public RegisterExecutor() {
        this.url = "/register";
        this.method = "post";
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception {
        HashMap<String, String> Map = this.map;
        HttpResponse response = null;
        HashMap<String, String> headers = new HashMap<>(request.getHeaders());
        String contentType = headers.get("Content-Type").split(";")[0].trim();
        byte[] body = request.getBody();

        if (!contentType.equals("application/x-www-form-urlencoded")) {
            response = Template.generateStatusCode_405();
            return response;
        }

        String Body = new String(body);
        String[] key_val = Body.split("&");

        if (key_val.length != 2) {
            System.out.println("register  body部分有问题");
        }

        String username = null;
        String password = null;

        for (int i = 0; i < key_val.length; i++) {
            String[] tmp = key_val[i].split("=");
            assert (tmp.length == 2);

            if (tmp[0].equals("username")) {
                username = tmp[1].trim();
            } else if (tmp[0].equals("password")) {
                password = tmp[1].trim();
            }
        }

        if (username == null || password == null) {
            response = Template.generateStatusCode_405();
        } else {
            if (!map.containsKey(username)) {
                map.put(username, password);
                String hint = "You have successfully register!";
                response = Template.generateStatusCode_200(hint);
            } else {
                String hint = "You have successfully register!";
                response = Template.generateStatusCode_200(hint);
            }
        }

        return response;
    }
}