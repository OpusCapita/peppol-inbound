package no.difi.oxalis.as2.inbound;

import com.google.inject.Injector;
import com.opuscapita.peppol.inbound.InboundModule;
import com.opuscapita.peppol.inbound.network.MessageHandler;
import com.opuscapita.peppol.inbound.rest.InboundBusinessServlet;
import com.opuscapita.peppol.inbound.rest.InboundHomeServlet;
import com.opuscapita.peppol.inbound.rest.InboundStatusServlet;
import com.opuscapita.peppol.inbound.rest.StatisticsServlet;
import no.difi.oxalis.as4.inbound.As4Servlet;
import no.difi.oxalis.commons.guice.GuiceModuleLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.http.HttpServlet;

@Configuration
public class GuiceBeansConfig {

    private final Injector injector;

    @Autowired
    public GuiceBeansConfig(MessageHandler messageHandler) {
        this.injector = GuiceModuleLoader.initiate(new InboundModule(messageHandler));
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> homeServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                new InboundHomeServlet(), "/", "/api/health/check");
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
    public ServletRegistrationBean<HttpServlet> statisticsServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                injector.getInstance(StatisticsServlet.class), "/statistics");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> businessServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                injector.getInstance(InboundBusinessServlet.class), "/a2a", "/xib", "/sirius", "/gw-httpbasic","/reprocess");
        MultipartConfigFactory configFactory = new MultipartConfigFactory();
        configFactory.setMaxFileSize(DataSize.ofMegabytes(150));
        configFactory.setMaxRequestSize(DataSize.ofMegabytes(150));
        bean.setMultipartConfig(configFactory.createMultipartConfig());
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
    public ServletRegistrationBean<HttpServlet> as4ServletBean() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>(
                injector.getInstance(As4Servlet.class), "/public/as4");
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
