package com.gccbenben.qqbotservice.component.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Working21BotSchedule {

    @Scheduled(cron = "0 40 14 5 * ?")
    public void working(){
        
    }
}
