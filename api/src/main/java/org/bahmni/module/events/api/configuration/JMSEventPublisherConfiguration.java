/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright 2026. CURE International. CURE International is a registered trademark
 * and the CURE International graphic logo is a trademark of CURE International.
 */

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