package com.github.russ_p.fxworkers;

public interface BiArgWorker<T, U> extends FxWorker{
	void run(T t, U u);
}