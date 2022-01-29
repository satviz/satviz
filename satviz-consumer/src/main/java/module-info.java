module edu.kit.satviz.consumer {

  requires edu.kit.satviz.network;
  requires edu.kit.satviz.parsers;
  requires com.google.gson;
  requires net.sourceforge.argparse4j;
  requires javafx.base;
  requires javafx.fxml;
  requires javafx.controls;

  opens edu.kit.satviz.consumer.config to com.google.gson;

}