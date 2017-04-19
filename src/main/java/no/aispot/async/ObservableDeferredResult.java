package no.aispot.async;

import org.springframework.web.context.request.async.DeferredResult;

import io.reactivex.Observable;

public class ObservableDeferredResult<T> extends DeferredResult<T>
{
	public ObservableDeferredResult(Observable<T> observable)
	{
		new DeferredResultObserver<T>(observable, this);
	}
}
