package com.safenetpay.setting.service.controller;

import com.safenetpay.common.UserCredentials;
import com.safenetpay.common.error.Error;
import com.safenetpay.common.throwable.ApplicationRuntimeException;
import com.safenetpay.datacontract.SettingsData;
import com.safenetpay.list.PageDataList;
import com.safenetpay.list.SettingsDataList;
import com.safenetpay.setting.service.db.SettingService;
import com.safenetpay.setting.service.db.SettingServiceDB;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.log4j.Logger;

/**
 * SettingServiceController. @Author azizxon
 *
 * @since 2021-06-17
 */
public class SettingServiceController extends BaseController {

  static final Logger LOGGER = Logger.getLogger(SettingServiceController.class);
  private Vertx vertx;
  private SettingService settingService;

  static final String HASH_SAULT = "put here any string you need";
  private WebClient authClient;
  private String serviceToken;

  // #region Common handlers
  /**
   * Constructor of settingService.
   *
   * @param vertx - default mounting vertex
   */
  public SettingServiceController(Vertx vertx) {
    super();
    LOGGER.info("init: SettingServiceController - start");
    this.vertx = vertx;

    JsonObject config = Vertx.currentContext().config();
    JsonObject settingServiceConfig = config.getJsonObject("SettingService").getJsonObject("db");
    this.settingService = new SettingServiceDB(this.vertx, settingServiceConfig);

    this.serviceToken = config.getJsonObject("SettingService").getString("token");

    WebClientOptions webClientOptions =
        new WebClientOptions()
            .setDefaultHost(
                config.getJsonObject("AuthService").getJsonObject("http").getString("host"))
            .setDefaultPort(
                config.getJsonObject("AuthService").getJsonObject("http").getInteger("port"));
    this.authClient = WebClient.create(vertx, webClientOptions);

    LOGGER.info("init: SettingServiceController - completed");
  }

  /**
   * Default SettingServiceController handler.
   *
   * @param context - context
   */
  public void defaultHandler(RoutingContext context) {
    LOGGER.info("call: SettingServiceController default Handler");
    context.next();
  }

