package com.github.russ_p.fxworkers.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import com.github.russ_p.fxworkers.BiArgWorker;
import com.github.russ_p.fxworkers.UniArgWorker;
import com.github.russ_p.fxworkers.ZeroArgWorker;

import javafx.application.Platform;

public class WorkersBuilderTest extends ApplicationTest {

	private AtomicInteger counterRun = new AtomicInteger(0);
	private AtomicInteger counterDo = new AtomicInteger(0);
	private AtomicInteger counterSuccess = new AtomicInteger(0);
	private AtomicInteger counterError = new AtomicInteger(0);
	private AtomicInteger counterComplete = new AtomicInteger(0);

	@Before
	public void setUp() throws Exception {
		counterRun.set(0);
		counterDo.set(0);
		counterSuccess.set(0);
		counterError.set(0);
		counterComplete.set(0);
	}

	private int doSmt() {
		return counterDo.incrementAndGet();
	}

	private void handleRun() {
		counterRun.incrementAndGet();
	}

	private void handleSuccess(Object o) {
		counterSuccess.incrementAndGet();
	}

	private void onSuccessRun() {
		counterSuccess.incrementAndGet();
	}

	private void handleError(Throwable t) {
		counterError.incrementAndGet();
	}

	private void handleComplete() {
		counterComplete.incrementAndGet();
	}

	@Test
	public void testConsumerFxService() throws Exception {
		UniArgWorker<Object> worker = WorkersBuilder.consumer(a -> doSmt())
				.service()
				.onRun(this::handleRun)
				.onSuccess(this::onSuccessRun)
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();
		
		Platform.runLater(() -> worker.run(1));
		while (counterSuccess.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasPositiveValue();
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasValue(0);
	}

	@Test
	public void testConsumer() throws Exception {
		UniArgWorker<Object> worker = WorkersBuilder.consumer(a -> doSmt())
				.onRun(this::handleRun)
				.onSuccess(this::onSuccessRun)
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();

		Platform.runLater(() -> worker.run(1));
		while (counterSuccess.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasPositiveValue();
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasValue(0);
	}

	@Test
	public void testFunction() throws Exception {
		UniArgWorker<Object> worker = WorkersBuilder.function(a -> doSmt())
				.onRun(this::handleRun)
				.onSuccess((v) -> this.handleSuccess(v))
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();

		Platform.runLater(() -> worker.run(1));
		while (counterSuccess.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasPositiveValue();
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasValue(0);
	}

	@Test
	public void testSupplier() throws Exception {
		ZeroArgWorker worker = WorkersBuilder.supplier(this::doSmt)
				.onRun(this::handleRun)
				.onSuccess((v) -> this.handleSuccess(v))
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();

		Platform.runLater(() -> worker.run());
		while (counterSuccess.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasPositiveValue();
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasValue(0);
	}

	@Test
	public void testBiConsumer() throws Exception {
		BiArgWorker<Object, Object> worker = WorkersBuilder.biConsumer((a, b) -> doSmt())
				.onRun(this::handleRun)
				.onSuccess(this::onSuccessRun)
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();

		Platform.runLater(() -> worker.run(1, 2));
		while (counterSuccess.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasPositiveValue();
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasValue(0);
	}

	@Test
	public void testBiFunction() throws Exception {
		BiArgWorker<Object, Object> worker = WorkersBuilder.biFunction((a, b) -> doSmt())
				.onRun(this::handleRun)
				.onSuccess(this::handleSuccess)
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();

		Platform.runLater(() -> worker.run(1, 2));
		while (counterSuccess.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasPositiveValue();
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasValue(0);
	}

	@Test
	public void testConsumerException() throws Exception {
		UniArgWorker<Object> worker = WorkersBuilder.consumer(a -> {
			doSmt();
			throw new IllegalStateException("Test");
		})
				.onRun(this::handleRun)
				.onSuccess(this::onSuccessRun)
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();

		Platform.runLater(() -> worker.run(1));
		while (counterComplete.get() < 1) {
		}

		assertThat(counterRun).hasPositiveValue();
		assertThat(counterDo).hasPositiveValue();
		assertThat(counterSuccess).hasValue(0);
		assertThat(counterComplete).hasPositiveValue();
		assertThat(counterError).hasPositiveValue();
	}
}
