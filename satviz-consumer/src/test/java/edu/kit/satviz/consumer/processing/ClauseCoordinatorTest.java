package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.ClauseUpdate;
import edu.kit.satviz.serial.SerializationException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ClauseCoordinatorTest {

  private static final String TEMP_DIR = "src/test/temp";

  private int clausesAdded;
  private int currentlyAdvanced;
  private ClauseCoordinator coordinator;
  private Graph graph;
  private ClauseUpdateProcessor processor1;
  private ClauseUpdateProcessor processor2;
  private AtomicInteger changeListenerCallAmount;

  private final ClauseUpdate[] clauseUpdates = new ClauseUpdate[]{
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 6, 1),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 6, 2),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 6, 3),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, -6, 4),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, -6, 5),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 1, 4),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 2, 4),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 3, 4),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 1, 5),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 2, 5),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 3, 5),
          ClauseUpdate.of(ClauseUpdate.Type.ADD, 6),
          ClauseUpdate.of(ClauseUpdate.Type.ADD)
  };

  @BeforeEach
  void setUp() throws IOException {
    clausesAdded = 0;
    currentlyAdvanced = 0;
    graph = mock(Graph.class);
    processor1 = mock(ClauseUpdateProcessor.class);
    processor2 = mock(ClauseUpdateProcessor.class);
    changeListenerCallAmount = new AtomicInteger();

    Path tempDir = Files.createDirectory(Paths.get(TEMP_DIR));
    coordinator = new ClauseCoordinator(graph, tempDir);
    coordinator.addProcessor(processor1);

    coordinator.registerChangeListener(() -> changeListenerCallAmount.getAndIncrement());
  }

  @Test
  void test_advanceVisualization() throws IOException, SerializationException {
    int processor1AdvanceCalls = 0;
    int processor2AdvanceCalls = 0;
    for (int i = 4; i < clauseUpdates.length; i += 4) {
      addUpdatesAndAdvance(Arrays.copyOfRange(clauseUpdates, i - 4, i));
      processor1AdvanceCalls++;
    }
    verify(processor1, times(processor1AdvanceCalls)).process(notNull(), eq(graph));
    coordinator.addProcessor(processor2);
    while (clausesAdded < clauseUpdates.length) {
      addUpdatesAndAdvance(Arrays.copyOfRange(clauseUpdates, clausesAdded, clausesAdded + 1));
      processor1AdvanceCalls++;
      processor2AdvanceCalls++;
    }
    verify(processor1, times(processor1AdvanceCalls)).process(notNull(), eq(graph));
    verify(processor2, times(processor2AdvanceCalls)).process(notNull(), eq(graph));
  }

  private void addUpdatesAndAdvance(ClauseUpdate[] clauseUpdatesToAdd)
          throws IOException, SerializationException {
    for (ClauseUpdate update : clauseUpdatesToAdd) {
      coordinator.addClauseUpdate(update);
      clausesAdded++;
    }
    assertEquals(clausesAdded, coordinator.totalUpdateCount());
    assertEquals(currentlyAdvanced, coordinator.currentUpdate());
    coordinator.advanceVisualization(clauseUpdatesToAdd.length);
    currentlyAdvanced += clauseUpdatesToAdd.length;
    assertEquals(currentlyAdvanced, coordinator.currentUpdate());
  }

  @Test
  void test_advanceVisualization_once() throws IOException, SerializationException {
    ClauseUpdate[] array = Arrays.copyOfRange(clauseUpdates, 0, 4);
    for (ClauseUpdate update : array) {
      coordinator.addClauseUpdate(update);
    }
    assertEquals(4, coordinator.totalUpdateCount());
    verify(processor1, never()).process(notNull(), eq(graph));
    coordinator.advanceVisualization(1);
    assertEquals(1, changeListenerCallAmount.get());
  }

  // registerChangeListener

  @Test
  void test_registerChangeListener_advanceOnce() throws IOException, SerializationException {
    coordinator.addClauseUpdate(clauseUpdates[0]);
    assertEquals(0, changeListenerCallAmount.get());
    coordinator.advanceVisualization(1);
    assertEquals(1, changeListenerCallAmount.get());
  }

  @Test
  void test_registerChangeListener_advanceMultipleTimes()
          throws SerializationException, IOException {
    int expectedCallAmount = 0;
    for (ClauseUpdate update : clauseUpdates) {
      coordinator.addClauseUpdate(update);
    }
    while (coordinator.currentUpdate() < clauseUpdates.length) {
      coordinator.advanceVisualization(1);
      expectedCallAmount++;
    }
    assertEquals(expectedCallAmount, changeListenerCallAmount.get());
  }

  @Test
  void test_registerChangeListener_withoutAdvance() throws IOException {
    for (ClauseUpdate update : clauseUpdates) {
      coordinator.addClauseUpdate(update);
    }
    assertEquals(0, changeListenerCallAmount.get());
  }

  @Test
  void test_registerChangeListener_advanceZero() throws SerializationException, IOException {
    coordinator.advanceVisualization(0);
    assertEquals(0, changeListenerCallAmount.get());
  }

  // seekToUpdate

  @Test
  void test_seekToUpdate_goForward() throws SerializationException, IOException {
    ClauseUpdate[] someUpdates = Arrays.copyOfRange(clauseUpdates, 0, 4);
    for (ClauseUpdate update : someUpdates) {
      coordinator.addClauseUpdate(update);
    }
    assertEquals(0, coordinator.currentUpdate());
    coordinator.seekToUpdate(4);
    verify(processor1).process(eq(someUpdates), eq(graph));
    assertEquals(4, coordinator.currentUpdate());
  }

  @Test
  void test_seekToUpdate_goToCurrentSnapshot() throws SerializationException, IOException {
    for (ClauseUpdate update : Arrays.copyOfRange(clauseUpdates, 0, 4)) {
      coordinator.addClauseUpdate(update);
    }
    coordinator.advanceVisualization(3);
    for (ClauseUpdate update : Arrays.copyOfRange(clauseUpdates, 4, 7)) {
      coordinator.addClauseUpdate(update);
    }
    assertEquals(3, coordinator.currentUpdate());
    coordinator.seekToUpdate(3);
    assertEquals(3, coordinator.currentUpdate());
  }

  @Test
  void test_seekToUpdate_goToPast() throws SerializationException, IOException {
    for (ClauseUpdate update : Arrays.copyOfRange(clauseUpdates, 0, 5)) {
      coordinator.addClauseUpdate(update);
    }
    coordinator.advanceVisualization(4);
    for (ClauseUpdate update : Arrays.copyOfRange(clauseUpdates, 5, 11)) {
      coordinator.addClauseUpdate(update);
    }
    coordinator.advanceVisualization(5);
    coordinator.advanceVisualization(1);
    assertEquals(10, coordinator.currentUpdate());
    coordinator.seekToUpdate(6);
    assertEquals(6, coordinator.currentUpdate());
  }

  @AfterEach
  void clean() throws IOException {
    coordinator.close();
  }

}