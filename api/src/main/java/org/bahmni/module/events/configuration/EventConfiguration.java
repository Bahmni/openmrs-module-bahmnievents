package org.bahmni.module.events.configuration;

import org.bahmni.module.events.listner.PatientAdvice;
import org.bahmni.module.events.publisher.EventPublisher;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.jms.ConnectionFactory;

@Conditional(EventPublishingToggleCondition.class)
@Configuration
public class EventConfiguration {

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
    public PatientAdvice patientEventAdvice() {
        return new PatientAdvice();
    }

    @Bean
    public AspectJExpressionPointcut patientEventAdvicePointcut() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.openmrs.api.PatientService.savePatient(..))");
        return pointcut;
    }

    @Bean
    public DefaultPointcutAdvisor patientAdviceAdvisor(AspectJExpressionPointcut patientEventAdvicePointcut, PatientAdvice patientEventAdvice) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(patientEventAdvicePointcut);
        advisor.setAdvice(patientEventAdvice);
        return advisor;
    }

    @Bean
    public EventPublisher eventPublisher(JmsTemplate jmsTemplate) {
        return new EventPublisher(jmsTemplate, new ObjectMapper());
    }
}