module edu.kit.satviz.consumer {

  requires edu.kit.satviz.network;
  requires edu.kit.satviz.parsers;
  requires com.google.gson;
  requires net.sourceforge.argparse4j;
  requires javafx.base;
  requires javafx.fxml;
  requires javafx.controls;

  exports edu.kit.satviz.consumer.gui.config to javafx.graphics;

  opens edu.kit.satviz.consumer.gui.config to javafx.fxml;

}