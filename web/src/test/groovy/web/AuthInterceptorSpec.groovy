package web


import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(AuthInterceptor)
class AuthInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test auth interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"auth")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
