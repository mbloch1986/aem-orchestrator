package com.shinesolutions.aemorchestrator.aem;

import com.shinesolutions.aemorchestrator.model.AemCredentials;
import com.shinesolutions.aemorchestrator.model.UserPasswordCredentials;
import com.shinesolutions.swaggeraem4j.api.SlingApi;
import com.shinesolutions.swaggeraem4j.auth.HttpBasicAuth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class AemApiFactoryTest {
    
    @InjectMocks
    private AemApiFactory aemApiFactory;
    
    @Mock
    private AemCredentials aemCredentials;
    
    private String basePath;
    
    private Integer connectionTimeout;
    
    private Boolean useDebug;
    
    private Boolean verifySsl;

    @Before
    public void setup() {
        basePath = "testBasePath";
        useDebug = false;
        verifySsl = false;
        connectionTimeout = 30000;
        
        setField(aemApiFactory, "useDebug", useDebug);
        setField(aemApiFactory, "connectionTimeout", connectionTimeout);
        setField(aemApiFactory, "verifySsl", verifySsl);

    }
    
    @Test
    public void testGetSlingApi_ClientIsOrchestrator() {
        UserPasswordCredentials orchestratorCredentials = new UserPasswordCredentials();
        orchestratorCredentials.setUserName("orchestratorUsername");
        orchestratorCredentials.setPassword("orchestratorPassword");
        when(aemCredentials.getOrchestratorCredentials()).thenReturn(orchestratorCredentials);
        
        SlingApi result = aemApiFactory.getSlingApi(basePath, AgentAction.DELETE);
        
        assertThat(result.getApiClient().getBasePath(), equalTo(basePath));
        assertThat(result.getApiClient().isDebugging(), is(useDebug));
        assertThat(result.getApiClient().isVerifyingSsl(), is(verifySsl));
        assertThat(result.getApiClient().getConnectTimeout(), equalTo(connectionTimeout));
        
        HttpBasicAuth auth = (HttpBasicAuth) result.getApiClient().getAuthentication("aemAuth");
        assertThat(auth.getUsername(), equalTo(orchestratorCredentials.getUserName()));
        assertThat(auth.getPassword(), equalTo(orchestratorCredentials.getPassword()));
    }
    
    @Test
    public void testGetSlingApi_ClientIsReplicator() {
        UserPasswordCredentials replicatorCredentials = new UserPasswordCredentials();
        replicatorCredentials.setUserName("replicatorUsername");
        replicatorCredentials.setPassword("replicatorPassword");
        when(aemCredentials.getReplicatorCredentials()).thenReturn(replicatorCredentials);
        
        SlingApi result = aemApiFactory.getSlingApi(basePath, AgentAction.CREATE);
        
        assertThat(result.getApiClient().getBasePath(), equalTo(basePath));
        assertThat(result.getApiClient().isDebugging(), is(useDebug));
        assertThat(result.getApiClient().isVerifyingSsl(), is(verifySsl));
        assertThat(result.getApiClient().getConnectTimeout(), equalTo(connectionTimeout));
        
        HttpBasicAuth auth = (HttpBasicAuth) result.getApiClient().getAuthentication("aemAuth");
        assertThat(auth.getUsername(), equalTo(replicatorCredentials.getUserName()));
        assertThat(auth.getPassword(), equalTo(replicatorCredentials.getPassword()));
    }
}
