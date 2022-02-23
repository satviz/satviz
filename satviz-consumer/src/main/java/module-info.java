module edu.kit.satviz.consumer {

  requires com.fasterxml.jackson.databind;
  requires edu.kit.satviz.network;
  requires edu.kit.satviz.serial;
  requires edu.kit.satviz.parsers;
  requires net.sourceforge.argparse4j;
  requires java.logging;
  requires javafx.base;
  requires javafx.fxml;
  requires javafx.controls;
  requires jdk.incubator.foreign;
  requires edu.kit.satviz.common;
  requires zip4j;

  exports edu.kit.satviz.consumer.gui.config to javafx.graphics;

  opens edu.kit.satviz.consumer.config;
  opens edu.kit.satviz.consumer.gui.config to javafx.fxml;

}