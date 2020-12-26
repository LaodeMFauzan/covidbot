package com.dicoding.covidbot.service;

import com.dicoding.covidbot.adapter.KawalCoronaAdaptor;
import com.dicoding.covidbot.model.CoronaData;
import com.dicoding.covidbot.model.LineEventsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineSignatureValidator;
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
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CallbackService {

    @Autowired
    private BotService botService;

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @Autowired
    private KawalCoronaAdaptor kawalCoronaAdaptor;

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
        } else if (msgText.contains("id")
                || msgText.contains("find")
                || msgText.contains("join")
                || msgText.contains("teman")) {
            handleFallbackMessage(replyToken, new RoomSource(roomId, sender.getUserId()));
        }
    }

    private void handleOneOnOneChats(String replyToken, String textMessage) {
        String msgText = textMessage.toLowerCase();
        if (msgText.contains("info")) {
            String replyText = "COVID-19 adalah penyakit menular yang disebabkan oleh jenis coronavirus " +
                    "yang baru ditemukan. Ini merupakan virus baru dan penyakit yang tidak dikenal " +
                    "sebelum terjadi wabah di Wuhan, Tiongkok, bulan Desember 2019.\n\n" +
                    "COVID-19 adalah singkatan dari CoronaVirus Disease-2019.";
            botService.replyText(replyToken, replyText);
            // info covid
        } else if (msgText.contains("rumah sakit")){
             // show hospital that handle covid
        } else if (msgText.contains("kasus")){
            // show the number cases of covid19 in Indonesia

        } else {
            handleFallbackMessage(replyToken, new UserSource(sender.getUserId()));
        }
    }

    private void handleFallbackMessage(String replyToken, Source source) {
        greetingMessage(replyToken, source, "Hi " + sender.getDisplayName() + ", aku belum  mengerti maksud kamu. Silahkan ikuti petunjuk ya :)");
    }

    private void handleCovidCasesRequest(String replyToken){
        String askRegionText ="Silahkan ketik nama provinsi atau ketik Indonesia untuk kasus seluruh Indonesia";
        botService.replyText(replyToken, askRegionText);
    }

    private String showIndonesianAllCovidCasesData(){
        CoronaData[] indonesianCoronaData = kawalCoronaAdaptor.getIndonesiaCovidData();
        return  "Total Kasus Covid19 di Indonesia \n" +
                "\nPositif: " +indonesianCoronaData[0].getPositif() +
                "\nMeninggal: "+ indonesianCoronaData[0].getMeninggal() +
                "\nSembuh: "+ indonesianCoronaData[0].getSembuh() +
                "\nDirawat: "+ indonesianCoronaData[0].getDirawat();
    }

}
