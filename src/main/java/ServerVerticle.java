import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class ServerVerticle extends AbstractVerticle{

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new ServerVerticle());
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.get("/shop").handler(this::handleGetShop);
        router.get("/variety").handler(this::handleGetVariety);

        vertx.createHttpServer().requestHandler(router::accept).listen(4000);
    }

    private void handleGetShop(RoutingContext context) {
        System.out.println("shop");
    }

    private void handleGetVariety(RoutingContext context) {
        System.out.println("variety");
    }

}
