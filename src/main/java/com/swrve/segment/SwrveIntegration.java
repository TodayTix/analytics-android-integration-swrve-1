package com.swrve.segment;

import android.app.Activity;
import android.app.Application;
import com.swrve.sdk.config.SwrveConfig;
import com.swrve.sdk.SwrveHelper;
import com.swrve.sdk.SwrveSDK;
import com.segment.analytics.Analytics;
import com.segment.analytics.Traits;
import com.segment.analytics.ValueMap;
import com.segment.analytics.integrations.IdentifyPayload;
import com.segment.analytics.integrations.Integration;
import com.segment.analytics.integrations.Logger;
import com.segment.analytics.integrations.ScreenPayload;
import com.segment.analytics.integrations.TrackPayload;
import java.util.HashMap;
import java.util.Map;

public class SwrveIntegration extends Integration<Void> {
  private static final String SWRVE_KEY = "Swrve";

  // Swrve needs to be initialized early in order to work correctly.
  public static Factory createFactory(
      final Application application, int appId, String apiKey, SwrveConfig swrveConfig) {
    SwrveSDK.createInstance(application, appId, apiKey, swrveConfig);

    return new Factory() {
      @Override
      public Integration<?> create(ValueMap settings, Analytics analytics) {
        Logger logger = analytics.logger(SWRVE_KEY);
        return new SwrveIntegration(settings, logger);
      }

      @Override
      public String key() {
        return SWRVE_KEY;
      }
    };
  }

  private final Logger logger;

  SwrveIntegration(ValueMap settings, Logger logger) {
    this.logger = logger;
  }

  @Override
  public void identify(IdentifyPayload identify) {
    super.identify(identify);

    String userId = identify.userId();
    if (SwrveHelper.isNotNullOrEmpty(userId)) {
      Map<String, String> attributes = new HashMap<>();

      attributes.put("customer.id", userId);
      SwrveSDK.userUpdate(attributes);
      logger.verbose("SwrveSDK.userUpdate(%s)", attributes);
    }

    SwrveSDK.userUpdate(identify.traits().toStringMap());
    logger.verbose("SwrveSDK.userUpdate(%s);", identify.traits().toStringMap());
  }

  @Override
  public void track(TrackPayload track) {
    super.track(track);
    SwrveSDK.event(track.event(), track.properties().toStringMap());
    logger.verbose("SwrveSDK.event(%s, %s)", track.event(), track.properties().toStringMap());
  }

  @Override
  public void screen(ScreenPayload screen) {
    String eventName = String.format("screen.%s", screen.event());
    SwrveSDK.event(eventName, screen.properties().toStringMap());
    logger.verbose("SwrveSDK.event(%s, %s)", eventName, screen.properties().toStringMap());
  }

  @Override
  public void onActivityResumed(Activity activity) {
    super.onActivityResumed(activity);
    SwrveSDK.onResume(activity);
    logger.verbose("SwrveSDK.onResume(%s)", activity);
  }

  @Override
  public void onActivityPaused(Activity activity) {
    super.onActivityPaused(activity);
    SwrveSDK.onPause();
    logger.verbose("SwrveSDK.onPause();");
  }

  @Override
  public void flush() {
    super.flush();
    SwrveSDK.sendQueuedEvents();
    logger.verbose("SwrveSDK.sendQueuedEvents();");
  }
}
