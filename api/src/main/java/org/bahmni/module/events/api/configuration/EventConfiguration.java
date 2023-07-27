package org.bahmni.module.events.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.events.api.listener.AppointmentAdvice;
import org.bahmni.module.events.api.listener.EncounterAdvice;
import org.bahmni.module.events.api.listener.PatientAdvice;
import org.bahmni.module.events.api.publisher.EventPublisher;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.ApplicationContext;
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
    public AppointmentAdvice appointmentEventAdvice(ApplicationContext applicationContext) {
        return new AppointmentAdvice(applicationContext);
    }

    @Bean
    public AspectJExpressionPointcut appointmentEventAdvicePointcut() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.openmrs.module.appointments.service.AppointmentsService.validateAndSave(..))");
        return pointcut;
    }

    @Bean
    public DefaultPointcutAdvisor appointmentAdviceAdvisor(AspectJExpressionPointcut appointmentEventAdvicePointcut, AppointmentAdvice appointmentEventAdvice) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(appointmentEventAdvicePointcut);
        advisor.setAdvice(appointmentEventAdvice);
        return advisor;
    }

    @Bean
    public EncounterAdvice encounterEventAdvice() {
        return new EncounterAdvice();
    }

    @Bean
    public AspectJExpressionPointcut encounterEventAdvicePointcut() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* org.openmrs.api.EncounterService.saveEncounter(..))");
        return pointcut;
    }

    @Bean
    public DefaultPointcutAdvisor encounterAdviceAdvisor(AspectJExpressionPointcut encounterEventAdvicePointcut, EncounterAdvice encounterEventAdvice) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setPointcut(encounterEventAdvicePointcut);
        advisor.setAdvice(encounterEventAdvice);
        return advisor;
    }

    @Bean
    public EventPublisher eventPublisher(JmsTemplate jmsTemplate) {
        return new EventPublisher(jmsTemplate, new ObjectMapper());
    }
}