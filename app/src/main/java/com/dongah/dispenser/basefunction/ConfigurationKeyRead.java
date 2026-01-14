package com.dongah.dispenser.basefunction;

import com.dongah.dispenser.utils.FileManagement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

public class ConfigurationKeyRead {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationKeyRead.class);

    final String fileName = "ConfigurationKey";
    String configurationString;

    public ConfigurationKeyRead() {
    }

    public void onRead() {
        try {
            File file = new File(GlobalVariables.getRootPath() + File.separator + fileName);
            if (file.exists()) {
                FileManagement fileManagement = new FileManagement();
                configurationString = fileManagement.getStringFromFile(GlobalVariables.getRootPath() + File.separator + fileName);
                JSONObject jsonObjectData = new JSONObject(configurationString);
                JSONArray jsonArrayContent = jsonObjectData.getJSONArray("values");
                for (int i = 0; i < jsonArrayContent.length(); i++) {
                    JSONObject contDetail = jsonArrayContent.getJSONObject(i);
                    if (Objects.equals("AllowOfflineTxForUnknownId", contDetail.getString("key"))) {
                        GlobalVariables.setAllowOfflineTxForUnknownId(contDetail.getBoolean("value"));
                    } else if (Objects.equals("AuthorizationCacheEnabled", contDetail.getString("key"))) {
                        GlobalVariables.setAuthorizationCacheEnabled(contDetail.getBoolean("value"));
                    } else if (Objects.equals("AuthorizeRemoteTxRequests", contDetail.getString("key"))) {
                        GlobalVariables.setAuthorizeRemoteTxRequests(Boolean.parseBoolean(contDetail.getString("value")));
                    } else if (Objects.equals("BlinkRepeat", contDetail.getString("key"))) {
                        GlobalVariables.setBlinkRepeat(contDetail.getInt("value"));
                    } else if (Objects.equals("ClockAlignedDataInterval", contDetail.getString("key"))) {
                        GlobalVariables.setClockAlignedDataInterval(contDetail.getInt("value"));
                    } else if (Objects.equals("ConnectionTimeOut", contDetail.getString("key"))) {
                        GlobalVariables.setConnectionTimeOut(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("GetConfigurationMaxKeys", contDetail.getString("key"))) {
                        GlobalVariables.setGetConfigurationMaxKeys(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("HeartbeatInterval", contDetail.getString("key"))) {
                        GlobalVariables.setHeartBeatInterval(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("LightIntensity", contDetail.getString("key"))) {
                        GlobalVariables.setLightIntensity(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("LocalAuthorizeOffline", contDetail.getString("key"))) {
                        GlobalVariables.setLocalAuthorizeOffline(contDetail.getBoolean("value"));
                    } else if (Objects.equals("LocalPreAuthorize", contDetail.getString("key"))) {
                        GlobalVariables.setLocalPreAuthorize(contDetail.getBoolean("value"));
                    } else if (Objects.equals("MaxEnergyOnInvalidId", contDetail.getString("key"))) {
                        GlobalVariables.setMaxEnergyOnInvalidId(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MeterValuesAlignedData", contDetail.getString("key"))) {
                        GlobalVariables.setMeterValuesAlignedData(contDetail.getString("value"));
                    } else if (Objects.equals("MeterValuesAlignedDataMaxLength", contDetail.getString("key"))) {
                        GlobalVariables.setMeterValuesAlignedDataMaxLength(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MeterValuesSampledData", contDetail.getString("key"))) {
                        GlobalVariables.setMeterValuesSampledData(contDetail.getString("value"));
                    } else if (Objects.equals("MeterValuesSampledDataMaxLength", contDetail.getString("key"))) {
                        GlobalVariables.setMeterValuesSampledDataMaxLength(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MeterValueSampleInterval", contDetail.getString("key"))) {
                        GlobalVariables.setMeterValueSampleInterval(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MinimumStatusDuration", contDetail.getString("Key"))) {
                        GlobalVariables.setMinimumStatusDuration(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("NumberOfConnectors", contDetail.getString("Key"))) {
                        GlobalVariables.setNumberOfConnectors(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("ResetRetries", contDetail.getString("key"))) {
                        GlobalVariables.setResetRetries(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("StopTransactionOnEVSideDisconnect", contDetail.getString("key"))) {
                        GlobalVariables.setStopTransactionOnEVSideDisconnect(contDetail.getBoolean("value"));
                    } else if (Objects.equals("StopTransactionOnInvalidId", contDetail.getString("key"))) {
                        GlobalVariables.setStopTransactionOnInvalidId(contDetail.getBoolean("value"));
                    } else if (Objects.equals("StopTxnAlignedData", contDetail.getString("key"))) {
                        GlobalVariables.setStopTxnAlignedData(contDetail.getString("value"));
                    } else if (Objects.equals("StopTxnSampledData", contDetail.getString("key"))) {
                        GlobalVariables.setStopTxnSampledData(contDetail.getString("value"));
                    } else if (Objects.equals("SupportedFeatureProfiles", contDetail.getString("key"))) {
                        GlobalVariables.setSupportedFeatureProfiles(contDetail.getString("value"));
                    } else if (Objects.equals("TransactionMessageAttempts", contDetail.getString("key"))) {
                        GlobalVariables.setTransactionMessageAttempts(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("TransactionMessageRetryInterval", contDetail.getString("key"))) {
                        GlobalVariables.setTransactionMessageRetryInterval(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("UnlockConnectorOnEVSideDisconnect", contDetail.getString("key"))) {
                        GlobalVariables.setUnlockConnectorOnEVSideDisconnect(contDetail.getBoolean("value"));
                    } else if (Objects.equals("LocalAuthListEnabled", contDetail.getString("key"))) {
                        GlobalVariables.setLocalAuthListEnabled(contDetail.getBoolean("value"));
                    } else if (Objects.equals("LocalAuthListMaxLength", contDetail.getString("key"))) {
                        GlobalVariables.setLocalAuthListMaxLength(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("SendLocalListMaxLength", contDetail.getString("key"))) {
                        GlobalVariables.setSendLocalListMaxLength(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("ReserveConnectorZeroSupported", contDetail.getString("key"))) {
                        GlobalVariables.setReserveConnectorZeroSupported(contDetail.getBoolean("value"));
                    } else if (Objects.equals("ChargeProfileMaxStackLevel", contDetail.getString("key"))) {
                        GlobalVariables.setChargeProfileMaxStackLevel(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("ChargingScheduleAllowedChargingRateUnit", contDetail.getString("key"))) {
                        GlobalVariables.setChargingScheduleAllowedChargingRateUnit(contDetail.getString("value"));
                    } else if (Objects.equals("ChargingScheduleMaxPeriods", contDetail.getString("key"))) {
                        GlobalVariables.setChargingScheduleMaxPeriods(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("MaxChargingProfilesInstalled", contDetail.getString("key"))) {
                        GlobalVariables.setMaxChargingProfilesInstalled(Integer.parseInt(contDetail.getString("value")));
                    }

                    else if (Objects.equals("FullRechgAmt", contDetail.getString("key"))) {
                        GlobalVariables.setFullRechgAmt(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("PersonUtztnLmtYn", contDetail.getString("key"))) {
                        GlobalVariables.setPersonUtztnLmtYn(contDetail.getString("value"));
                    } else if (Objects.equals("PersonUtztnLmtHr", contDetail.getString("key"))) {
                        GlobalVariables.setPersonUtztnLmtHr(Integer.parseInt(contDetail.getString("value")));
                    } else if (Objects.equals("webSocketURL", contDetail.getString("key"))) {
                        GlobalVariables.setWebSocketURL(contDetail.getString("value"));
                    }

                    else if (Objects.equals("AuthorizationKey", contDetail.getString("key"))) {
                        GlobalVariables.setAuthorizationKey(contDetail.getString("value"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ConfigurationKeyRead onRead error {}", e.getMessage());
        }
    }
}
