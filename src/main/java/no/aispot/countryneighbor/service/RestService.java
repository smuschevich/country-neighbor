package no.aispot.countryneighbor.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.reactivex.Observable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Service
public class RestService
{
	private static final Object EXCEPTION_RESULT = new Object();
	
	private RestTemplate restTemplate;

	public <R> Observable<R> get(String url, Class<R> responseType)
	{
		return Observable.<R>create(emitter ->
		{
			Supplier<R> supplier = () -> getRestTemplate().getForObject(url, responseType);
			CompletableFuture.supplyAsync(supplier)
				.exceptionally(e -> {
					emitter.onError(e.getCause());
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
	
	public <T, R> Observable<T> post(String url, T request, Class<R> responseType)
	{
		// TODO: implement in the future
		throw new IllegalStateException("not implemented");
	}
	
	public <T> Observable<Void> put(String url, T request)
	{
		// TODO: implement in the future
		throw new IllegalStateException("not implemented");
	}
	
	public Observable<Void> delete(String url)
	{
		// TODO: implement in the future
		throw new IllegalStateException("not implemented");
	}
	
	public RestTemplate getRestTemplate()
	{
		if (restTemplate == null)
		{
			restTemplate = new RestTemplate();
		}
		return restTemplate;
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
