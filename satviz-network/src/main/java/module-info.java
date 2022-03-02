module edu.kit.satviz.network {

  requires transitive edu.kit.satviz.sat;
  requires edu.kit.satviz.serial;

  exports edu.kit.satviz.network;
    exports edu.kit.satviz.network.general;
    exports edu.kit.satviz.network.pub;
}