package com.shinesolutions.aemorchestrator.config;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClientBuilder;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder;
import com.amazonaws.util.EC2MetadataUtils;
import com.shinesolutions.aemorchestrator.model.EnvironmentValues;
import com.shinesolutions.aemorchestrator.service.AwsHelperService;

@Configuration
@Profile("default")
public class AwsConfig {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${aws.sqs.queueName}")
    private String queueName;

    @Value("${aws.client.useProxy}")
    private Boolean useProxy;

    @Value("${aws.client.protocol}")
    private String clientProtocol;

    @Value("${aws.client.proxy.host}")
    private String clientProxyHost;

    @Value("${aws.client.proxy.port}")
    private Integer clientProxyPort;

    @Value("${aws.client.connection.timeout}")
    private Integer clientConnectionTimeout;

    @Value("${aws.client.max.errorRetry}")
    private Integer clientMaxErrorRetry;
    
    @Value("${aws.cloudformation.stackName.publishDispatcher}")
    private String awsPublishDispatcherStackName;
    
    @Value("${aws.cloudformation.stackName.publish}")
    private String awsPublishStackName;
    
    @Value("${aws.cloudformation.stackName.authorDispatcher}")
    private String awsAuthorDispatcherStackName;
    
    @Value("${aws.cloudformation.stackName.author}")
    private String awsAuthorStackName;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.publishDispatcher}")
    private String awsPublishDispatcherAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.publish}")
    private String awsPublishAutoScaleGroupLogicalId;

    @Value("${aws.cloudformation.autoScaleGroup.logicalId.authorDispatcher}")
    private String awsAuthorDispatcherAutoScaleGroupLogicalId;
    
    @Value("${aws.cloudformation.loadBalancer.logicalId.author}")
    private String awsAuthorLoadBalancerLogicalId;
    
    


    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        /*
         * For info on how this works, see:
         * http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/
         * credentials.html
         */
        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    public SQSConnection sqsConnection(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) throws JMSException {
        
        SQSConnectionFactory connectionFactory = SQSConnectionFactory.builder()
            .withRegion(RegionUtils.getRegion(EC2MetadataUtils.getEC2InstanceRegion())) //Gets region form meta data
            .withAWSCredentialsProvider(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();

        return connectionFactory.createConnection();
    }

    @Bean
    public MessageConsumer sqsMessageConsumer(SQSConnection connection) throws JMSException {

        /*
         * Create the session and use CLIENT_ACKNOWLEDGE mode. Acknowledging
         * messages deletes them from the queue
         */
        Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(session.createQueue(queueName));

        return consumer;
    }

    @Bean
    public ClientConfiguration awsClientConfig() {
        ClientConfiguration clientConfig = new ClientConfiguration();

        if (useProxy) {
            clientConfig.setProxyHost(clientProxyHost);
            clientConfig.setProxyPort(clientProxyPort);
        }

        clientConfig.setProtocol(Protocol.valueOf(clientProtocol.toUpperCase()));
        clientConfig.setConnectionTimeout(clientConnectionTimeout);
        clientConfig.setMaxErrorRetry(clientMaxErrorRetry);

        return clientConfig;
    }

    @Bean
    public AmazonEC2 amazonEC2Client(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonEC2ClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public AmazonElasticLoadBalancing amazonElbClient(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonElasticLoadBalancingClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public AmazonAutoScaling amazonAutoScalingClient(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonAutoScalingClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public AmazonCloudFormation amazonCloudFormationClient(AWSCredentialsProvider awsCredentialsProvider,
        ClientConfiguration awsClientConfig) {
        return AmazonCloudFormationClientBuilder.standard()
            .withCredentials(awsCredentialsProvider)
            .withClientConfiguration(awsClientConfig)
            .build();
    }

    @Bean
    public EnvironmentValues envValues(final AwsHelperService awsHelper) {
        EnvironmentValues envValues = new EnvironmentValues();

        envValues.setAutoScaleGroupNameForPublishDispatcher(
            awsHelper.getStackPhysicalResourceId(awsPublishDispatcherStackName, awsPublishDispatcherAutoScaleGroupLogicalId));
        logger.info("Resolved auto scaling group name for publish dispatcher to: " + 
            envValues.getAutoScaleGroupNameForPublishDispatcher());

        envValues.setAutoScaleGroupNameForPublish(
            awsHelper.getStackPhysicalResourceId(awsPublishStackName, awsPublishAutoScaleGroupLogicalId));
        
        logger.info("Resolved auto scaling group name for publish to: " + 
            envValues.getAutoScaleGroupNameForPublish());

        envValues.setAutoScaleGroupNameForAuthorDispatcher(
            awsHelper.getStackPhysicalResourceId(awsAuthorDispatcherStackName, awsAuthorDispatcherAutoScaleGroupLogicalId));
        
        logger.info("Resolved auto scaling group name for author dispatcher to: " + 
            envValues.getAutoScaleGroupNameForAuthorDispatcher());
        
        envValues.setElasticLoadBalancerNameForAuthor(
            awsHelper.getStackPhysicalResourceId(awsAuthorStackName, awsAuthorLoadBalancerLogicalId));
        
        logger.info("Resolved elastic load balancer name for author to: " + 
            envValues.getElasticLoadBalancerNameForAuthor());

        return envValues;
    }

}
