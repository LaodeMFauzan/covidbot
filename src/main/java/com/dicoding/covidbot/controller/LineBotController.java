package com.dicoding.covidbot.controller;

import com.dicoding.covidbot.service.CallbackService;
import com.linecorp.bot.client.LineSignatureValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LineBotController {

    @Autowired
    private CallbackService callbackService;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(@RequestHeader("X-Line-Signature") String xLineSignature,
                                           @RequestBody String eventsPayload) {
       return callbackService.execute(xLineSignature, eventsPayload);
    }
}
