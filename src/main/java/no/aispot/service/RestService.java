package no.aispot.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.reactivex.Observable;

@Service
public class RestService
{
	private static final Object EXCEPTION_RESULT = new Object();

	public <R> Observable<R> get(String url, Class<R> responseType)
	{
		return get(url, responseType, Collections.emptyMap());
	}

	public <R> Observable<R> get(String url, Class<R> responseType, Map<String, ?> uriVariables)
	{
		return Observable.<R>create(emitter ->
		{
			Supplier<R> supplier = () -> new RestTemplate().getForObject(url, responseType, uriVariables);
			CompletableFuture.supplyAsync(supplier)
				.exceptionally(e -> {
					emitter.onError(e);
					return getExceptionResult();
				})
				.thenAccept(r -> {
					if (!isExceptionResult(r)) {
						emitter.onNext(r);
						emitter.onComplete();
					}
				});
		});
	}
	
	public <T, R> Observable<T> post(String url, T request, Class<R> responseType, Map<String, ?> uriVariables)
	{
		// TODO: implement in the future
		throw new IllegalStateException("not implemented");
	}
	
	public <T> Observable<Void> put(String url, T request, Map<String, ?> uriVariables)
	{
		// TODO: implement in the future
		throw new IllegalStateException("not implemented");
	}
	
	public Observable<Void> delete(String url, Map<String, ?> uriVariables)
	{
		// TODO: implement in the future
		throw new IllegalStateException("not implemented");
	}
	
	@SuppressWarnings("unchecked")
	private static <R> R getExceptionResult()
	{
		return (R) EXCEPTION_RESULT;
	}
	
	private static boolean isExceptionResult(Object result)
	{
		return EXCEPTION_RESULT == result;
	}
}
