package com.linkedin.datahub.upgrade.nocodecleanup;

import com.linkedin.datahub.upgrade.UpgradeContext;
import com.linkedin.datahub.upgrade.UpgradeStep;
import com.linkedin.datahub.upgrade.UpgradeStepResult;
import com.linkedin.datahub.upgrade.impl.DefaultUpgradeStepResult;
import com.linkedin.metadata.entity.ebean.AspectStorageValidationUtil;
import com.linkedin.upgrade.DataHubUpgradeState;
import io.ebean.Database;
import java.util.function.Function;

public class NoCodeUpgradeQualificationStep implements UpgradeStep {

  private final Database _server;

  NoCodeUpgradeQualificationStep(Database server) {
    _server = server;
  }

  @Override
  public String id() {
    return "UpgradeQualificationStep";
  }

  @Override
  public int retryCount() {
    return 2;
  }

  @Override
  public Function<UpgradeContext, UpgradeStepResult> executable() {
    return (context) -> {
      try {
        if (!AspectStorageValidationUtil.checkV2TableExists(_server)) {
          // Unqualified (V2 Table does not exist)
          context
              .report()
              .addLine("You have not successfully migrated yet. Aborting the cleanup...");
          return new DefaultUpgradeStepResult(
              id(), DataHubUpgradeState.SUCCEEDED, UpgradeStepResult.Action.ABORT);
        } else {
          // Qualified.
          context.report().addLine("Found qualified upgrade candidate. Proceeding with upgrade...");
          return new DefaultUpgradeStepResult(id(), DataHubUpgradeState.SUCCEEDED);
        }
      } catch (Exception e) {
        context.report().addLine("Failed to check if metadata_aspect_v2 table exists: %s", e);
        return new DefaultUpgradeStepResult(id(), DataHubUpgradeState.FAILED);
      }
    };
  }
}
