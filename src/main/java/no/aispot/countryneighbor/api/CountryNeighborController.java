package no.aispot.countryneighbor.api;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import no.aispot.countryneighbor.model.CountryNeighborDto;
import no.aispot.countryneighbor.model.CountryNeighborPair;
import no.aispot.countryneighbor.service.CountryNeighborService;

@RestController()
@RequestMapping("/neighbors")
public class CountryNeighborController extends AbstractController
{
	private static final long DEFAULT_TIMEOUT = 3000; // 3 seconds

	@Autowired
	private CountryNeighborService countryNeighborService;

	// TODO: List<List<String>> will be replaced by list of objects with custom serialization converter
	@RequestMapping(method = RequestMethod.GET, path = "/{iso}")
	@ResponseBody
	public Observable<List<List<String>>> getNeighbors(@PathVariable("iso") String iso,
		@RequestParam(name = "timeout", required = false) Long timeout)
	{
		long t = Optional.ofNullable(timeout).orElse(DEFAULT_TIMEOUT);
		return countryNeighborService.getNeighbors(iso)
			.map(CountryNeighborDto::getBorders)
			.flatMap(neighbors -> getJointNeighbors(neighbors))
			.timeout(t, TimeUnit.MILLISECONDS, Observable.just(Collections.emptyList()))
			.reduce(new HashSet<>(), getDistinctReducer())
			.toObservable()
			.map(this::mapNeighbors);
	}

	private Observable<List<CountryNeighborPair>> getJointNeighbors(List<String> neighbors)
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
		return !sources.isEmpty() ? Observable.merge(sources) : Observable.just(Collections.emptyList());
	}
	
	private BiFunction<Set<CountryNeighborPair>, List<CountryNeighborPair>, Set<CountryNeighborPair>> getDistinctReducer()
	{
		return (r, neighbors) ->
		{
			for (CountryNeighborPair pairs : neighbors)
			{
				r.add(pairs);
			}
			return r;
		};
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
