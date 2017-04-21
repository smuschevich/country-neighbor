package no.aispot.countryneighbor.service

import java.text.MessageFormat

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

import io.reactivex.Observable
import no.aispot.countryneighbor.model.CountryNeighborDto
import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

@SpringBootTest
class CountryNeighborServiceSpec extends Specification {
	
	@Autowired
	private CountryNeighborService countryNeighborService

	private RestService restService
	private Random random = new Random()
	
	def setup() {
		restService = Stub(RestService)
		countryNeighborService.restService = restService
	}
	
	def "getNeighbors should return list of neighbors for iso code"() {
		setup:
			def iso = random.nextInt().toString()
			def url = MessageFormat.format("{0}/{1}?fields=borders", CountryNeighborService.URL, iso)
			def expectedResult = new CountryNeighborDto(random.nextInt().toString(),
				Collections.singletonList(random.nextInt().toString()))
			restService.get(url, CountryNeighborDto.class) >> Observable.just(expectedResult)
			def conds = new AsyncConditions()
		when:
			def result = countryNeighborService.getNeighbors(iso)
		then:
			result.subscribe { r ->
				conds.evaluate {
					assert r == expectedResult
					assert r.getFlag() == expectedResult.getFlag()
					assert r.getBorders() == expectedResult.getBorders()
				}
			}
			conds.await()
	}
	
	def "getNeighbors should throw exception for null iso code"() {
		setup:
			def iso = null
		when:
			def result = countryNeighborService.getNeighbors(iso)
		then:
			thrown IllegalArgumentException
	}
}