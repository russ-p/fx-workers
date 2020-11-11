package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import com.github.russ_p.fxworkers.ZeroArgWorker;

import javafx.application.Platform;

public class ImplementationsTest extends ApplicationTest {

	private static final int RUNS_COUNT = 100;//_000;

	private AtomicInteger counterRun = new AtomicInteger(0);
	private AtomicInteger counterDo = new AtomicInteger(0);
	private AtomicInteger counterSuccess = new AtomicInteger(0);
	private AtomicInteger counterError = new AtomicInteger(0);
	private AtomicInteger counterComplete = new AtomicInteger(0);
	private AtomicInteger counterCancel = new AtomicInteger(0);

	private Executor e = Executors.newSingleThreadExecutor();

	private int doSmt() {
		int res = counterDo.incrementAndGet();
		try {
			Thread.sleep(10);
		} catch (InterruptedException | CancellationException e ) {
			counterCancel.getAndIncrement();
		}
		return res;
	}

	private void handleRun() {
		counterRun.incrementAndGet();
	}

	private void handleSuccess(Integer o) {
		counterSuccess.incrementAndGet();
	}

	private void handleError(Throwable t) {
		counterError.incrementAndGet();
	}

	private void handleComplete() {
		counterComplete.incrementAndGet();
	}

	private void printCounters() {
		System.out.println("     RUN: " + counterRun);
		System.out.println("      DO: " + counterDo);
		System.out.println(" SUCCESS: " + counterSuccess);
		System.out.println("   ERROR: " + counterError);
		System.out.println("COMPLETE: " + counterComplete);
		System.out.println("  CANCEL: " + counterCancel);
	}

	@Before
	public void setUp() throws Exception {
		counterRun.set(0);
		counterDo.set(0);
		counterSuccess.set(0);
		counterError.set(0);
		counterComplete.set(0);
		counterCancel.set(0);

		System.gc();
	}

	@After
	public void end() throws Exception {
		printCounters();
	}

	@Test
	public void testCompletableFutureWorker() {
		ZeroArgWorker completableFutureWorker = new CompletableFutureWorkerImpl<Void, Void, Void, Void, Integer>(
				this::handleSuccess, this::handleError,
				this::handleRun, this::handleComplete) {

			@Override
			protected Integer exec(VarArg<Void, Void, Void, Void> arg) {
				return doSmt();
			}
		};
		completableFutureWorker.setExecutor(e);

		int i = 0;
		while (i++ < RUNS_COUNT) {
			// worker.run(1);
			Platform.runLater(() -> completableFutureWorker.run());
			// while (counterSuccess.get() < i) { }
		}
		i--;
		while (counterComplete.get() < i) {
		}
		
		System.out.println("ImplementationsTest.testCompletableFutureWorker()");
	}

	@Test
	public void testFutureWorker() {
		ZeroArgWorker worker = new FutureWorkerImpl<Void, Void, Void, Void, Integer>(
				this::handleSuccess, this::handleError,
				this::handleRun, this::handleComplete) {

			@Override
			protected Integer exec(VarArg<Void, Void, Void, Void> arg) {
				return doSmt();
			}
		};
		worker.setExecutor(e);

		int i = 0;
		while (i++ < RUNS_COUNT) {
			// worker.run(1);
			Platform.runLater(() -> worker.run());
			// while (counterSuccess.get() < i) { }
		}

		i--;
		while (counterComplete.get() < i) {
		}
		
		System.out.println("ImplementationsTest.testFutureWorker()");
	}

	@Test
	public void testFxServiceWorker() {
		ZeroArgWorker worker = new FxServiceWorkerImpl<Void, Void, Void, Void, Integer>(
				this::handleSuccess, this::handleError,
				this::handleRun, this::handleComplete) {

			@Override
			protected Integer exec(VarArg<Void, Void, Void, Void> arg) {
				return doSmt();
			}
		};
		worker.setExecutor(e);

		int i = 0;
		while (i++ < RUNS_COUNT) {
			// worker.run(1);
			Platform.runLater(() -> worker.run());
			// while (counterSuccess.get() < i) { }
		}

		i--;
		while (counterComplete.get() < i) {
		}
		
		System.out.println("ImplementationsTest.testFxServiceWorker()");
	}

}
