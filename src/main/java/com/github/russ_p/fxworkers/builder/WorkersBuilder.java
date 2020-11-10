package com.github.russ_p.fxworkers.builder;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.russ_p.fxworkers.BiArgWorker;
import com.github.russ_p.fxworkers.ZeroArgWorker;
import com.github.russ_p.fxworkers.FxWorker;
import com.github.russ_p.fxworkers.UniArgWorker;

import javafx.collections.ObservableList;

public class WorkersBuilder {

	private static Executor executor;

	private static final Runnable NOOP = () -> {
	};
	private static final Consumer<? extends Object> NOOP_C = (a) -> {
	};

	public interface WorkersBuilderCustomizer {

		Runnable completeHandler();

		Consumer<Throwable> errorHandler();

		Runnable runHandler();
	}

	@SuppressWarnings("unchecked")
	private static abstract class Builder<T, U extends Builder<T, ?, ?>, V extends FxWorker> {

		protected Consumer<T> onSuccess;
		protected Runnable onSuccessRun;
		protected Consumer<Throwable> onError;
		protected Runnable onRun;
		protected Runnable onComplete;

		protected boolean notUseFxService = true;

		public U onSuccess(Consumer<T> c) {
			onSuccess = c;
			return (U) this;
		}

		public U onSuccess(Runnable c) {
			onSuccessRun = c;
			return (U) this;
		}

		public U onError(Consumer<Throwable> c) {
			onError = c;
			return (U) this;
		}

		public U onRun(Runnable c) {
			onRun = c;
			return (U) this;
		}

		public U onComplete(Runnable c) {
			onComplete = c;
			return (U) this;
		}

		public U customize(WorkersBuilderCustomizer customizer) {
			onComplete = customizer.completeHandler();
			onRun = customizer.runHandler();
			onError = customizer.errorHandler();
			return (U) this;
		}

		public U service() {
			this.notUseFxService = false;
			return (U) this;
		}

		protected void validate() {
			if (onRun == null) {
				onRun = NOOP;
			}
			if (onSuccess == null) {
				onSuccess = (Consumer<T>) NOOP_C;
			}
			if (onError == null) {
				onError = (Consumer<Throwable>) NOOP_C;
			}
			if (onComplete == null) {
				onComplete = NOOP;
			}
		}

		protected abstract V doBuild();

		public V build() {
			validate();
			V s = doBuild();
			if (executor != null) {
				s.setExecutor(executor);
			}
			return s;
		};

	}

	private WorkersBuilder() {
	}

	public static void setExecutor(Executor exe) {
		WorkersBuilder.executor = exe;
	}

	public static class ConsumerWorkerBuilder<T> extends Builder<T, ConsumerWorkerBuilder<T>, UniArgWorker<T>> {

		private Consumer<T> consum;

		public ConsumerWorkerBuilder(Consumer<T> consum) {
			this.consum = consum;
		}

		@Override
		protected UniArgWorker<T> doBuild() {
			if (notUseFxService)
				return new CompletableFutureWorkerImpl<T, Void, Void, Void, Void>((arg) -> onSuccessRun.run(), onError, onRun,
						onComplete) {

					@Override
					protected Void exec(VarArg<T, Void, Void, Void> arg) {
						consum.accept(arg.getA());
						return null;
					}
				};
			else
				return new FxServiceWorkerImpl<T, Void, Void, Void, Void>((arg) -> onSuccessRun.run(), onError, onRun,
						onComplete) {

					@Override
					protected Void exec(VarArg<T, Void, Void, Void> arg) {
						consum.accept(arg.getA());
						return null;
					}
				};
		}

	}

	public static class BiConsumerWorkerBuilder<T, U>
			extends Builder<T, BiConsumerWorkerBuilder<T, U>, BiArgWorker<T, U>> {

		private BiConsumer<T, U> consum;

		public BiConsumerWorkerBuilder(BiConsumer<T, U> consum) {
			this.consum = consum;
		}

		@Override
		protected BiArgWorker<T, U> doBuild() {
			return new FxServiceWorkerImpl<T, U, Void, Void, Void>((arg) -> onSuccessRun.run(), onError, onRun,
					onComplete) {

				@Override
				protected Void exec(VarArg<T, U, Void, Void> arg) {
					consum.accept(arg.getA(), arg.getB());
					return null;
				}
			};
		}

	}

