package no.difi.oxalis.as2.inbound;

import com.google.inject.Injector;
import com.opuscapita.peppol.inbound.module.InboundModule;
import com.opuscapita.peppol.inbound.rest.InboundHomeServlet;
import com.opuscapita.peppol.inbound.rest.InboundStatusServlet;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class GuiceBeansConfig {

    // Oxalis needs some guice, give it to Oxalis
    private final Injector injector = GuiceModuleLoader.initiate(new InboundModule());

    @Bean
    public ServletRegistrationBean homeServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(new InboundHomeServlet(),
                "/",
                "/api/health/check",
                "/api/list/apis");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean statusServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(
                injector.getInstance(InboundStatusServlet.class), "/public/status");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean as2ServletBean() {
        ServletRegistrationBean bean = new ServletRegistrationBean(
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
