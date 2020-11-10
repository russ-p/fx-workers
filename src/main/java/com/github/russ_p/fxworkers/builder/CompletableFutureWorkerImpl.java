package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javafx.application.Platform;

abstract class CompletableFutureWorkerImpl<A, B, C, D, R> extends AbstractVarArgWorkerImpl<A, B, C, D, R> {

	private static final Executor fxExecutor = Platform::runLater;

	public CompletableFutureWorkerImpl(Consumer<R> onSuccess, Consumer<Throwable> onError, Runnable onRun,
			Runnable onComplete) {
		super(onSuccess, onError, onRun, onComplete);
	}

	protected void doRun(VarArg<A, B, C, D> varArg) {
		(executor == null ? CompletableFuture.supplyAsync(() -> exec(varArg))
				: CompletableFuture.supplyAsync(() -> exec(varArg), executor))
						.handleAsync(this::handler, fxExecutor);

		fxExecutor.execute(onRun);
	}

	private Void handler(R result, Throwable throwable) {
		if (throwable == null) {
			onSuccess.accept(result);
		} else {
			onError.accept(throwable);
		}
		onComplete.run();
		return null;
	}
}
