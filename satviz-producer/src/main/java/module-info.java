module edu.kit.satviz.producer {

  requires java.logging;

  requires edu.kit.satviz.common;
  requires edu.kit.satviz.sat;
  requires edu.kit.satviz.parsers;
  requires edu.kit.satviz.network;
  requires edu.kit.ipasir4j;
  requires net.sourceforge.argparse4j;
  requires org.lz4.java;

  opens edu.kit.satviz.producer.cli to net.sourceforge.argparse4j;
  opens edu.kit.satviz.producer.source to edu.kit.ipasir4j;
}