package no.difi.oxalis.as2.inbound;

import com.google.inject.Injector;
import com.opuscapita.peppol.inbound.InboundModule;
import com.opuscapita.peppol.inbound.rest.InboundBusinessServlet;
import com.opuscapita.peppol.inbound.rest.InboundHomeServlet;
import com.opuscapita.peppol.inbound.rest.InboundStatusServlet;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServlet;

@Configuration
public class GuiceBeansConfig {

    // Oxalis needs some guice, give it to Oxalis
    private final Injector injector = GuiceModuleLoader.initiate(new InboundModule());

    @Bean
    public ServletRegistrationBean<HttpServlet> homeServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(new InboundHomeServlet(), "/", "/api/health/check");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> statusServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                injector.getInstance(InboundStatusServlet.class), "/public/status");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> businessServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                injector.getInstance(InboundBusinessServlet.class), "/a2a", "/xib");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> as2ServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                injector.getInstance(As2Servlet.class), "/public/as2");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }

}
