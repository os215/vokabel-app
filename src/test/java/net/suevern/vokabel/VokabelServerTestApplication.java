package net.suevern.vokabel;

import net.seidengarn.keepasshttp.client.KeePassHttpConnector;
import net.seidengarn.keepasshttp.client.KeePassLogin;
import net.seidengarn.keepasshttp.client.exception.KeePassHttpException;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;

@SpringBootApplication
@Configuration
@Import({EnableEncryptablePropertiesConfiguration.class})
public class VokabelServerTestApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(VokabelServerTestApplication.class)
                .profiles("test")
                .run(args);
    }

    @Bean("jasyptStringEncryptor")
    public StringEncryptor keepassEncryptor() {
        return new KeePassEncryptor();
    }

    private class KeePassEncryptor implements StringEncryptor {
        @Override
        public String encrypt(String message) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String decrypt(String encryptedMessage) {
            if (encryptedMessage != null && encryptedMessage.startsWith("keepass:")) {
                return getKeePassLogin(StringUtils.substringAfter(encryptedMessage, "keepass:")).getPassword();
            } else if (encryptedMessage != null
                    && encryptedMessage.startsWith("keepass-user:")) {
                return getKeePassLogin(StringUtils.substringAfter(encryptedMessage, "keepass-user:")).getLogin();
            } else {
                return encryptedMessage;
            }
        }
    }

    private KeePassLogin getKeePassLogin(String url) {
        try {
            KeePassHttpConnector keePassConnector = new KeePassHttpConnector();
            return keePassConnector.getLogin(url);
        } catch (KeePassHttpException e) {
            throw new IllegalArgumentException("Exception while reading KeePass-Login for: " + url, e);
        }
    }
}
