package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import javafx.application.Platform;

abstract class FutureWorkerImpl<A, B, C, D, R> extends AbstractVarArgWorkerImpl<A, B, C, D, R> {

	public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(8);

	public FutureWorkerImpl(Consumer<R> onSuccess, Consumer<Throwable> onError, Runnable onRun, Runnable onComplete) {
		super(onSuccess, onError, onRun, onComplete);
	}

	@Override
	protected void doRun(VarArg<A, B, C, D> arg) {
		if (Platform.isFxApplicationThread()) {
			onRun.run();
		} else {
			Platform.runLater(onRun);
		}

		(executor == null ? EXECUTOR : executor)
				.execute(new Task(() -> exec(arg)));
	}

	protected abstract R exec(VarArg<A, B, C, D> arg);

	private final class Task extends FutureTask<R> {

		public Task(Callable<R> callable) {
			super(callable);
		}

		@Override
		protected void done() {
			if (Platform.isFxApplicationThread()) {
				report();
			} else {
				Platform.runLater(this::report);
			}
		}

		/**
		 * executes in FxApplicationThread
		 */
		private void report() {
			if (!this.isCancelled()) {
				try {
					onSuccess.accept(get());
				} catch (InterruptedException e) {
				} catch (ExecutionException e) {
					onError.accept(e.getCause());
				}
			}
			onComplete.run();
		}

	}

}
