package no.aispot.model;

import lombok.Getter;

@Getter
public class CountryNeighborPair
{
	private String country1;
	private String country2;

	public CountryNeighborPair(String country1, String country2)
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
		boolean result = obj instanceof CountryNeighborPair;
		if (result)
		{
			CountryNeighborPair that = (CountryNeighborPair) obj;
			result = (country1.equals(that.country1) && country2.equals(that.country2))
				|| (country1.equals(that.country2) && country1.equals(that.country2));
		}
		return result;
	}
}
