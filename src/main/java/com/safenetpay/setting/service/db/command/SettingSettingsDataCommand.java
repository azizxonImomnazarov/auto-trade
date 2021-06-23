package com.safenetpay.setting.service.db.command;

import com.safenetpay.datacontract.SettingsData;
import com.safenetpay.list.PageDataList;
import com.safenetpay.list.PageDataListImpl;
import com.safenetpay.list.SettingsDataList;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.apache.log4j.Logger;

/**
 * The type SettingSettingsDataCommand. @Author azizxon
 *
 * @since 2021-06-17
 */
public class SettingSettingsDataCommand extends BaseCommand {
  private static final Logger LOGGER = Logger.getLogger(SettingSettingsDataCommand.class);
  private PgPool client;
  /**
   * Instantiates a new SettingSettingsDataCommand.
   *
   * @param client the client
   */
  public SettingSettingsDataCommand(PgPool client) {
    super();
    LOGGER.info("init: Creating SettingSettingsDataCommand - start");
    this.client = client;
    LOGGER.info("init: Creating SettingSettingsDataCommand - completed");
  }

  /**
   * Add settingsData command future.
   *
   * @param loginId the login id
   * @param settingsData the settingsData
   * @return the future
   */
  public Future<Long> settingsDataAddCommand(Long loginId, SettingsData settingsData) {
    LOGGER.info("info: settingsDataAddCommand - start");
    Promise<Long> promise = Promise.promise();
    client
        .preparedQuery(
            "SELECT settings_data_add AS settings_data_id"
                + " FROM setting.settings_data_add($1, $2, $3, $4, $5 );")
        .execute(
            Tuple.of(
                loginId,
                settingsData.getKey(),
                settingsData.getValue(),
                this.timeStampToOffsetDateTime(settingsData.getValidFrom()),
                this.timeStampToOffsetDateTime(settingsData.getValidTo())),
            ar -> {
              LOGGER.info("info: handle settingsDataAddCommand query result");
              if (ar.succeeded()) {
                System.out.println("Got " + ar.result().size() + " rows ");
                for (Row row : ar.result()) {
                  LOGGER.info("info: handle query settingsDataAddCommand result - ok");
                  promise.complete(row.getLong("settings_data_id"));
                }

                if (promise.tryComplete()) {
                  LOGGER.info("info: handle query settingsDataAddCommand result - no result");
                }
              } else {
                LOGGER.error("info: handle query settingsDataAddCommand result - failed");
                promise.fail(ar.cause());
              }
            });
    return promise.future();
  }

  /**
   * Delete settingsData command future.
   *
   * @param loginId the login id
   * @param settingsDataId the settingsData id
   * @return the future
   */
  public Future<Long> settingsDataDeleteCommand(Long loginId, Long settingsDataId) {
    LOGGER.info("info: settingsDataDeleteCommand - start");
    Promise<Long> promise = Promise.promise();
    client
        .preparedQuery(
            "SELECT settings_data_delete AS settings_data_id "
                + " FROM setting.settings_data_delete($1, $2);")
        .execute(
            Tuple.of(loginId, settingsDataId),
            ar -> {
              LOGGER.info("info: handle settingsDataDeleteCommand query result");
              if (ar.succeeded()) {
                System.out.println("Got " + ar.result().size() + " rows ");
                for (Row row : ar.result()) {
                  LOGGER.info("info: handle query settingsDataDeleteCommand result - ok");
                  promise.complete(row.getLong("settings_data_id"));
                }

                if (promise.tryComplete()) {
                  LOGGER.info("info: handle query settingsDataDeleteCommand result - no result");
                }

              } else {
                LOGGER.error("info: handle query settingsDataDeleteCommand result - failed");
                promise.fail(ar.cause());
              }
            });
    return promise.future();
  }

