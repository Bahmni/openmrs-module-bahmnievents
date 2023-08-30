package org.bahmni.module.events.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.events.api.publisher.BahmniEventPublisher;
import org.bahmni.module.events.api.publisher.JMSEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.ConnectionFactory;

@Conditional(JMSEventPublishingToggleCondition.class)
@Configuration
public class JMSEventPublisherConfiguration {

    @Bean
    public JndiObjectFactoryBean eventJndiObjectFactoryBean() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();

        String jndiJMSResourceName = "jmsConnectionFactory";
        jndiObjectFactoryBean.setJndiName("java:comp/env/" + jndiJMSResourceName);
        jndiObjectFactoryBean.setProxyInterface(ConnectionFactory.class);
        jndiObjectFactoryBean.setLookupOnStartup(true);

        return jndiObjectFactoryBean;
    }

    @Bean
    public DynamicDestinationResolver eventDestinationResolver() {
        return new DynamicDestinationResolver();
    }

    @Bean
    public JmsTemplate jmsTemplate(JndiObjectFactoryBean eventJndiObjectFactoryBean, DynamicDestinationResolver eventDestinationResolver) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory((ConnectionFactory) eventJndiObjectFactoryBean.getObject());
        jmsTemplate.setDestinationResolver(eventDestinationResolver);
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }

    @Bean
    public JMSEventPublisher jmsEventPublisher(JmsTemplate jmsTemplate) {
        return new JMSEventPublisher(jmsTemplate, new ObjectMapper());
    }
}