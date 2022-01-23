module edu.kit.satviz.producer {

  requires edu.kit.satviz.common;
  requires edu.kit.ipasir4j;
  requires net.sourceforge.argparse4j;

  opens edu.kit.satviz.producer.cli to net.sourceforge.argparse4j;
}