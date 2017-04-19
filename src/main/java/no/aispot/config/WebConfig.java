package no.aispot.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import no.aispot.api.handler.ObservableReturnValueHandler;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers)
	{
		returnValueHandlers.add(new ObservableReturnValueHandler());
	}
}
