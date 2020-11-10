package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.github.russ_p.fxworkers.BiArgWorker;
import com.github.russ_p.fxworkers.UniArgWorker;
import com.github.russ_p.fxworkers.ZeroArgWorker;

abstract class AbstractVarArgWorkerImpl<A, B, C, D, R> implements ZeroArgWorker, UniArgWorker<A>, BiArgWorker<A, B> {

	protected final Consumer<R> onSuccess;
	protected final Consumer<Throwable> onError;
	protected final Runnable onRun;
	protected final Runnable onComplete;

	protected Executor executor;

	protected AbstractVarArgWorkerImpl(Consumer<R> onSuccess, Consumer<Throwable> onError, Runnable onRun,
			Runnable onComplete) {
		this.onSuccess = onSuccess;
		this.onError = onError;
		this.onRun = onRun;
		this.onComplete = onComplete;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void run() {
		doRun(new VarArg<>(null, null, null, null));
	}

	@Override
	public void run(A a) {
		doRun(new VarArg<>(a, null, null, null));
	}

	@Override
	public void run(A a, B b) {
		doRun(new VarArg<>(a, b, null, null));
	}

	protected abstract void doRun(VarArg<A, B, C, D> varArg);

	protected abstract R exec(VarArg<A, B, C, D> arg);
}
