package fr.hippo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.Update;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.telegram.telegrambots.ApiConstants.BASE_URL;

@RestController
@RequestMapping("/${keeper.bot_id}")
public class DescriptionKeeperController {

    private static final String BOT_COMMAND = "bot_command";
    private static final String SET_COMMAND = "/set";
    private static final String GET_COMMAND = "/get";
    private static final String ADD_COMMAND = "/add";
    private static final String MESSAGE_SEPARATOR = "\n";

    private static Map<Long, String> title = new HashMap<>();

    @Value("${keeper.bot_id}")
    private String botId;

    @GetMapping("/")
    public String init() {
        return "hello world";
    }

    @PostMapping("/send")
    public void index(@RequestBody Update update) {
        System.out.println("update = " + update);
        final Message message = update.getMessage();
        if (message == null) {
            return;
        }
        final Long chatId = message.getChatId();
        System.out.println("chatId = " + chatId);
        final List<MessageEntity> entities = message.getEntities();
        if (entities != null && entities.stream().map(MessageEntity::getType).anyMatch(BOT_COMMAND::equals)) {
            final String text = message.getText();
            System.out.println("text = " + text);

            if (text != null) {
                if (text.startsWith(SET_COMMAND)) {
                    setDescription(chatId, text);
                } else if (text.startsWith(ADD_COMMAND) && title.containsKey(chatId)) {
                    addDescription(chatId, text);
                } else if (text.equals(GET_COMMAND) && title.containsKey(chatId)) {
                    getDescription(chatId);
                }
            }
        }


    }

    private void getDescription(Long chatId) {
        RestTemplate template = new RestTemplate();
        final URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
                .path("{bot_id}/sendMessage")
                .queryParam("text", title.get(chatId))
                .queryParam("chat_id", chatId)
                .queryParam("disable_web_page_preview", true)
                .buildAndExpand(botId)
                .toUri();

        final Object forObject = template.getForObject(uri, Object.class);
        System.out.println("for object");
        System.out.println(forObject);
    }

    private void setDescription(Long chatId, String text) {
        final String newTitle = text.substring(SET_COMMAND.length() + 1);
        System.out.println("newTitle = " + newTitle);
        title.put(chatId, newTitle);
    }

    private void addDescription(Long chatId, String text) {
        final String newTitle = title.get(chatId) + MESSAGE_SEPARATOR + text.substring(ADD_COMMAND.length() + 1);
        System.out.println("newTitle = " + newTitle);
        title.put(chatId, newTitle);
    }

}