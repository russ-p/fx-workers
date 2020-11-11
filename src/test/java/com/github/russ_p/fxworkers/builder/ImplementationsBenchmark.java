package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.testfx.api.FxToolkit;

import com.github.russ_p.fxworkers.ZeroArgWorker;

import javafx.application.Platform;

public class ImplementationsBenchmark {

	@State(Scope.Benchmark)
	public abstract static class WorkerExecutionPlan {

		protected AtomicInteger counterRun = new AtomicInteger(0);
		protected AtomicInteger counterDo = new AtomicInteger(0);
		protected AtomicInteger counterSuccess = new AtomicInteger(0);
		protected AtomicInteger counterError = new AtomicInteger(0);
		protected AtomicInteger counterComplete = new AtomicInteger(0);

		protected ZeroArgWorker worker;

		protected ExecutorService e = Executors.newFixedThreadPool(1);

		protected void setup() {
			try {
				FxToolkit.registerPrimaryStage();
			} catch (TimeoutException e1) {
				e1.printStackTrace();
			}
			if (e.isTerminated()) {
				e = Executors.newFixedThreadPool(8);
			}
		}

		protected void cleanUp() {
			try {
				e.awaitTermination(1, TimeUnit.SECONDS);
				e.shutdownNow();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				FxToolkit.cleanupStages();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
	}

	@State(Scope.Benchmark)
	public static class CompletableFutureWorkerExecutionPlan extends WorkerExecutionPlan {

		@Param({ "1000" })
		public int iterations;

		@Setup(Level.Invocation)
		public void setUp() {
			super.setup();
			worker = new CompletableFutureWorkerImpl<Void, Void, Void, Void, Integer>(
					(i) -> counterSuccess.incrementAndGet(),
					(t) -> counterError.incrementAndGet(),
					() -> counterRun.incrementAndGet(),
					() -> counterComplete.incrementAndGet()) {

				@Override
				protected Integer exec(VarArg<Void, Void, Void, Void> arg) {
					return counterDo.incrementAndGet();
				}
			};
			worker.setExecutor(e);
		}

		@TearDown(Level.Invocation)
		public void down() {
			super.cleanUp();
		}
	}

	@State(Scope.Benchmark)
	public static class FutureWorkerExecutionPlan extends WorkerExecutionPlan {

		@Param({ "1000" })
		public int iterations;

		@Setup(Level.Invocation)
		public void setUp() {
			super.setup();

			worker = new FutureWorkerImpl<Void, Void, Void, Void, Integer>(
					(i) -> counterSuccess.incrementAndGet(),
					(t) -> counterError.incrementAndGet(),
					() -> counterRun.incrementAndGet(),
					() -> counterComplete.incrementAndGet()) {

				@Override
				protected Integer exec(VarArg<Void, Void, Void, Void> arg) {
					return counterDo.incrementAndGet();
				}
			};
			worker.setExecutor(e);
		}

		@TearDown(Level.Invocation)
		public void down() {
			super.cleanUp();
		}
	}

	@State(Scope.Benchmark)
	public static class ServiceWorkerExecutionPlan extends WorkerExecutionPlan {

		@Param({ "1000" })
		public int iterations;

		@Setup(Level.Invocation)
		public void setUp() {
			super.setup();

			worker = new FxServiceWorkerImpl<Void, Void, Void, Void, Integer>(
					(i) -> counterSuccess.incrementAndGet(),
					(t) -> counterError.incrementAndGet(),
					() -> counterRun.incrementAndGet(),
					() -> counterComplete.incrementAndGet()) {

				@Override
				protected Integer exec(VarArg<Void, Void, Void, Void> arg) {
					return counterDo.incrementAndGet();
				}
			};
			worker.setExecutor(e);
		}

		@TearDown(Level.Invocation)
		public void down() {
			super.cleanUp();
		}
	}

	@Test
	public void launchBenchmark() throws Exception {
		Options opt = new OptionsBuilder()
				// Specify which benchmarks to run.
				// You can be more specific if you'd like to run only one benchmark per test.
				.include(this.getClass().getName() + ".*")
				// Set the following options as needed
				.mode(Mode.AverageTime)
				.timeUnit(TimeUnit.MICROSECONDS)
				.warmupTime(TimeValue.seconds(2))
				.warmupIterations(2)
				.measurementTime(TimeValue.seconds(10))
				.measurementIterations(2)
				.threads(2)
				.forks(1)
				.shouldFailOnError(true)
				.shouldDoGC(true)
				// .jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
				.addProfiler(org.openjdk.jmh.profile.GCProfiler.class)
//				.addProfiler(org.openjdk.jmh.profile.HotspotMemoryProfiler.class)
				.build();

		new Runner(opt).run();
	}

	@Benchmark
	public void testCompletableFutureWorker(CompletableFutureWorkerExecutionPlan plan) {
		for (int i = plan.iterations; i > 0; i--) {
			Platform.runLater(() -> plan.worker.run());
			while (plan.counterComplete.get() < plan.iterations - i + 1) {
			}
		}
		while (plan.counterComplete.get() < plan.iterations) {
		}
	}

	@Benchmark
	public void testFutureWorker(FutureWorkerExecutionPlan plan) {
		for (int i = plan.iterations; i > 0; i--) {
			Platform.runLater(() -> plan.worker.run());
			while (plan.counterComplete.get() < plan.iterations - i + 1) {
			}
		}
		while (plan.counterComplete.get() < plan.iterations) {
		}
	}

	@Benchmark
	public void testServiceWorker(ServiceWorkerExecutionPlan plan) {
		for (int i = plan.iterations; i > 0; i--) {
			Platform.runLater(() -> plan.worker.run());
			while (plan.counterComplete.get() < plan.iterations - i + 1) {
			}
		}
		while (plan.counterComplete.get() < plan.iterations) {
		}
	}

}
