package no.aispot.countryneighbor.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AbstractController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

	/**
	 * Very simple exception handler for REST controllers.
	 * 
	 * @param ex thrown by REST controller
	 * @return the error message
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleException(Throwable ex) {
		LOGGER.error("Unexpected error occurred while processing request", ex);
		return "HTTP Status 400 Bad Request: " + ex.getMessage();
	}
}
