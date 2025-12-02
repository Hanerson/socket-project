package Executor;

import common.HttpRequest;
import common.HttpResponse;

public abstract class AbstractExecutor {
    String url;
    String method;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public abstract HttpResponse handle(HttpRequest request) throws Exception;
}