	public static class FunctionWorkerBuilder<T, R> extends Builder<R, FunctionWorkerBuilder<T, R>, UniArgWorker<T>> {

		private Function<T, R> func;

		public FunctionWorkerBuilder(Function<T, R> func) {
			this.func = func;
		}

		@Override
		protected UniArgWorker<T> doBuild() {
			return new FxServiceWorkerImpl<T, Void, Void, Void, R>(onSuccess, onError, onRun, onComplete) {

				@Override
				protected R exec(VarArg<T, Void, Void, Void> arg) {
					return func.apply(arg.getA());
				}

			};
		}

	}

	public static class BiFunctionWorkerBuilder<T, U, R>
			extends Builder<R, BiFunctionWorkerBuilder<T, U, R>, BiArgWorker<T, U>> {

		private BiFunction<T, U, R> func;

		public BiFunctionWorkerBuilder(BiFunction<T, U, R> func) {
			this.func = func;
		}

		@Override
		protected BiArgWorker<T, U> doBuild() {
			return new FxServiceWorkerImpl<T, U, Void, Void, R>(onSuccess, onError, onRun, onComplete) {

				@Override
				protected R exec(VarArg<T, U, Void, Void> arg) {
					return func.apply(arg.getA(), arg.getB());
				}

			};
		}

	}

	public static class SupplierWorkerBuilder<T> extends Builder<T, SupplierWorkerBuilder<T>, ZeroArgWorker> {

		private Supplier<T> func;

		public SupplierWorkerBuilder(Supplier<T> func) {
			this.func = func;
		}

		@Override
		protected ZeroArgWorker doBuild() {
			return new FxServiceWorkerImpl<Void, Void, Void, Void, T>(onSuccess, onError, onRun, onComplete) {

				@Override
				protected T exec(VarArg<Void, Void, Void, Void> arg) {
					return func.get();
				}

			};
		}

	}

	public static class ListSupplierWorkerBuilder<T> extends SupplierWorkerBuilder<List<T>> {

		public ListSupplierWorkerBuilder(Supplier<List<T>> sup) {
			super(sup);
		}

		public ListSupplierWorkerBuilder<T> setAllTo(ObservableList<T> list) {
			onSuccess = (l) -> list.setAll(l);
			return this;
		}
	}

	public static class ListFunctionWorkerBuilder<T, R> extends FunctionWorkerBuilder<T, List<R>> {

		public ListFunctionWorkerBuilder(Function<T, List<R>> func) {
			super(func);
		}

		public ListFunctionWorkerBuilder<T, R> setAllTo(ObservableList<R> list) {
			onSuccess = (l) -> list.setAll(l);
			return this;
		}
	}

	//

	public static <T> ConsumerWorkerBuilder<T> consumer(Consumer<T> consum) {
		return new ConsumerWorkerBuilder<T>(consum);
	}

	public static <T, U> BiConsumerWorkerBuilder<T, U> biConsumer(BiConsumer<T, U> consum) {
		return new BiConsumerWorkerBuilder<>(consum);
	}

	public static <T, R> FunctionWorkerBuilder<T, R> function(Function<T, R> func) {
		return new FunctionWorkerBuilder<>(func);
	}

	public static <T, U, R> BiFunctionWorkerBuilder<T, U, R> biFunction(BiFunction<T, U, R> func) {
		return new BiFunctionWorkerBuilder<>(func);
	}

	public static <T, R> ListFunctionWorkerBuilder<T, R> listFunction(Function<T, List<R>> func) {
		return new ListFunctionWorkerBuilder<>(func);
	}

	public static <T, R> SupplierWorkerBuilder<T> supplier(Supplier<T> sup) {
		return new SupplierWorkerBuilder<>(sup);
	}

	public static <T, R> ListSupplierWorkerBuilder<T> listSupplier(Supplier<List<T>> sup) {
		return new ListSupplierWorkerBuilder<>(sup);
	}
}
