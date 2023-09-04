package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.page.PageEvent;
import com.dalstonsemantics.confluence.semantics.cloud.processor.EventDispatcher;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PageWebhookController extends EventWebhookController {

    public PageWebhookController(
            @Autowired UUIDProvider uuidp, 
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        super(uuidp, dispatcher, enforceLicense);
    }

    @PostMapping(value = "/webhook/page_created")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageCreated(@IssParam String iss, @RequestParam String lic, @RequestBody PageEvent pageEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received page_created. Iss: {}. Lic: {}. Page: {}", iss, lic, pageEvent.getPage().getId());

        createdRemovedRestoredTrashedUpdated(iss, lic, pageEvent.getUserAccountId(),  pageEvent.getPage(), TEAM.CURRENT);

        log.info("Completed processing of page_created.");
    }

    @PostMapping(value = "/webhook/page_removed")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageRemoved(@IssParam String iss, @RequestParam String lic, @RequestBody PageEvent pageEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received page_removed. Iss: {}. Lic: {}. Page: {}", iss, lic, pageEvent.getPage().getId());

        createdRemovedRestoredTrashedUpdated(iss, lic, pageEvent.getUserAccountId(),  pageEvent.getPage(), TEAM.REMOVED);

        log.info("Completed processing of page_removed.");
    }        

    @PostMapping(value = "/webhook/page_restored")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageRestored(@IssParam String iss, @RequestParam String lic, @RequestBody PageEvent pageEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received page_restored. Iss: {}. Lic: {}. Page: {}", iss, lic, pageEvent.getPage().getId());
        
        createdRemovedRestoredTrashedUpdated(iss, lic, pageEvent.getUserAccountId(),  pageEvent.getPage(), TEAM.CURRENT);

        log.info("Completed processing of page_restored.");
    }

    @PostMapping(value = "/webhook/page_trashed")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageTrashed(@IssParam String iss, @RequestParam String lic, @RequestBody PageEvent pageEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received page_trashed. Iss: {}. Lic: {}. Page: {}", iss, lic, pageEvent.getPage().getId());
        
        createdRemovedRestoredTrashedUpdated(iss, lic, pageEvent.getUserAccountId(),  pageEvent.getPage(), TEAM.TRASHED);

        log.info("Completed processing of page_trashed.");
    }

    @PostMapping(value = "/webhook/page_updated")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageUpdated(@IssParam String iss, @RequestParam String lic, @RequestBody PageEvent pageEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received page_updated. Iss: {}. Lic: {}. Page: {} ", iss, lic, pageEvent.getPage().getId());

        createdRemovedRestoredTrashedUpdated(iss, lic, pageEvent.getUserAccountId(),  pageEvent.getPage(), TEAM.CURRENT);

        log.info("Completed processing of page_updated.");
    }
}