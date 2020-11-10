package com.github.russ_p.fxworkers.builder;

import lombok.Value;

@Value
class VarArg<A, B, C, D> {
	private final A a;
	private final B b;
	private final C c;
	private final D d;
}