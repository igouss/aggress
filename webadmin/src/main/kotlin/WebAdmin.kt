import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;

/**
 * Copyright Naxsoft
 */

fun main(args: Array<String>): Unit {
    val vertx = Vertx.vertx();
    val server = vertx.createHttpServer()
    val templateEngine = ThymeleafTemplateEngine.create()
    val router = Router.router(vertx)

    router.route("/css/*").handler(StaticHandler.create("basedir/thymeleaf/css"))
    router.route("/fonts/*").handler(StaticHandler.create("basedir/thymeleaf/fonts"))
    router.route("/img/*").handler(StaticHandler.create("basedir/thymeleaf/img"))
    router.route("/js/*").handler(StaticHandler.create("basedir/thymeleaf/js"))
    router.route("/").handler { ctx ->
        templateEngine.render(ctx, "templates", "index.html", { res ->
            if (res.succeeded()) {
                ctx.response().end(res.result())
            } else {
                ctx.fail(res.cause())
            }
        })
    }
    server.requestHandler { req -> router.accept(req) }
    server.listen(8081)
}