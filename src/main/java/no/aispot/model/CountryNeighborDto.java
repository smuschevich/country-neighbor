package no.aispot.model;

import java.util.List;

import lombok.Getter;

@Getter
public class CountryNeighborDto
{
	private String flag;
	private List<String> borders;

	public CountryNeighborDto()
	{
		super();
	}

	public CountryNeighborDto(String flag, List<String> borders)
	{
		this.flag = flag;
		this.borders = borders;
	}
}
