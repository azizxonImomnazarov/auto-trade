package com.safenetpay.setting.service.db;

import com.safenetpay.common.UserCredentials;
import com.safenetpay.datacontract.SettingsData;
import com.safenetpay.list.PageDataList;
import com.safenetpay.list.SettingsDataList;
import com.safenetpay.setting.service.db.command.SettingSettingsDataCommand;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.apache.log4j.Logger;

public class SettingServiceDB implements SettingService {

  private static final Logger LOGGER = Logger.getLogger(SettingServiceDB.class);

  private Vertx vertx;

  private SettingSettingsDataCommand settingSettingsDataCommand;

  private JsonObject config;

  /**
   * Constructor.
   *
   * @param vertx - main vertex
   * @param config - json configuration
   */
  public SettingServiceDB(Vertx vertx, JsonObject config) {
    LOGGER.info("init: Creating SettingServiceDB - start");
    this.vertx = vertx;
    this.config = config;
    LOGGER.info("init: connection - start");
    PgConnectOptions connectionOptions =
        new PgConnectOptions()
            .setPort(this.config.getInteger("port"))
            .setHost(this.config.getString("host"))
            .setDatabase(this.config.getString("db_name"))
            .setUser(this.config.getString("user"))
            .setPassword(this.config.getString("password"));

    PoolOptions poolOptions = new PoolOptions().setMaxSize(this.config.getInteger("max_pool_size"));
    LOGGER.info("init: connection - completed");

    PgPool client = PgPool.pool(this.vertx, connectionOptions, poolOptions);
    this.settingSettingsDataCommand = new SettingSettingsDataCommand(client);

    LOGGER.info("init: Creating SettingServiceDB - completed");
  }

  // #endregion

  // #region settingsData

  public Future<Long> settingsDataAdd(UserCredentials uc, SettingsData settingsData) {
    LOGGER.info("call: settingsDataAdd method");
    return this.settingSettingsDataCommand.settingsDataAddCommand(uc.getLoginId(), settingsData);
  }

  public Future<Long> settingsDataDelete(UserCredentials uc, Long settingsDataId) {
    LOGGER.info("call: settingsDataDelete method");
    return this.settingSettingsDataCommand.settingsDataDeleteCommand(
        uc.getLoginId(), settingsDataId);
  }

  public Future<SettingsData> settingsDataGet(UserCredentials uc, Long settingsDataId) {
    LOGGER.info("call: settingsDataGet method");
    return this.settingSettingsDataCommand.settingsDataGetCommand(uc.getLoginId(), settingsDataId);
  }

  @Override
  public Future<SettingsData> settingsDataGetValue(UserCredentials uc, String settingDataKey) {
    LOGGER.info("call: settingsDataGetValue method");
    return this.settingSettingsDataCommand.settingsDataGetValueCommand(
        uc.getLoginId(), settingDataKey);
  }

  public Future<SettingsDataList> settingsDataGetList(
      UserCredentials uc, Long skip, Long pageSize) {
    LOGGER.info("call: settingsDataGetList method");
    return this.settingSettingsDataCommand.settingsDataGetListCommand(
        uc.getLoginId(), skip, pageSize);
  }

  public Future<PageDataList<SettingsData>> settingsDataGetSummaryList(
      UserCredentials uc, String sortExpression, String filterCondition, Long skip, Long pageSize) {
    LOGGER.info("call: settingsDataGetSummaryListCommand method");
    return this.settingSettingsDataCommand.settingsDataGetSummaryListCommand(
        uc.getLoginId(), sortExpression, filterCondition, skip, pageSize);
  }

  public Future<Long> settingsDataUpdate(UserCredentials uc, SettingsData settingsData) {
    LOGGER.info("call: settingsDataUpdate method");
    return this.settingSettingsDataCommand.settingsDataUpdateCommand(uc.getLoginId(), settingsData);
  }

  // #endregion
}
