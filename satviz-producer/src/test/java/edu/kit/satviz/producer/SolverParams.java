package edu.kit.satviz.producer;

import static edu.kit.satviz.producer.ResourceHelper.extractResource;

import edu.kit.satviz.producer.cli.ProducerParameters;
import java.io.IOException;

public class SolverParams {

  public static ProducerParameters solverParams(String solverLib, String instanceFile) throws IOException {
    var solver = extractResource(solverLib);
    var instance = extractResource(instanceFile);
    var params = new ProducerParameters();
    params.setHost("example.com");
    params.setSolverFile(solver);
    params.setInstanceFile(instance);
    return params;
  }

}
