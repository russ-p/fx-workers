package com.github.russ_p.fxworkers;

import java.util.concurrent.Executor;

import javafx.concurrent.Service;

public interface FxWorker {

	default <R> Service<R> asService() {
		throw new UnsupportedOperationException("Not implemented");
	}

	default <R> Service<R> asService(Class<R> clazz) {
		throw new UnsupportedOperationException("Not implemented");
	}

	void setExecutor(Executor executor);
}
