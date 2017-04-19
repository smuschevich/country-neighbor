package no.aispot.service;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import io.reactivex.Observable;
import no.aispot.model.CountryNeighborDto;

@Service
public class CountryNeighborService
{
	private static final String URL = "https://restcountries.eu/rest/v2/alpha";

	@Autowired
	private RestService restService;
	
	public Observable<CountryNeighborDto> getNeighbors(String iso) {
		Assert.notNull(iso, "ISO must not be null");
		String url = MessageFormat.format("{0}/{1}?fields=borders", URL, iso);
		return restService.get(url, CountryNeighborDto.class);
	}
}
