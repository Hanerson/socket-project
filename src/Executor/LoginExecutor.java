package Executor;

import Executor.Template.Template;
import common.HttpRequest;
import common.HttpResponse;

import java.util.HashMap;

public class LoginExecutor extends AbstractExecutor {
    public LoginExecutor(){
        this.url = "/login";
        this.method = "post";
    }
    @Override
    public HttpResponse handle(HttpRequest request) {/*
    POST /login HTTP/1.1
Host: localhost:8080
Content-Type: application/x-www-form-urlencoded; charset=UTF-8
Content-Length: 27

username=admin&password=123456*/
        HashMap<String, String> db = RegisterExecutor.map;
        HttpResponse response = null;
        HashMap<String,String> headers = new HashMap<>( request.getHeaders());
        String contentType = headers.get("Content-Type").split(";")[0].trim();
        byte[] body = request.getBody();

        if(!contentType.equals("application/x-www-form-urlencoded")){
        response = Template.generateStatusCode_400();
        }

        String[] key_val = new String(body).split("&");
        assert (key_val.length == 2);
        String username = null;
        String password = null;
        for(int i = 0; i < key_val.length; i++){
            String[] tmp = key_val[i].split("=");
            assert (tmp.length == 2);
            if(tmp[0].equals("username")){
                username = tmp[1].trim();
            }else if (tmp[0].equals("password")){
                password = tmp[1].trim();
            }
        }
        if(username == null || password == null){
            Template.generateStatusCode_400();
        }else {

            if (db.containsKey(username) && db.get(username).equals(password)) {
                String hint = "You have successfully login in!";
                response = Template.generateStatusCode_200(hint);
            } else {
                String hint = "login failed";
                response = Template.generateStatusCode_200(hint);
            }
        }

        return response;
    }



}
