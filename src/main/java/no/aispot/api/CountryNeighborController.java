package no.aispot.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.reactivex.Observable;
import no.aispot.model.CountryNeighborDto;
import no.aispot.model.CountryNeighborPair;
import no.aispot.service.CountryNeighborService;

@RestController()
@RequestMapping("/neighbors")
public class CountryNeighborController extends AbstractController
{
	private static final long DEFAULT_TIMEOUT = 3000; // 3 seconds

	@Autowired
	private CountryNeighborService countryNeighborService;

	// TODO: List<List<String>> will be replaced by list of objects with custom serialization converter
	@RequestMapping(method = RequestMethod.GET, path = "/{iso}")
	public Observable<List<List<String>>> getNeighbors(@PathVariable("iso") String iso,
		@RequestParam(name = "timeout", required = false) Long timeout)
	{
		// 3rd party service is very fast
		// do not apply given timeout to the first call to give a chance to test partial responses
		return countryNeighborService.getNeighbors(iso, DEFAULT_TIMEOUT)
			.map(CountryNeighborDto::getBorders)
			.flatMap(neighbors -> getJointNeighbors(neighbors, Optional.ofNullable(timeout).orElse(DEFAULT_TIMEOUT)))
			.map(this::mapNeighbors);
	}

	private Observable<Set<CountryNeighborPair>> getJointNeighbors(List<String> neighbors, long timeout)
	{
		List<Observable<List<CountryNeighborPair>>> sources = new ArrayList<>();
		for (String neighbor : neighbors)
		{
			sources.add(countryNeighborService.getNeighbors(neighbor)
				.map(subNeighbors ->
				{
					List<CountryNeighborPair> result = new ArrayList<>();
					for (String subNeighbor : subNeighbors.getBorders())
					{
						if (!neighbor.equals(subNeighbor) && neighbors.contains(subNeighbor))
						{
							result.add(new CountryNeighborPair(neighbor, subNeighbor));
						}
					}
					return result;
				}));
		}
		return mergeJointNeighbors(sources, timeout);
	}
	
	private Observable<Set<CountryNeighborPair>> mergeJointNeighbors(List<Observable<List<CountryNeighborPair>>> sources,
		long timeout)
	{
		Observable<Set<CountryNeighborPair>> result;
		if (!sources.isEmpty())
		{
			Set<CountryNeighborPair> seed = new HashSet<>();
			result = Observable.merge(sources)
				.timeout(timeout, TimeUnit.MILLISECONDS, Observable.just(Collections.emptyList()))
				.reduce(seed, (r, neighbors) ->
				{
					for (CountryNeighborPair pairs : neighbors)
					{
						r.add(pairs);
					}
					return r;
				})
				.toObservable();
		}
		else
		{
			result = Observable.just(Collections.emptySet());
		}
		return result;
	}

	private List<List<String>> mapNeighbors(Set<CountryNeighborPair> neighbors)
	{
		return neighbors.stream().map(pair ->
		{
			List<String> result = new ArrayList<>();
			result.add(pair.getCountry1());
			result.add(pair.getCountry2());
			return result;
		}).collect(Collectors.toList());
	}
}
