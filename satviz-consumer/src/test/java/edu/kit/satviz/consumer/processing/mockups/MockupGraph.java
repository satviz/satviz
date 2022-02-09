package edu.kit.satviz.consumer.processing.mockups;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.consumer.graph.GraphUpdate;
import edu.kit.satviz.serial.IntSerializer;
import edu.kit.satviz.serial.SerializationException;
import edu.kit.satviz.serial.StringSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MockupGraph extends Graph {

  private Map<String, Integer> updateCounter;
  private StringSerializer stringSerializer;

  protected MockupGraph() {
    super(null);
    updateCounter = new HashMap<>();
    stringSerializer = new StringSerializer();
  }

  public static MockupGraph create(long nodes) {
    return new MockupGraph();
  }

  @Override
  public void submitUpdate(GraphUpdate update) {
    String updateName = update.getClass().getName();
    Integer prev;
    if (updateCounter.containsKey(updateName)) {
      prev = updateCounter.get(updateName);
      updateCounter.put(updateName, prev + 1);
    } else {
      updateCounter.put(updateName, 1);
    }
  }

  @Override
  public void serialize(OutputStream stream) {
    try {
      for (Map.Entry entry : updateCounter.entrySet()) {
        for (int i = 0; i < (int) entry.getValue(); i++) {
          stringSerializer.serialize((String) entry.getKey(), stream);
          stream.write(' ');
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SerializationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(InputStream stream) {
    updateCounter.clear();
    Scanner scanner = new Scanner(stream);
    try {
      while (scanner.hasNext()) {
        submitUpdate(
                (GraphUpdate) Class.forName(
                        stringSerializer.deserialize(
                                new ByteArrayInputStream(
                                        scanner.next().getBytes(StandardCharsets.UTF_8)
                                )
                        )
                ).getConstructor().newInstance()
        );
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SerializationException e) {
    } catch (ClassNotFoundException e) {
    } catch (InvocationTargetException e) {
    } catch (InstantiationException e) {
    } catch (IllegalAccessException e) {
    } catch (NoSuchMethodException e) {
    }
  }

  public Map<String, Integer> getUpdateCounter() {
    return updateCounter;
  }

}
