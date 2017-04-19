package no.aispot.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.reactivex.Observable;
import no.aispot.model.CountryNeighbor;
import no.aispot.service.CountryNeighborService;

@RestController()
@RequestMapping("/neighbors")
public class CountryNeighborController
{
	@Autowired
	private CountryNeighborService countryNeighborService;

	@RequestMapping(method = RequestMethod.GET, path = "/{iso}")
	public Observable<List<String>> getNeighbors(@PathVariable("iso") String iso)
	{
		return countryNeighborService.getNeighbors(iso).map(CountryNeighbor::getBorders);
	}
}