  /**
   * Gets settingsData command.
   *
   * @param loginId the login id
   * @param settingsDataId the settingsData id
   * @return the settingsData command
   */
  public Future<SettingsData> settingsDataGetCommand(Long loginId, Long settingsDataId) {
    LOGGER.info("info: settingsDataGetCommand - start");
    Promise<SettingsData> promise = Promise.promise();
    client
        .preparedQuery("SELECT * FROM setting.settings_data_get($1, $2);")
        .execute(
            Tuple.of(loginId, settingsDataId),
            ar -> {
              try {
                LOGGER.info("info: handle settingsDataGetCommand query result");
                if (ar.succeeded()) {
                  for (Row row : ar.result()) {
                    SettingsData settingsData = createSettingsData(row);
                    LOGGER.info("info: handle query settingsDataGetCommand result - ok");
                    promise.complete(settingsData);
                  }

                  if (promise.tryComplete()) {
                    LOGGER.info("info: handle query settingsDataGetCommand result - no result");
                  }
                } else {
                  LOGGER.info("info: handle query settingsDataGetCommand result - failed");
                  promise.fail(ar.cause());
                }
              } catch (Exception e) {
                LOGGER.info("info: handle query settingsDataGetCommand result - failed");
                promise.fail(e);
              }
            });
    LOGGER.info("info: settingsDataGetCommand - end");
    return promise.future();
  }

  /**
   * Gets settingsData command.
   *
   * @param loginId the login id
   * @param settingsDataId the settingsData id
   * @return the settingsData command
   */
  public Future<SettingsData> settingsDataGetValueCommand(Long loginId, String settingDataKey) {
    LOGGER.info("info: settingsDataGetCommand - start");
    Promise<SettingsData> promise = Promise.promise();
    client
        .preparedQuery("SELECT * FROM setting.settings_data_get_value($1, $2);")
        .execute(
            Tuple.of(loginId, settingDataKey),
            ar -> {
              try {
                LOGGER.info("info: handle settingsDataGetCommand query result");
                if (ar.succeeded()) {
                  if (ar.result().size() == 0) {
                    promise.complete(new SettingsData());
                  } else {
                    for (Row row : ar.result()) {
                      SettingsData settingsData = createSettingsData(row);
                      LOGGER.info("info: handle query settingsDataGetCommand result - ok");
                      promise.complete(settingsData);
                    }

                    if (promise.tryComplete()) {
                      LOGGER.info("info: handle query settingsDataGetCommand result - no result");
                    }
                  }
                } else {
                  LOGGER.info("info: handle query settingsDataGetCommand result - failed");
                  promise.fail(ar.cause());
                }
              } catch (Exception e) {
                LOGGER.info("info: handle query settingsDataGetCommand result - failed");
                promise.fail(e);
              }
            });
    LOGGER.info("info: settingsDataGetCommand - end");
    return promise.future();
  }

  /**
   * SettingsDataGetList command.
   *
   * @param loginId the login id
   * @param skip the skip
   * @param pageSize the page size
   * @return the settingsDataGetListCommand
   */
  public Future<SettingsDataList> settingsDataGetListCommand(
      Long loginId, Long skip, Long pageSize) {
    LOGGER.info("info: settingsDataGetListCommand - start");
    Promise<SettingsDataList> promise = Promise.promise();
    SettingsDataList settingsDatas = new SettingsDataList();

    client
        .preparedQuery("SELECT * FROM setting.settings_data_get_list($1, $2, $3);")
        .execute(
            Tuple.of(loginId, skip, pageSize),
            ar -> {
              try {
                LOGGER.info("info: handle settingsDataGetListCommand query result");
                if (ar.succeeded()) {
                  for (Row row : ar.result()) {
                    SettingsData settingsData = createSettingsData(row);
                    LOGGER.info("info: handle query settingsDataGetListCommand result - ok");
                    settingsDatas.add(settingsData);
                  }

                  promise.complete(settingsDatas);

                  if (promise.tryComplete()) {
                    LOGGER.info("info: handle query settingsDataGetListCommand result - no result");
                  }
                } else {
                  LOGGER.info("info: handle query settingsDataGetListCommand result - failed");
                  promise.fail(ar.cause());
                }
              } catch (Exception e) {
                LOGGER.info("info: handle query settingsDataGetListCommand result - failed");
                promise.fail(e);
              }
            });
    LOGGER.info("info: settingsDataGetListCommand - end");
    return promise.future();
  }