  /**
   * Default SettingServiceController fail handler.
   *
   * @param context - context
   */
  public void defaultFailureHandler(RoutingContext context) {
    try {
      if (context.failure() instanceof ApplicationRuntimeException) {
        ApplicationRuntimeException ex = (ApplicationRuntimeException) context.failure();
        LOGGER.error("info: SettingServiceController default failure handler");
        this.respondJsonResult(
            context,
            200,
            this.getId(context),
            null,
            new JsonObject().put("code", ex.getError().getCode()).put("message", ex.toString()));
      } else {
        LOGGER.error("info: SettingServiceController default failure handler");
        this.respondJsonResult(
            context,
            200,
            this.getId(context),
            null,
            new JsonObject().put("code", -1).put("message", context.failure().getMessage()));
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      this.respondJsonResult(
          context,
          200,
          this.getId(context),
          null,
          new JsonObject()
              .put("code", -1)
              .put(
                  "message",
                  String.format(
                      "unknown error: Method %s for path %s with message %s",
                      context.request().method(),
                      context.request().absoluteURI(),
                      e.getMessage())));
    }
  }

  /**
   * Default SettingServiceController fail handler.
   *
   * @param context - context
   */
  public void handlerNotFound(RoutingContext context) {
    LOGGER.info("info: SettingServiceController default handler Not Found");
    HttpMethod method = context.request().method();
    this.respondJsonResult(
        context,
        404,
        10L,
        null,
        new JsonObject()
            .put("code", -1)
            .put(
                "message",
                String.format(
                    "unknown error: Method %s for path %s",
                    method, context.request().absoluteURI())));
  }

  /**
   * Security handler.
   *
   * @param context - context
   */
  public void handleApiKeyAuth(RoutingContext context) {
    LOGGER.info("call: SettingServiceController ApiKeyAuth Handler");
    String tokenParam = context.request().headers().get("token");

    JsonObject jsonBody =
        new JsonObject()
            .put("jsonrpc", "2.0")
            .put("id", 1)
            .put("params", new JsonObject().put("user_id", 2).put("token", this.serviceToken));

    this.authClient
        .post("/auth/authenticate")
        .putHeader("token", tokenParam)
        .sendJsonObject(
            jsonBody,
            response -> {
              try {
                if (response.succeeded()) {
                  JsonObject authResult = response.result().bodyAsJsonObject();
                  if (authResult.getJsonObject("result") != null) {
                    context.put("auth", authResult.getJsonObject("result"));
                    context.next();
                  } else {
                    throw new ApplicationRuntimeException(
                        authResult.getJsonObject("error").getString("message"), Error.AUTH);
                  }
                } else {
                  context.fail(response.cause());
                }
              } catch (Exception e) {
                context.fail(e);
              }
            });
  }

  // #endregion
  // #region references handlers

  // #endregion

  // #region SettingsData

  /**
   * SettingsDataAdd handler.
   *
   * @param context - context
   */
  public void handleSettingsDataAdd(RoutingContext context) {

    try {
      LOGGER.info("call: SettingServiceController /setting/settings-data/add Handler");
      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));

      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      if (params.getJsonObject("settings_data") != null) {

        SettingsData settingsData =
            SettingsData.fromJsonObject(
                params.getJsonObject("settings_data").encodePrettily(), SettingsData.class);

        Future<Long> futureSettingsData = settingService.settingsDataAdd(credentials, settingsData);
        futureSettingsData.onComplete(
            res -> {
              try {
                if (res.succeeded()) {
                  if (res.result() > 0) {
                    this.respondJsonResult(
                        context,
                        200,
                        1L,
                        new JsonObject().put("settings_data_id", res.result()),
                        null);
                  } else {
                    throw new ApplicationRuntimeException(
                        "Setting SettingsDataAdd failed.", Error.DATABASE);
                  }
                } else {
                  context.fail(res.cause());
                }
              } catch (Exception e) {
                context.fail(e);
              }
            });
      } else {
        throw new ApplicationRuntimeException("param value error", Error.APPLICATION);
      }
    } catch (Exception e) {
      context.fail(e);
    }
  }

  /**
   * SettingsDataDelete handler.
   *
   * @param context - context
   */
  public void handleSettingsDataDelete(RoutingContext context) {

    try {
      LOGGER.info("call: SettingServiceController /setting/settings-data/delete Handler");
      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));

      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      Long settingsDataId = params.getLong("settings_data_id");

      Future<Long> futureSettingsData =
          settingService.settingsDataDelete(credentials, settingsDataId);
      futureSettingsData.onComplete(
          res -> {
            try {
              if (res.succeeded()) {
                if (res.result() > 0) {
                  this.respondJsonResult(
                      context,
                      200,
                      1L,
                      new JsonObject().put("settings_data_id", res.result()),
                      null);
                } else {
                  throw new ApplicationRuntimeException(
                      "Setting SettingsDataDelete failed.", Error.DATABASE);
                }
              } else {
                context.fail(res.cause());
              }
            } catch (Exception e) {
              context.fail(e);
            }
          });

    } catch (Exception e) {
      context.fail(e);
    }
  }

  /**
   * SettingsDataGet.
   *
   * @param context - routing context
   */
  public void handleSettingsDataGet(RoutingContext context) {

    try {
      LOGGER.info("call: SettingServiceController /setting/settings-data/get Handler");
      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));

      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      Long settingsDataId = params.getLong("settings_data_id");

      Future<SettingsData> futureSettingsData =
          settingService.settingsDataGet(credentials, settingsDataId);
      futureSettingsData.onComplete(
          res -> {
            try {
              if (res.succeeded()) {
                SettingsData settingsData = res.result();
                if (settingsData == null) {
                  throw new ApplicationRuntimeException(
                      "Setting SettingsDataGet failed.", Error.DATABASE);
                }

                this.respondJsonResult(
                    context,
                    200,
                    1L,
                    new JsonObject().put("settings_data", settingsData.toJsonObject()),
                    null);

              } else {
                context.fail(res.cause());
              }
            } catch (Exception e) {
              context.fail(e);
            }
          });

    } catch (Exception e) {
      context.fail(e);
    }
  }

  /**
   * SettingsDataGetConfig.
   *
   * @param context - routing context
   */
  public void handleSettingsDataGetConfig(RoutingContext context) {

    try {
      LOGGER.info("call: SettingServiceController /setting/settings-data/get Handler");
      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));

      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      String settingsDataKey = params.getString("key");

      Future<SettingsData> futureSettingsData =
          settingService.settingsDataGetValue(credentials, settingsDataKey);
      futureSettingsData.onComplete(
          res -> {
            try {
              if (res.succeeded()) {
                SettingsData settingsData = res.result();
                if (settingsData == null) {
                  throw new ApplicationRuntimeException(
                      "Setting SettingsDataGet failed.", Error.DATABASE);
                }

                this.respondJsonResult(
                    context,
                    200,
                    1L,
                    new JsonObject().put("settings_data", settingsData.toJsonObject()),
                    null);

              } else {
                context.fail(res.cause());
              }
            } catch (Exception e) {
              context.fail(e);
            }
          });

    } catch (Exception e) {
      context.fail(e);
    }
  }

  /**
   * Get SettingsData List handler.
   *
   * @param context - routing context
   */
  public void handleSettingsDataGetList(RoutingContext context) {

    try {
      LOGGER.info("call: Setting Service Controller /setting/settings-data/get-list Handler");

      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));

      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      Long pageSize = params.getLong("page_size");
      Long skip = params.getLong("skip_count");

      Future<SettingsDataList> futureSettingsDatas =
          settingService.settingsDataGetList(credentials, skip, pageSize);
      futureSettingsDatas.onComplete(
          res -> {
            try {
              if (res.succeeded()) {
                SettingsDataList settingsDatas = res.result();
                if (settingsDatas == null) {
                  throw new ApplicationRuntimeException(
                      "Setting get SettingsData List failed.", Error.DATABASE);
                }
                this.respondJsonResult(
                    context,
                    200,
                    1L,
                    new JsonObject().put(settingsDatas.getLabel(), settingsDatas.toJsonArray()),
                    null);
              } else {
                context.fail(res.cause());
              }
            } catch (Exception e) {
              context.fail(e);
            }
          });

    } catch (Exception e) {
      context.fail(e);
    }
  }

  /**
   * SettingsDataGetSummaryList handler.
   *
   * @param context - context
   */
  public void handleSettingsDataGetSummaryList(RoutingContext context) {
    try {
      LOGGER.info(
          "call: setting Service Controller " + "/setting/settings-data/get-summary-list Handler");

      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));

      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      String sortExpression = params.getString("sort_expression");
      String filterCondition = params.getString("filter_condition");
      Long pageSize = params.getLong("page_size");
      Long skip = params.getLong("skip_count");

      Future<PageDataList<SettingsData>> futureSettingsDataSummaries =
          settingService.settingsDataGetSummaryList(
              credentials, sortExpression, filterCondition, skip, pageSize);

      futureSettingsDataSummaries.onComplete(
          res -> {
            try {
              if (res.succeeded()) {
                PageDataList<SettingsData> settingsDatas = res.result();
                if (settingsDatas == null) {
                  throw new ApplicationRuntimeException(
                      "settingsDataGetSummaryList failed.", Error.DATABASE);
                }
                this.respondJsonResult(
                    context,
                    200,
                    1L,
                    new JsonObject().put("settings_data_summaries", settingsDatas.toJsonObject()),
                    null);
              } else {
                context.fail(res.cause());
              }
            } catch (Exception e) {
              context.fail(e);
            }
          });

    } catch (Exception e) {
      context.fail(e);
    }
  }

  /**
   * SettingsDataUpdate handler.
   *
   * @param context - context
   */
  public void handleSettingsDataUpdate(RoutingContext context) {
    try {
      LOGGER.info("call: SettingServiceController /setting/settings-data/update Handler");
      JsonObject auth = context.get("auth");
      UserCredentials credentials = new UserCredentials().setLoginId(auth.getLong("user_id"));
      JsonObject params = this.getBodyJsonObjectParam(context, "params");
      if (params.getJsonObject("settings_data") != null) {

        SettingsData settingsData =
            SettingsData.fromJsonObject(
                params.getJsonObject("settings_data").encodePrettily(), SettingsData.class);

        Future<Long> futureSettingsData =
            settingService.settingsDataUpdate(credentials, settingsData);
        futureSettingsData.onComplete(
            res -> {
              try {
                if (res.succeeded()) {
                  if (res.result() > 0) {
                    this.respondJsonResult(
                        context,
                        200,
                        1L,
                        new JsonObject().put("settings_data_id", res.result()),
                        null);
                  } else {
                    throw new ApplicationRuntimeException(
                        "Setting SettingsDataUpdate failed.", Error.DATABASE);
                  }
                } else {
                  context.fail(res.cause());
                }
              } catch (Exception e) {
                context.fail(e);
              }
            });
      } else {
        throw new ApplicationRuntimeException("param value error", Error.APPLICATION);
      }
    } catch (Exception e) {
      context.fail(e);
    }
  }

  // #endregion

}
