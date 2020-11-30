package com.bluesoft.piko.admin.mailcatcher;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.net.URL;

@ConfigurationProperties("mailcatcher")
@Component
@Validated
@Data
public class MailCatcherProperties {

    @NotNull
    private URL url;

}
