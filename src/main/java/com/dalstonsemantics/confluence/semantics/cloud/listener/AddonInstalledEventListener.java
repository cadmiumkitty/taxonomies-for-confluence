package com.dalstonsemantics.confluence.semantics.cloud.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.atlassian.connect.spring.AddonInstalledEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AddonInstalledEventListener implements ApplicationListener<AddonInstalledEvent> {

    private TaxonomyVersionGraphInitializer taxonomyVersionGraphInitializer;
    private long delaySec;

    private ScheduledExecutorService executorService;

    public AddonInstalledEventListener(
            TaxonomyVersionGraphInitializer taxonomyVersionGraphInitializer,    
            @Value("${addon.repositories.taxonomy.context.taxonomy-version.init-delay-sec}") long delaySec) {
        this.taxonomyVersionGraphInitializer = taxonomyVersionGraphInitializer;
        this.delaySec = delaySec;

        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(AddonInstalledEvent addonInstalledEvent) {

        log.info("Received AddonInstalledEvent: {}. Scheduling to run in {} seconds", addonInstalledEvent, delaySec);

        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                taxonomyVersionGraphInitializer.initializeTaxonomyVersionGraph(addonInstalledEvent.getHost());
            }
        }, delaySec, TimeUnit.SECONDS);

        log.info("Scheduled creation of initial taxonomy versions");
    }
}