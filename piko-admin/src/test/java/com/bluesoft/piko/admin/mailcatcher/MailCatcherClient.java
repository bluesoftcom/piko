package com.bluesoft.piko.admin.mailcatcher;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
public class MailCatcherClient {

    private static final TypeReference<List<MessageSummary>> MESSAGES_TYPE_REFERENCE = new TypeReference<>() {
    };

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MailCatcherProperties mailCatcherProperties;

    public MailCatcherClient(ObjectMapper objectMapper, MailCatcherProperties mailCatcherProperties) {
        this.objectMapper = objectMapper;
        this.mailCatcherProperties = mailCatcherProperties;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public List<MessageSummary> listMessages() throws IOException, InterruptedException {
        final URI uri = origin()
                .path("/messages")
                .build()
                .toUri();

        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        final HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream in = response.body()) {
            return objectMapper.readValue(in, MESSAGES_TYPE_REFERENCE);
        }
    }

    private UriComponentsBuilder origin() {
        return UriComponentsBuilder.fromUriString(mailCatcherProperties.getUrl().toExternalForm());
    }

    public MailcatcherMessage fetchMessage(int id) throws IOException, InterruptedException {
        final URI uri = origin()
                .path("/messages/{id}.json")
                .buildAndExpand(id)
                .toUri();

        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != HttpServletResponse.SC_OK) {
            throw new IllegalStateException("Cannot find a message with id: " + id);
        }
        return objectMapper.readValue(response.body(), MailcatcherMessage.class);
    }

    @Value
    public static class MessageSummary {

        int id;
        String sender;
        List<String> recipients;
        String subject;
        String size;
        String createdAt;

        @JsonCreator
        public MessageSummary(
                @JsonProperty("id") int id,
                @JsonProperty("sender") String sender,
                @JsonProperty("recipients") List<String> recipients,
                @JsonProperty("subject") String subject,
                @JsonProperty("size") String size,
                @JsonProperty("created_at") String createdAt
        ) {
            this.id = id;
            this.sender = sender;
            this.recipients = recipients;
            this.subject = subject;
            this.size = size;
            this.createdAt = createdAt;
        }
    }

    @Value
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MailcatcherMessage {

        int id;
        String sender;
        List<String> recipients;
        String subject;
        String source;

        @JsonCreator
        public MailcatcherMessage(
                @JsonProperty("id") int id,
                @JsonProperty("sender") String sender,
                @JsonProperty("recipients") List<String> recipients,
                @JsonProperty("subject") String subject,
                @JsonProperty("source") String source
        ) {
            this.id = id;
            this.sender = sender;
            this.recipients = recipients;
            this.subject = subject;
            this.source = source;
        }
    }
}
