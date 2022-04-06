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
  requires java.desktop;
  requires jdk.incubator.foreign;
  requires edu.kit.satviz.common;
  requires zip4j;

  exports edu.kit.satviz.consumer.gui.config to javafx.graphics;
  exports edu.kit.satviz.consumer.gui.visualization to javafx.graphics;
  exports edu.kit.satviz.consumer.processing to com.fasterxml.jackson.databind;
  exports edu.kit.satviz.consumer.config.routines to com.fasterxml.jackson.databind;

  opens edu.kit.satviz.consumer.config;
  opens edu.kit.satviz.consumer.gui.config to javafx.fxml;
  opens edu.kit.satviz.consumer.gui.visualization to javafx.fxml;

}