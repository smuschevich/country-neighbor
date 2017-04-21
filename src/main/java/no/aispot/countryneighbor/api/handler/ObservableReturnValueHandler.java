package no.aispot.countryneighbor.api.handler;

import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.method.support.AsyncHandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.reactivex.Observable;
import no.aispot.countryneighbor.async.ObservableDeferredResult;

public class ObservableReturnValueHandler implements AsyncHandlerMethodReturnValueHandler
{

	@Override
	public boolean supportsReturnType(MethodParameter returnType)
	{
		return Observable.class.isAssignableFrom(returnType.getParameterType());
	}
	
	@Override
	public boolean isAsyncReturnValue(Object returnValue, MethodParameter returnType)
	{
		return returnValue != null && supportsReturnType(returnType);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest) throws Exception
	{
		Observable<?> observable = (Observable<?>) returnValue;
		ObservableDeferredResult<?> result = new ObservableDeferredResult<>(observable);
		WebAsyncUtils.getAsyncManager(webRequest).startDeferredResultProcessing(result, mavContainer);
	}
}
