package com.safenetpay.setting.service.db;

import com.safenetpay.common.UserCredentials;
import com.safenetpay.datacontract.SettingsData;
import com.safenetpay.list.PageDataList;
import com.safenetpay.list.SettingsDataList;
import io.vertx.core.Future;

public interface SettingService {

  // #region SettingsData table

  Future<Long> settingsDataAdd(UserCredentials uc, SettingsData settingsData);

  Future<Long> settingsDataDelete(UserCredentials uc, Long settingsDataId);

  Future<SettingsData> settingsDataGet(UserCredentials uc, Long settingsDataId);

  Future<SettingsData> settingsDataGetValue(UserCredentials uc, String settingDataKey);

  Future<SettingsDataList> settingsDataGetList(UserCredentials uc, Long skip, Long pageSize);

  Future<PageDataList<SettingsData>> settingsDataGetSummaryList(
      UserCredentials uc, String sortExpression, String filterCondition, Long skip, Long pageSize);

  Future<Long> settingsDataUpdate(UserCredentials uc, SettingsData settingsData);

  // #endregion
}
