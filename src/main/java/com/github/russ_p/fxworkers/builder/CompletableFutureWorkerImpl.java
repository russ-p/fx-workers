package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javafx.application.Platform;

abstract class CompletableFutureWorkerImpl<A, B, C, D, R> extends AbstractVarArgWorkerImpl<A, B, C, D, R> {

	private static final Executor fxExecutor = Platform::runLater;

	private CompletableFuture<R> future;

	public CompletableFutureWorkerImpl(Consumer<R> onSuccess, Consumer<Throwable> onError, Runnable onRun,
			Runnable onComplete) {
		super(onSuccess, onError, onRun, onComplete);
	}

	protected void doRun(VarArg<A, B, C, D> varArg) {
		if (future != null && !future.isDone()) {
			future.cancel(true);
			future = null;
		}

		future = (executor == null ? CompletableFuture.supplyAsync(() -> exec(varArg))
				: CompletableFuture.supplyAsync(() -> exec(varArg), executor));
		future.handleAsync(this::handler, fxExecutor);

		fxExecutor.execute(onRun);
	}

	private Void handler(R result, Throwable throwable) {
		if (throwable == null) {
			onSuccess.accept(result);
		} else if (!(throwable instanceof CancellationException)) {
			onError.accept(throwable);
		}
		onComplete.run();
		return null;
	}
}
