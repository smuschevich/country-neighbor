package no.aispot.countryneighbor.async;

import org.springframework.web.context.request.async.DeferredResult;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableObserver;

class DeferredResultObserver<T> extends DisposableObserver<T> implements Runnable
{
	private final DeferredResult<T> deferredResult;

	public DeferredResultObserver(Observable<T> observable, DeferredResult<T> deferredResult)
	{
		this.deferredResult = deferredResult;
		this.deferredResult.onTimeout(this);
		this.deferredResult.onCompletion(this);
		observable.subscribe(this);
	}

	@Override
	public void onNext(T value)
	{
		deferredResult.setResult(value);
	}

	@Override
	public void onError(Throwable e)
	{
		deferredResult.setErrorResult(e);
	}

	@Override
	public void onComplete()
	{
		// nothing to do
	}

	@Override
	public void run()
	{
		// dispose the observer in case of success or error
		this.dispose();
	}
}
