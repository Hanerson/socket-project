package Server.dispatcher;

import Executor.AbstractExecutor;
import Executor.ErrorExecutor;
import Executor.LoginExecutor;
import Executor.RegisterExecutor;
import Executor.StaticResourceExecutor;
import Executor.Template.Template;
import common.HttpRequest;
import common.HttpResponse;

import java.util.ArrayList;

/**
 * [临时替身] Role C 的分发器
 * 目前仅用于测试 Role B 是否能正常接收请求并调用分发器。
 */
public class RequestDispatcher {

    public HttpResponse dispatch(HttpRequest request) throws Exception {
        String target = request.getUri();
        String method = request.getMethod();
        HttpResponse response = null;
        AbstractExecutor executor = null;

        ArrayList<AbstractExecutor> executors = new ArrayList<>();
        executors.add(new ErrorExecutor());
        executors.add(new LoginExecutor());
        executors.add(new RegisterExecutor());

        // 处理静态资源请求
        if (StaticResourceExecutor.isStaticTarget(target) && method.toLowerCase().equals("get")) {
            executor = new StaticResourceExecutor();
        } else {
            // 在持有的executor中找到匹配的处理器
            for (AbstractExecutor e : executors) {
                if (target.endsWith(e.getUrl()) && method.toLowerCase().equals(e.getMethod().toLowerCase())) {
                    executor = e;
                    break;
                }
            }
        }

        // 根据匹配结果生成响应
        if (executor == null) {
            response = Template.generateStatusCode_404();

            // todo 针对post静态资源会出现bug，不一定是404
            for (AbstractExecutor e : executors) {
                if (target.endsWith(e.getUrl())) {
                    response = Template.generateStatusCode_405();
                    break;
                }
            }
        } else {
            response = executor.handle(request);
        }

        return new HttpResponse(response);
    }
}