package Executor;

import common.HttpRequest;
import common.HttpResponse;

public class ErrorExecutor extends AbstractExecutor {
    public ErrorExecutor(){
        this.url = "/error";
        this.method = "get";
    }

    @Override
    public HttpResponse handle(HttpRequest request) throws Exception{
        // do something bad
        throw new Exception("error");
    }
}
