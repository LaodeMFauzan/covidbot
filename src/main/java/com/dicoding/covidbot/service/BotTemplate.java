package com.dicoding.covidbot.service;

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.event.source.UserSource;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BotTemplate {
    public TemplateMessage createButton(String message) {
        List<Action> actionList = new ArrayList<>();
        actionList.add(new MessageAction("Info Covid", "Info"));
        actionList.add(new MessageAction("Kasus Covid19", "Kasus"));
        actionList.add(new MessageAction("RS Covid", "Penanganan"));

        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(
                null,
                null,
                message,
               actionList
        );

        return new TemplateMessage("Covid",buttonsTemplate);
    }

    public TemplateMessage greetingMessage(Source source, UserProfileResponse sender) {
        String message  = "Halo! Aku vio, virtual assistant yang siap menjawab pertanyaan seputar covid19 di Indonesia";
        if (source instanceof GroupSource) {
            message = String.format(message, "Group");
        } else if (source instanceof RoomSource) {
            message = String.format(message, "Room");
        } else if(source instanceof UserSource) {
            message = String.format(message, sender.getDisplayName());
        } else {
            message = "Unknown Message Source!";
        }

        return createButton(message);
    }

    public List<Message> constructReplyMessage(List<Message> mainMessage) {
        List<Message> messages = new ArrayList<>(mainMessage);
        messages.add(new TextMessage("Ada lagi yang bisa dibantu?"));
        messages.add(new TextMessage("Silahkan ketik info untuk info covid, kasus untuk kasus covid"+
                ",penanganan untuk cari rs covid, dan keluar untuk akhiri percakapan"));
        return messages;
    }
}
