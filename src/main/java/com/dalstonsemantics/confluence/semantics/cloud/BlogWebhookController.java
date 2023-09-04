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

import com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.blog.BlogEvent;
import com.dalstonsemantics.confluence.semantics.cloud.processor.EventDispatcher;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class BlogWebhookController extends EventWebhookController {

    public BlogWebhookController(
            @Autowired UUIDProvider uuidp, 
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        super(uuidp, dispatcher, enforceLicense);
    }

    @PostMapping(value = "/webhook/blog_created")
    @ResponseStatus(value = HttpStatus.OK)
    public void blogCreated(@IssParam String iss, @RequestParam String lic, @RequestBody BlogEvent blogEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received blog_created. Iss: {}. Lic: {}. Blog: {}", iss, lic, blogEvent.getBlog().getId());

        createdRemovedRestoredTrashedUpdated(iss, lic, blogEvent.getUserAccountId(), blogEvent.getBlog(), TEAM.CURRENT);

        log.info("Completed processing of blog_created.");
    }        

    @PostMapping(value = "/webhook/blog_removed")
    @ResponseStatus(value = HttpStatus.OK)
    public void blogRemoved(@IssParam String iss, @RequestParam String lic, @RequestBody BlogEvent blogEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received blog_removed. Iss: {}. Lic: {}. Blog: {}", iss, lic, blogEvent.getBlog().getId());

        createdRemovedRestoredTrashedUpdated(iss, lic, blogEvent.getUserAccountId(), blogEvent.getBlog(), TEAM.REMOVED);

        log.info("Completed processing of blog_removed.");
    }        

    @PostMapping(value = "/webhook/blog_restored")
    @ResponseStatus(value = HttpStatus.OK)
    public void blogRestored(@IssParam String iss, @RequestParam String lic, @RequestBody BlogEvent blogEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received blog_restored. Iss: {}. Lic: {}. Blog: {}", iss, lic, blogEvent.getBlog().getId());
        
        createdRemovedRestoredTrashedUpdated(iss, lic, blogEvent.getUserAccountId(), blogEvent.getBlog(), TEAM.CURRENT);

        log.info("Completed processing of blog_restored.");
    }

    @PostMapping(value = "/webhook/blog_trashed")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageTrashed(@IssParam String iss, @RequestParam String lic, @RequestBody BlogEvent blogEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received blog_trashed. Iss: {}. Lic: {}. Blog: {}", iss, lic, blogEvent.getBlog().getId());
        
        createdRemovedRestoredTrashedUpdated(iss, lic, blogEvent.getUserAccountId(), blogEvent.getBlog(), TEAM.TRASHED);

        log.info("Completed processing of blog_trashed.");
    }

    @PostMapping(value = "/webhook/blog_updated")
    @ResponseStatus(value = HttpStatus.OK)
    public void pageUpdated(@IssParam String iss, @RequestParam String lic, @RequestBody BlogEvent blogEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received blog_updated. Iss: {}. Lic: {}. Blog: {} ", iss, lic, blogEvent.getBlog().getId());

        createdRemovedRestoredTrashedUpdated(iss, lic, blogEvent.getUserAccountId(), blogEvent.getBlog(), TEAM.CURRENT);

        log.info("Completed processing of blog_updated.");
    }
}