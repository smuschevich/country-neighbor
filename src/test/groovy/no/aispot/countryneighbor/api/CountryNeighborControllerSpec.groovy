package no.aispot.countryneighbor.api

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*

import java.text.MessageFormat
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import groovy.json.JsonSlurper
import io.reactivex.Observable
import no.aispot.countryneighbor.model.CountryNeighborDto
import no.aispot.countryneighbor.model.CountryNeighborPair
import no.aispot.countryneighbor.service.CountryNeighborService
import spock.lang.Specification

@SpringBootTest
class CountryNeighborControllerSpec extends Specification {
	@Autowired
	private WebApplicationContext webApplicationContext
	@Autowired
	private CountryNeighborController countryNeighborController

	private MockMvc mockMvc
	private CountryNeighborService countryNeighborService
	private Random random = new Random()
	
	def setup() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(webApplicationContext)
			.build()
		countryNeighborService = Stub(CountryNeighborService)
		countryNeighborController.countryNeighborService = countryNeighborService
	}
	
	def "getNeighbors should return neighbor pairs"() {
		setup:
			def iso = random.nextInt().toString()
			def url = MessageFormat.format("/neighbors/{0}", iso)
			def neighbors = Arrays.asList(random.nextInt().toString(), random.nextInt().toString(), random.nextInt().toString())

			countryNeighborService.getNeighbors(_) >> Observable.just(new CountryNeighborDto(null, neighbors))

			def expectedPairs = Arrays.asList(new CountryNeighborPair(neighbors.get(0), neighbors.get(1)),
				new CountryNeighborPair(neighbors.get(0), neighbors.get(2)),
				new CountryNeighborPair(neighbors.get(1), neighbors.get(2)))
		when:
			def response = perforAsyncRequest(url)
			def content = new JsonSlurper().parseText(response.contentAsString)
		then:
			response.status == HttpStatus.OK.value()
			def actualPairs = content.stream().map({ p -> new CountryNeighborPair(p.get(0), p.get(1)) })
				.collect(Collectors.toList())
			actualPairs.stream().allMatch { p -> expectedPairs.contains(p) }
	}
	
	def "getNeighbors should return 'partial' neighbor pairs with small timeout"() {
		setup:
			def iso = random.nextInt().toString()
			def timeout = random.nextInt(1000)
			def url = MessageFormat.format("/neighbors/{0}?timeout={1}", iso, timeout)
			def neighbor1 = random.nextInt().toString()
			def neighbor2 = random.nextInt().toString()
			def neighbor3 = random.nextInt().toString()

			countryNeighborService.getNeighbors(iso) >> Observable.just(
				new CountryNeighborDto(null, Arrays.asList(neighbor1, neighbor2, neighbor3)))
			countryNeighborService.getNeighbors(neighbor1) >> Observable.timer((long) (timeout / 2), TimeUnit.MILLISECONDS)
				.map({ new CountryNeighborDto(null, Collections.singletonList(neighbor3)) })
			countryNeighborService.getNeighbors(neighbor2) >> Observable.timer((long) (timeout * 1000), TimeUnit.MILLISECONDS)
				.map({ new CountryNeighborDto(null, Collections.singletonList(neighbor3)) })

			def expectedPair = new CountryNeighborPair(neighbor1, neighbor3)
		when:
			def response = perforAsyncRequest(url)
			def content = new JsonSlurper().parseText(response.contentAsString)
		then:
			response.status == HttpStatus.OK.value()
			content.size() == 1
			def actualPair = new CountryNeighborPair(content.get(0).get(0), content.get(0).get(1))
			actualPair.equals(expectedPair)
			
	}
	
	def "getNeighbors should return empty neighbor pairs for 'orphan' country"() {
		setup:
			def iso = random.nextInt().toString()
			def url = MessageFormat.format("/neighbors/{0}", iso)
			def neighbors = Collections.emptyList()
			countryNeighborService.getNeighbors(_) >> Observable.just(new CountryNeighborDto(null, neighbors))
		when:
			def response = perforAsyncRequest(url)
			def content = new JsonSlurper().parseText(response.contentAsString)
		then:
			response.status == HttpStatus.OK.value()
			content.isEmpty()
	}
	
	def perforAsyncRequest(String url) {
		def result = mockMvc.perform(get(url)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON))
			.andReturn()
		return mockMvc.perform(asyncDispatch(result))
			.andReturn()
			.getResponse()
	}
}