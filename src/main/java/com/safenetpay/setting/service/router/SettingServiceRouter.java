package com.safenetpay.setting.service.router;

import com.safenetpay.setting.service.controller.SettingServiceController;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * SettingServiceRouter class. @Author azizxon
 *
 * @since 2021-06-17
 */
public class SettingServiceRouter {

  private static final Logger LOGGER = Logger.getLogger(SettingServiceRouter.class);
  private Router router;
  private Vertx vertx;
  private SettingServiceController settingServiceController;
  private OpenAPI3RouterFactory routerFactory;

  /**
   * Constructor SettingServiceRouter.
   *
   * @param vertx - default vertex
   */
  public SettingServiceRouter(Vertx vertx, OpenAPI3RouterFactory routerFactory) {
    LOGGER.info("init: Creating Router - start");
    this.vertx = vertx;
    this.routerFactory = routerFactory;
    this.settingServiceController = new SettingServiceController(this.vertx);
    LOGGER.info("init: Creating Router - completed");
  }

  public Router getRouter() {
    return this.router;
  }

  /**
   * Create route class with bind handlers.
   *
   * @return - this
   */
  public Router createRouting() {

    LOGGER.info("init: Making Router - start");

    // map methods here
    // this.routerFactory.setBodyHandler(BodyHandler.create());

    // #region Reference handlers

    this.routerFactory.addSecurityHandler(
        "ApiKeyAuth", this.settingServiceController::handleApiKeyAuth);

    // #region SettingsData route

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataAdd", this.settingServiceController::handleSettingsDataAdd);

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataDelete", this.settingServiceController::handleSettingsDataDelete);

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataGet", this.settingServiceController::handleSettingsDataGet);

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataGetConfig", this.settingServiceController::handleSettingsDataGetConfig);

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataGetList", this.settingServiceController::handleSettingsDataGetList);

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataGetSummaryList",
        this.settingServiceController::handleSettingsDataGetSummaryList);

    this.routerFactory.addHandlerByOperationId(
        "settingSettingsDataUpdate", this.settingServiceController::handleSettingsDataUpdate);

    // #endregion

    // base methods
    this.router = this.routerFactory.getRouter();

    final Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("Access-Control-Allow-Method");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("Access-Control-Allow-Credentials");
    allowedHeaders.add("Access-Control-Allow-Headers");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("token");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);

    this.router
        .route()
        .handler(
            CorsHandler.create(".*.")
                .allowCredentials(true)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders));
    this.router.route().handler(StaticHandler.create().setCachingEnabled(false));

    this.router.route().handler(StaticHandler.create().setCachingEnabled(false));
    this.router.route().failureHandler(this.settingServiceController::defaultFailureHandler);
    this.router.route().handler(this.settingServiceController::handlerNotFound);

    LOGGER.info("init: Making Router - completed");

    return this.getRouter();
  }
}