  /**
   * Gets SettingsDataGetSummaryList command.
   *
   * @param loginId the login id
   * @param sortExpression - sort expression
   * @param filterCondition - filter condition
   * @param skip the skip
   * @param pageSize the page size
   * @return the SettingsData List
   */
  public Future<PageDataList<SettingsData>> settingsDataGetSummaryListCommand(
      Long loginId, String sortExpression, String filterCondition, Long skip, Long pageSize) {
    LOGGER.info("info: settingsDataGetSummaryListCommand - start");
    Promise<PageDataList<SettingsData>> promise = Promise.promise();
    PageDataList<SettingsData> result = new PageDataListImpl<SettingsData>();
    SettingsDataList settingsDataSummaries = new SettingsDataList();
    client
        .preparedQuery("SELECT * FROM setting.settings_data_get_summary_list($1, $2, $3, $4, $5);")
        .execute(
            Tuple.of(loginId, sortExpression, filterCondition, skip, pageSize),
            ar -> {
              try {
                LOGGER.info("info: handle settingsDataGetSummaryListCommand query result");
                if (ar.succeeded()) {
                  for (Row row : ar.result()) {
                    if (row.getBoolean("hidden_is_empty") != null
                        && !row.getBoolean("hidden_is_empty")) {
                      SettingsData settingsDataSummary = createSettingsData(row);
                      settingsDataSummaries.add(settingsDataSummary);
                      LOGGER.info(
                          "info: handle query settingsDataGetSummaryListCommand result - ok");
                    }

                    if (row.getLong("hidden_row_count") != null) {
                      result.setTotalRowCount(row.getLong("hidden_row_count"));
                    } else {
                      throw new Exception("Incorrect summary get list output format");
                    }
                  }
                  result.setData(settingsDataSummaries);
                  promise.complete(result);
                } else {
                  LOGGER.info(
                      "info: handle query settingsDataGetSummaryListCommand result - failed");
                  promise.fail(ar.cause());
                }
              } catch (Exception e) {
                LOGGER.info("info: handle query settingsDataGetSummaryListCommand result - failed");
                promise.fail(e);
              }
            });
    LOGGER.info("info: settingsDataGetSummaryListCommand - end");
    return promise.future();
  }

  private SettingsData createSettingsData(Row row) throws Exception {
    return new SettingsData()
        .setSettingsDataId(row.getLong("settings_data_id"))
        .setKey(row.getString("key"))
        .setValue(row.getString("value"))
        .setValidFrom(this.offsetDateTimeToTimestamp(row, "valid_from"))
        .setValidTo(this.offsetDateTimeToTimestamp(row, "valid_to"));
  }

  /**
   * Update settingsData command future.
   *
   * @param loginId the login id
   * @param settingsData the settingsData
   * @return the future
   */
  public Future<Long> settingsDataUpdateCommand(Long loginId, SettingsData settingsData) {
    LOGGER.info("info: settingsDataUpdateCommand - start");
    Promise<Long> promise = Promise.promise();
    client
        .preparedQuery(
            "SELECT settings_data_update AS settings_data_id"
                + " FROM setting.settings_data_update($1,  $2, $3, $4, $5, $6 );")
        .execute(
            Tuple.of(
                loginId,
                settingsData.getSettingsDataId(),
                settingsData.getKey(),
                settingsData.getValue(),
                this.timeStampToOffsetDateTime(settingsData.getValidFrom()),
                this.timeStampToOffsetDateTime(settingsData.getValidTo())),
            ar -> {
              LOGGER.info("info: handle settingsDataUpdateCommand query result");
              if (ar.succeeded()) {
                System.out.println("Got " + ar.result().size() + " rows ");
                for (Row row : ar.result()) {
                  LOGGER.info("info: handle query settingsDataUpdateCommand result - ok");
                  promise.complete(row.getLong("settings_data_id"));
                }

                if (promise.tryComplete()) {
                  LOGGER.info("info: handle query settingsDataUpdateCommand result - no result");
                }
              } else {
                LOGGER.error("info: handle query settingsDataUpdateCommand result - failed");
                promise.fail(ar.cause());
              }
            });
    return promise.future();
  }
}
