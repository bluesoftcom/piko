package com.bluesoft.piko.maps;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.bluesoft.piko.maps.location.messaging.LocationEventsBindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding(LocationEventsBindings.class)
public class PikoMapsConfiguration {

    @Bean
    public AWSCredentials awsCredentials() {
        return new BasicAWSCredentials("AKIAWXEX7NHUW2NRWJUD", "QPr+ZmH44K5+abRfp4aMIyr7kAah5jVV/cKQsKd9");
    }

    @Bean
    public AmazonSimpleEmailService sesClient(AWSCredentials awsCredentials) {
        return AmazonSimpleEmailServiceClient.builder()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:23493/ses", "eu-west-1"))
                .build();
    }

    @Bean
    public AWSCognitoIdentityProvider cognitoClient(AWSCredentials awsCredentials) {
        return AWSCognitoIdentityProviderClient.builder()
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:25493/cognito", "eu-west-1"))
                .withRegion("eu-west-1")
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }


}
