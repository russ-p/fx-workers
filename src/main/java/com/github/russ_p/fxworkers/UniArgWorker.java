package com.github.russ_p.fxworkers;

public interface UniArgWorker<T> extends FxWorker {
	void run(T t);
}