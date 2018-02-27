package fr.hippo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.net.URI;
import java.time.Instant;

import static org.telegram.telegrambots.ApiConstants.BASE_URL;

@RestController
@RequestMapping("/${keeper.bot_id}")
public class DescriptionKeeperController {

    private static final String SET_COMMAND = "/set";
    private static final String GET_COMMAND = "/get";
    private static final String ADD_COMMAND = "/add";
    private static final String REMOVE_COMMAND = "/remove";
    private static final String MESSAGE_SEPARATOR = "\n";
    private static final int MAX_SECOND_AFTER_RECEIVING = 60 * 60; // 1 hour

    @Value("${keeper.bot_id}")
    private String botId;

    @Autowired
    private KvstoreService kvstoreService;

    @GetMapping("/")
    public String init() {
        return "hello world";
    }

    @PostMapping("/send")
    public void index(@RequestBody Update update) {
        System.out.println("update = " + update);
        final Message message = update.getMessage();
        if (message == null || !message.isCommand()) {
            System.out.println("Ignoring message because it's not a command.");
            return;
        }
        final Integer receivedDate = message.getDate();
        final Instant unixTime = Instant.ofEpochSecond(receivedDate);
        final Instant now = Instant.now();

        final long nowInSeconds = now.toEpochMilli() / 1000;
        System.out.println("received at: " + unixTime + " / " + receivedDate + " (now = " + nowInSeconds + ")");
        if (unixTime.plusSeconds(MAX_SECOND_AFTER_RECEIVING).isBefore(now)) {
            System.out.println("Ignoring old message!");
            return;
        }
        final Long chatId = message.getChatId();
        System.out.println("chatId = " + chatId);
        final String text = message.getText();
        System.out.println("text = " + text);

        if (text != null) {
            if (text.startsWith(SET_COMMAND)) {
                setDescription(chatId, text);
            } else if (text.startsWith(ADD_COMMAND)) {
                addDescription(chatId, text);
            } else if (text.startsWith(REMOVE_COMMAND)) {
                removeDescription(chatId, text);
            } else if (text.startsWith(GET_COMMAND)) {
                getDescription(chatId);
            }
        }

    }

    private void getDescription(Long chatId) {
        RestTemplate template = new RestTemplate();
        final String message = kvstoreService.getMessage(chatId);
        if (message == null) {
            System.out.println("chat " + chatId + " does not contain key yet");
            return;
        }
        final URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .path("{bot_id}/sendMessage")
                .queryParam("text", message)
                .queryParam("chat_id", chatId)
                .queryParam("disable_web_page_preview", true)
                .buildAndExpand(botId)
                .toUri();

        try {
            final Object forObject = template.getForObject(uri, Object.class);
            System.out.println(forObject);
        } catch (HttpClientErrorException httpClientErrorException) {
            System.err.println(httpClientErrorException.getMessage() + " for uri= " + uri);
        }
    }

    private void setDescription(Long chatId, String text) {
        final String newTitle = text.substring(SET_COMMAND.length() + 1);
        System.out.println("newTitle = " + newTitle);
        kvstoreService.putMessage(chatId, newTitle);
    }

    private void addDescription(Long chatId, String text) {
        String message = kvstoreService.getMessage(chatId);
        if (message == null) {
            message = "";
        }
        final String newTitle = message + MESSAGE_SEPARATOR + text.substring(ADD_COMMAND.length() + 1);
        System.out.println("newTitle = " + newTitle);
        kvstoreService.putMessage(chatId, newTitle);
    }

    private void removeDescription(Long chatId, String text) {
        String message = kvstoreService.getMessage(chatId);
        if (message == null) {
            message = "";
        }
        String toDelete = text.substring(REMOVE_COMMAND.length() + 1);
        if (!StringUtils.isEmpty(toDelete) && message.contains(MESSAGE_SEPARATOR + toDelete)) {
            toDelete = MESSAGE_SEPARATOR + toDelete;
        }
        final String newTitle = message.replaceFirst(toDelete, "");
        System.out.println("newTitle = " + newTitle);
        kvstoreService.putMessage(chatId, newTitle);
    }


}