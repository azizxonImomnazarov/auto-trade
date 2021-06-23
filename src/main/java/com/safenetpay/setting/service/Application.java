package com.safenetpay.setting.service;

import com.safenetpay.common.error.Error;
import com.safenetpay.common.throwable.ApplicationRuntimeException;
import com.safenetpay.setting.service.verticle.SettingServiceVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import org.apache.log4j.Logger;

public class Application extends AbstractVerticle {

  private static final Logger LOGGER = Logger.getLogger("Application");

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    LOGGER.info("init: Application start");
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(
        res -> {
          try {
            if (res.failed()) {
              throw new ApplicationRuntimeException("fail to load config", Error.APPLICATION);
            } else {
              startPromise.complete();
              vertx.deployVerticle(
                  new SettingServiceVerticle(), new DeploymentOptions().setConfig(res.result()));
              LOGGER.info("init: Application started");
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
}
