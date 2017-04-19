package no.aispot.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import no.aispot.model.CountryNeighborDto;
import no.aispot.model.CountryNeighborPair;
import no.aispot.service.CountryNeighborService;

@RestController()
@RequestMapping("/neighbors")
public class CountryNeighborController extends AbstractController
{
	@Autowired
	private CountryNeighborService countryNeighborService;

	// TODO: List<List<String>> will be replaced by list of objects with custom serialization converter
	@RequestMapping(method = RequestMethod.GET, path = "/{iso}")
	public Observable<List<List<String>>> getNeighbors(@PathVariable("iso") String iso)
	{
		return countryNeighborService.getNeighbors(iso)
			.map(CountryNeighborDto::getBorders)
			.flatMap(this::getJointNeighbors)
			.map(this::distinctNeighbors);
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
		return !sources.isEmpty() ? Observable.combineLatest(sources, getJointNeighborsCombiner())
			: Observable.just(Collections.emptyList());
	}
	
	@SuppressWarnings("unchecked")
	private Function<Object[], List<CountryNeighborPair>> getJointNeighborsCombiner()
	{
		return neighbors ->
		{
			List<CountryNeighborPair> result = new ArrayList<>();
			for (Object pairs : neighbors)
			{
				result.addAll((List<CountryNeighborPair>) pairs);
			}
			return result;
		};
	}

	private List<List<String>> distinctNeighbors(List<CountryNeighborPair> neighbors)
	{
		return new HashSet<CountryNeighborPair>(neighbors).stream().map(pair ->
		{
			List<String> result = new ArrayList<>();
			result.add(pair.getCountry1());
			result.add(pair.getCountry2());
			return result;
		}).collect(Collectors.toList());
	}
}
