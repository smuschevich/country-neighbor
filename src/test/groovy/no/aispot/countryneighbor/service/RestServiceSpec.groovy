package no.aispot.countryneighbor.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

import spock.lang.Specification
import spock.util.concurrent.AsyncConditions

@SpringBootTest
class RestServiceSpec extends Specification {
	@Autowired
	private RestService restService
	
	private RestTemplate restTemplate
	private Random random = new Random()
	
	def setup() {
		restTemplate = Stub(RestTemplate)
		restService.setRestTemplate(restTemplate)
	}

	def "get should make get request and return result in case of success"() {
		setup:
			def url = random.nextInt().toString()
			def expectedResult = random.nextInt()
			restTemplate.getForObject(url, Integer.class) >> expectedResult
			def conds = new AsyncConditions()
		when:
			def result = restService.get(url, Integer.class)
		then:
			result.subscribe { r ->
				conds.evaluate { assert r == expectedResult }
			}
			conds.await()
	}
	
	def "get should make get request and return error in case of fail"() {
		setup:
			def url = random.nextInt().toString()
			def expectedMessage = random.nextInt().toString()
			restTemplate.getForObject(url, Object.class) >> {
				throw new RestClientException(expectedMessage)
			}
			def conds = new AsyncConditions()
		when:
			def result = restService.get(url, Object.class)
		then:
			result.subscribe({}, { e ->
				conds.evaluate {
					assert e instanceof RestClientException
					assert e.getMessage() == expectedMessage
				}
			})
			conds.await()
	}
}