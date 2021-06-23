package com.safenetpay.setting.service.verticle;

import com.safenetpay.common.error.Error;
import com.safenetpay.common.throwable.ApplicationRuntimeException;
import com.safenetpay.setting.service.router.SettingServiceRouter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import org.apache.log4j.Logger;

public class SettingServiceVerticle extends AbstractVerticle {

  private static final Logger LOGGER = Logger.getLogger(SettingServiceVerticle.class);
  private static final String PAYMENT_SERVICE = "SettingService";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    JsonObject config = this.config();
    JsonObject settingServiceConfig = config.getJsonObject(PAYMENT_SERVICE);

    OpenAPI3RouterFactory.create(
        vertx,
        "webroot/safenetpay.openapi.yaml",
        asyncResult -> {
          if (!asyncResult.succeeded()) {
            LOGGER.error(asyncResult.cause().getMessage());
            startPromise.fail(asyncResult.cause());
          } else {

            vertx
                .createHttpServer()
                .requestHandler(
                    new SettingServiceRouter(vertx, asyncResult.result()).createRouting())
                .listen(
                    settingServiceConfig.getJsonObject("http").getInteger("port"),
                    settingServiceConfig.getJsonObject("http").getString("host"),
                    http -> {
                      try {
                        if (http.succeeded()) {
                          startPromise.complete();
                          LOGGER.info(
                              String.format(
                                  "HTTP server started on port %d",
                                  settingServiceConfig.getJsonObject("http").getInteger("port")));
                        } else {
                          startPromise.fail(http.cause());
                          throw new ApplicationRuntimeException(
                              String.format(
                                  "HTTP server start failed on port %d",
                                  settingServiceConfig.getJsonObject("http").getInteger("port")),
                              Error.APPLICATION);
                        }
                      } catch (ApplicationRuntimeException appEX) {
                        LOGGER.error(appEX.toString());
                        startPromise.fail(appEX.getMessage());
                      } catch (RuntimeException e) {
                        LOGGER.error(String.format("unhandled exception: %s", e.getMessage()));
                        startPromise.fail(e.getMessage());
                      }
                    });
          }
        });
  }
}
