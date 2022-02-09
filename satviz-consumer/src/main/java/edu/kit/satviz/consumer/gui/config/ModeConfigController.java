package edu.kit.satviz.consumer.gui.config;

import edu.kit.satviz.consumer.config.ConsumerModeConfig;

public abstract class ModeConfigController extends ConfigController {

  protected abstract void loadSettings(ConsumerModeConfig config);

}
