package com.dicoding.covidbot.service;

import com.dicoding.covidbot.model.LineEventsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.ReplyEvent;
import com.linecorp.bot.model.event.message.MessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Paths;
import java.util.*;

@Service
public class CallbackService {

    @Autowired
    private BotService botService;

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Autowired
    CasesHandlerService casesHandlerService;

    private UserProfileResponse sender = null;

    @Autowired
    private BotTemplate botTemplate;

    public ResponseEntity<String> execute(String xLineSignature, String eventsPayload) {
        try {
            // validasi line signature. matikan validasi ini jika masih dalam pengembangan
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            System.out.println(eventsPayload);
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            LineEventsModel eventsModel = objectMapper.readValue(eventsPayload, LineEventsModel.class);

            eventsModel.getEvents().forEach((event) -> {
                if (event instanceof JoinEvent || event instanceof FollowEvent) {
                    String replyToken = ((ReplyEvent) event).getReplyToken();
                    handleJointOrFollowEvent(replyToken, event.getSource());
                } else if (event instanceof MessageEvent) {
                    handleMessageEvent((MessageEvent) event);
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void handleJointOrFollowEvent(String replyToken, Source source) {
        greetingMessage(replyToken, source, null);
    }

    private void handleMessageEvent(MessageEvent event) {
        String replyToken = event.getReplyToken();
        MessageContent content = event.getMessage();
        Source source = event.getSource();
        String senderId = source.getSenderId();
        sender = botService.getProfile(senderId);

        if (content instanceof TextMessageContent) {
            handleTextMessage(replyToken, (TextMessageContent) content, source);
        } else {
            System.out.println("GREET THE USER!!!");
            greetingMessage(replyToken, source, null);
        }
    }

    private void greetingMessage(String replyToken, Source source, String additionalMessage) {
        if (sender == null) {
            String senderId = source.getSenderId();
            sender = botService.getProfile(senderId);
        }

        TemplateMessage greetingMessage = botTemplate.greetingMessage(source, sender);

        if (additionalMessage != null) {
            List<Message> messages = new ArrayList<>();
            messages.add(new TextMessage(additionalMessage));
            messages.add(greetingMessage);
            botService.reply(replyToken, messages);
        } else {
            botService.reply(replyToken, greetingMessage);
        }
    }

    private void handleTextMessage(String replyToken, TextMessageContent content, Source source) {
        if (source instanceof GroupSource) {
            handleGroupChats(replyToken, content.getText(), ((GroupSource) source).getGroupId());
        } else if (source instanceof RoomSource) {
            handleRoomChats(replyToken, content.getText(), ((RoomSource) source).getRoomId());
        } else if (source instanceof UserSource) {
            handleOneOnOneChats(replyToken, content.getText());
        } else {
            botService.replyText(replyToken, "Unknown Message Source!");
        }
    }

    private void handleGroupChats(String replyToken, String textMessage, String groupId) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("bot leave")) {
            if (sender == null) {
                botService.replyText(replyToken, "Hi, tambahkan dulu covid bot sebagai teman!");
            } else {
                botService.leaveGroup(groupId);
            }
        } else if (msgText.contains("id")
                || msgText.contains("find")
                || msgText.contains("join")
                || msgText.contains("teman")
        ) {
            handleFallbackMessage(replyToken, new GroupSource(groupId, sender.getUserId()));
        }

    }

    private void handleRoomChats(String replyToken, String textMessage, String roomId) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("bot leave")) {
            if (sender == null) {
                botService.replyText(replyToken, "Hi, tambahkan covid bot sebagai teman!");
            } else {
                botService.leaveRoom(roomId);
            }
        }
    }

    private void handleOneOnOneChats(String replyToken, String textMessage) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("info")) {
            showInfoCovid(replyToken);
        } else if (msgText.contains("penanganan")) {
            showCovidHospital(replyToken);
        } else if (msgText.contains("kasus")) {
            casesHandlerService.handleCovidCasesRequest(replyToken);
        } else if (msgText.contains("indonesia")) {
            showIndonesiaCovidCases(replyToken);
        } else if ((casesHandlerService.mapProvinceCovidCases().containsKey(msgText.toLowerCase()))) {
            showProvinceCovidCases(replyToken, msgText);
        } else if (msgText.contains("keluar")) {
            botService.replyText(replyToken, "Terimakasih sudah menghubungi vidbo. Stay safe, stay sane!");
        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }
    }

    private void handleFallbackMessage(String replyToken, Source source) {
        greetingMessage(replyToken, source, "Hi " + sender.getDisplayName() +
                ", aku belum  mengerti maksud kamu. Silahkan ikuti petunjuk ya :)");
    }

    private void showInfoCovid(String replyToken) {
        String replyText = null;
        try {
            replyText = new String(Files.readAllBytes(Paths.get("src/main/resources/covid_info.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Message> messages = new ArrayList<>();
        messages.add(new TextMessage(replyText));

        botService.reply(replyToken, constructReplyMessage(Collections.singletonList(new TextMessage(replyText))));
    }

    public void showCovidHospital(String replyToken) {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate = IOUtils.toString(Objects.requireNonNull
                    (classLoader.getResourceAsStream("flex_message.json")));


            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);

            List<Message> mainMessage = new ArrayList<>();
            mainMessage.add(new TextMessage("Berikut beberapa RS yang dapat menangani covid."));
            mainMessage.add(new FlexMessage("RS Covid", flexContainer));

            ReplyMessage replyMessage = new ReplyMessage(replyToken, constructReplyMessage(mainMessage));

            botService.reply(replyToken, replyMessage.getMessages());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showIndonesiaCovidCases(String replyToken) {
        botService.reply(replyToken, constructReplyMessage(
                Collections.singletonList(new TextMessage(casesHandlerService.getIndonesianAllCovidCases())))
        );
    }

    private void showProvinceCovidCases(String replyToken, String province) {
        botService.reply(replyToken, constructReplyMessage(
                Collections.singletonList(
                        new TextMessage(casesHandlerService.mapProvinceCovidCases
                                (casesHandlerService.mapProvinceCovidCases(), province))
                ))
        );
    }

    private List<Message> constructReplyMessage(List<Message> mainMessage) {
        List<Message> messages = new ArrayList<>(mainMessage);
        messages.add(new TextMessage("Ada lagi yang bisa dibantu?"));
        messages.add(new TextMessage("Silahkan ketik info untuk info covid, kasus untuk kasus covid, " +
                ",penanganan untuk cari rs covid, dan keluar untuk akhiri percakapan"));
        return messages;
    }
}
