package com.github.russ_p.fxworkers.builder;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.russ_p.fxworkers.BiArgWorker;
import com.github.russ_p.fxworkers.UniArgWorker;
import com.github.russ_p.fxworkers.ZeroArgWorker;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

abstract class FxServiceWorkerImpl<A, B, C, D, R> extends Service<R>
		implements ZeroArgWorker, UniArgWorker<A>, BiArgWorker<A, B> {

	private final AtomicReference<VarArg<A, B, C, D>> varArg = new AtomicReference<>();
	private final Consumer<R> onSuccess;
	private final Consumer<Throwable> onError;
	private final Runnable onRun;
	private final Runnable onComplete;

	public FxServiceWorkerImpl(Consumer<R> onSuccess, Consumer<Throwable> onError, Runnable onRun, Runnable onComplete) {
		this.onSuccess = onSuccess;
		this.onError = onError;
		this.onRun = onRun;
		this.onComplete = onComplete;
	}

	@Override
	public void run() {
		this.varArg.set(new VarArg<A, B, C, D>(null, null, null, null));
		super.restart();
	}

	@Override
	public void run(A a) {
		this.varArg.set(new VarArg<A, B, C, D>(a, null, null, null));
		super.restart();
	}

	@Override
	public void run(A a, B b) {
		this.varArg.set(new VarArg<A, B, C, D>(a, b, null, null));
		super.restart();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Service<R> asService() {
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Service<T> asService(Class<T> clazz) {
		return (Service<T>) this;
	}

	@Override
	protected Task<R> createTask() {
		VarArg<A, B, C, D> arg = this.varArg.get();
		return new Task<R>() {

			@Override
			protected R call() throws Exception {
				return exec(arg);
			}

			@Override
			protected void succeeded() {
				onSuccess.accept(getValue());
				onComplete.run();
			}

			@Override
			protected void failed() {
				onError.accept(getException());
				onComplete.run();
			}

			@Override
			protected void running() {
				onRun.run();
			}

			protected void scheduled() {
			}

			@Override
			protected void cancelled() {
				onComplete.run();
			}
		};
	}

	protected abstract R exec(VarArg<A, B, C, D> arg);

	public VarArg<A, B, C, D> getArg() {
		return varArg.get();
	}

}