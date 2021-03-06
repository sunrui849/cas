package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasConsentJdbcConfiguration;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link JpaConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CasConsentJdbcConfiguration.class, RefreshAutoConfiguration.class})
public class JpaConsentRepositoryTests {

    private static final DefaultConsentDecisionBuilder BUILDER = new DefaultConsentDecisionBuilder(CipherExecutor.noOpOfSerializableToString());
    private static final Service SVC = RegisteredServiceTestUtils.getService();
    private static final AbstractRegisteredService REG_SVC = RegisteredServiceTestUtils.getRegisteredService(SVC.getId());

    private static final Map<String, Object> ATTR = CollectionUtils.wrap("attribute", "value");

    @Autowired
    @Qualifier("consentRepository")
    private ConsentRepository repository;

    @Test
    public void verifyConsentDecisionIsNotFound() {
        val d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication());
        assertNull(d);
    }

    @Test
    public void verifyConsentDecisionIsSaved() {
        val decision = BUILDER.build(SVC, REG_SVC, "casuser", ATTR);
        decision.setId(100);
        repository.storeConsentDecision(decision);

        var d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication("casuser"));
        assertNotNull(d);
        assertEquals("casuser", d.getPrincipal());

        val res = this.repository.deleteConsentDecision(d.getId(), d.getPrincipal());
        assertTrue(res);
        assertTrue(this.repository.findConsentDecisions().isEmpty());
        d = this.repository.findConsentDecision(SVC, REG_SVC, CoreAuthenticationTestUtils.getAuthentication("casuser"));
        assertNull(d);
    }

}
