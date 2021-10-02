package com.maxwaterfall.webcrawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes tasks using an ExecutorService.
 *
 * <p>Calling waitForCompletion() will block until all tasks have been completed.
 */
public class TaskExecutor {

  private final AtomicInteger noOfTasks;
  private final ExecutorService executorService;

  public TaskExecutor(ExecutorService executorService) {
    this.noOfTasks = new AtomicInteger(0);
    this.executorService = executorService;
  }

  public synchronized void scheduleTask(Runnable task) {
    noOfTasks.incrementAndGet();
    executorService.submit(
        () -> {
          try {
            task.run();
          } finally {
            completeTask();
          }
        });
  }

  private synchronized void completeTask() {
    noOfTasks.decrementAndGet();
    notifyAll();
  }

  public synchronized void waitForCompletion() throws InterruptedException {
    while (noOfTasks.get() > 0) {
      wait();
    }
  }
}
