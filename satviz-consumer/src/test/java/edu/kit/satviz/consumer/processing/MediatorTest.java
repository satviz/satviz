package edu.kit.satviz.consumer.processing;

import edu.kit.satviz.consumer.config.ConsumerConfig;
import edu.kit.satviz.consumer.display.VideoController;
import edu.kit.satviz.consumer.graph.Graph;
import edu.kit.satviz.sat.Clause;
import edu.kit.satviz.sat.ClauseUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MediatorTest {
  ScheduledExecutorService glScheduler;
  Graph graph;
  VideoController controller;
  ClauseCoordinator coordinator;
  Heatmap heatmap;
  VariableInteractionGraph vig;
  ConsumerConfig config;
  Mediator med;

  boolean closed;
  boolean framed;

  @BeforeEach
  void beforeEach() {
    glScheduler = Executors.newSingleThreadScheduledExecutor();
    graph = mock(Graph.class);
    controller = mock(VideoController.class);
    coordinator = mock(ClauseCoordinator.class);
    heatmap = mock(Heatmap.class);
    vig = mock(VariableInteractionGraph.class);
    config = mock(ConsumerConfig.class);
    med = new Mediator.MediatorBuilder()
        .setGlScheduler(glScheduler)
        .setGraph(graph)
        .setController(controller)
        .setCoordinator(coordinator)
        .setHeatmap(heatmap)
        .setVig(vig)
        .setConfig(config)
        .createMediator();
  }

  @Test
  void testCloseAction() {
    // test if close action is executed and all parts of the mediator are closed
    closed = false;
    med.registerCloseAction(() -> closed = true);
    try {
      med.close();
    } catch (Exception e) {
      fail(e);
    }
    assertTrue(closed);
    verify(graph).close();
    verify(controller).close();
    try {
      verify(coordinator).close();
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void testRender() {
    // test if interactions work (updateWindowSize, seekToUpdate), frame actions are executed
    // while rendering
    framed = false;
    med.registerFrameAction(() -> framed = true);
    med.updateWindowSize(42);
    med.seekToUpdate(42);
    med.relayout();
    med.startRendering();
    try {
      med.close();
    } catch (Exception e) {
      fail(e);
    }
    assertTrue(framed);
    verify(controller, atLeastOnce()).nextFrame();
    verify(heatmap).setHeatmapSize(42);
    try {
      verify(coordinator).seekToUpdate(42);
    } catch (Exception e) {
      fail();
    }
    verify(graph).recalculateLayout();
    verify(controller).resetCamera();
  }

  @Test
  void testOnClauseUpdate() {
    ClauseUpdate c = new ClauseUpdate(new Clause(new int[]{1, -1, 1000000}), ClauseUpdate.Type.ADD);
    med.onClauseUpdate(null, c);
    try {
      med.close();
      verify(coordinator).addClauseUpdate(c);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  void testStartStopRecording() {
    when(config.getVideoTemplatePath()).thenReturn("test.ogv");
    // start
    med.startOrStopRecording();
    // stop
    med.startOrStopRecording();

    med.startRendering();
    try {
      med.close();
    } catch (Exception e) {
      fail(e);
    }

    InOrder order = inOrder(controller);
    order.verify(controller).startRecording(eq("test.ogv"), anyString());
    order.verify(controller).finishRecording();
  }

  @Test
  void testPauseContinueRecording() {
    when(config.getVideoTemplatePath()).thenReturn("test.ogv");
    med.startOrStopRecording();
    // pause
    med.pauseOrContinueRecording();
    // continue
    med.pauseOrContinueRecording();

    med.startRendering();
    try {
      med.close();
    } catch (Exception e) {
      fail(e);
    }

    InOrder order = inOrder(controller);
    order.verify(controller).stopRecording();
    order.verify(controller).resumeRecording();
  }
}
