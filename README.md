# fx-workers

Фабрика в fluent-стиле для выполнения фоновых задач, как `javafx.concurrent.Service` (или нет, см. ниже)

````
		UniArgWorker<Integer> worker = WorkersBuilder.consumer(a -> doSmth(a))
				.onRun(this::handleRun)           //  runs in JavaFX Application thread
				.onSuccess(this::handleSuccess)   //  runs in JavaFX Application thread
				.onError(this::handleError)       //  runs in JavaFX Application thread
				.onComplete(this::handleComplete) //  runs in JavaFX Application thread
				.build();
				
		worker.run(1);
````

`onRun` - выполняется при запуске

`onSuccess` - выполняется при успешном завершении

`onError` - выполняется при ошибке

`onComplete` - выполняется при успехе и ошибке

Все обработчики запускаются в JavaFX Application thread.

По-умолчанию используется реализация на базе Future. Если нужен именно `javafx.concurrent.Service`, передоставляющий свои дополнительные возможности и свойства, то это нужно явно указать при создании:

````
		UniArgWorker<Object> worker = WorkersBuilder.consumer(a -> doSmt())
				.service() // use javafx.concurrent.Service impl.
				.onRun(this::handleRun)
				.onSuccess(this::onSuccessRun)
				.onError(this::handleError)
				.onComplete(this::handleComplete)
				.build();
		
		Service<Object> service = worker.asService();
		// do smth with service
````
