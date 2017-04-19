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
import no.aispot.model.CountryNeighbor;
import no.aispot.service.CountryNeighborService;

@RestController()
@RequestMapping("/neighbors")
public class CountryNeighborController extends AbstractController
{
	@Autowired
	private CountryNeighborService countryNeighborService;

	@RequestMapping(method = RequestMethod.GET, path = "/{iso}")
	public Observable<List<List<String>>> getNeighbors(@PathVariable("iso") String iso)
	{
		return countryNeighborService.getNeighbors(iso)
			.map(CountryNeighbor::getBorders)
			.flatMap(this::getJointNeighbors)
			.map(this::distinctNeighbors);
	}

	private Observable<List<Pair>> getJointNeighbors(List<String> neighbors)
	{
		List<Observable<List<Pair>>> sources = new ArrayList<>();
		for (String neighbor : neighbors)
		{
			sources.add(countryNeighborService.getNeighbors(neighbor)
				.map(subNeighbors ->
				{
					List<Pair> result = new ArrayList<>();
					for (String subNeighbor : subNeighbors.getBorders())
					{
						if (!neighbor.equals(subNeighbor) && neighbors.contains(subNeighbor))
						{
							result.add(new Pair(neighbor, subNeighbor));
						}
					}
					return result;
				}));
		}
		return !sources.isEmpty() ? Observable.combineLatest(sources, getJointNeighborsCombiner())
			: Observable.just(Collections.emptyList());
	}
	
	@SuppressWarnings("unchecked")
	private Function<Object[], List<Pair>> getJointNeighborsCombiner()
	{
		return neighbors ->
		{
			List<Pair> result = new ArrayList<>();
			for (Object pairs : neighbors)
			{
				result.addAll((List<Pair>) pairs);
			}
			return result;
		};
	}

	private List<List<String>> distinctNeighbors(List<Pair> neighbors)
	{
		return new HashSet<Pair>(neighbors).stream().map(pair ->
		{
			List<String> result = new ArrayList<>();
			result.add(pair.country1);
			result.add(pair.country2);
			return result;
		}).collect(Collectors.toList());
	}

	private final class Pair
	{
		private String country1;
		private String country2;

		public Pair(String country1, String country2)
		{
			this.country1 = country1;
			this.country2 = country2;
		}
		
		@Override
		public int hashCode()
		{
			return country1.hashCode() | country2.hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			boolean result = obj instanceof Pair;
			if (result)
			{
				Pair that = (Pair) obj;
				result = (country1.equals(that.country1) && country2.equals(that.country2))
					|| (country1.equals(that.country2) && country1.equals(that.country2));
			}
			return result;
		}
	}
}
