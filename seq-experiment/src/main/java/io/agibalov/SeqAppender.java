package io.agibalov;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.status.ErrorStatus;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class SeqAppender extends AppenderBase<ILoggingEvent> {
    private CloseableHttpClient httpClient;

    @Getter
    @Setter
    private Encoder<ILoggingEvent> encoder;

    @Getter
    @Setter
    private String seqBaseUrl;

    @Override
    public void start() {
        if (seqBaseUrl == null || seqBaseUrl.equals("")) {
            addStatus(new ErrorStatus("seqBaseUrl is not set", this));
            return;
        }

        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(10000)
                        .setConnectionRequestTimeout(5000)
                        .build())
                .build();

        super.start();
    }

    @Override
    public void stop() {
        try {
            httpClient.close();
        } catch (IOException e) {
            addStatus(new ErrorStatus(String.format("Error while closing httpClient: %s", e.getMessage()), this));
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        byte[] eventJson = encoder.encode(eventObject);

        HttpPost httpPost = new HttpPost(seqBaseUrl + "/api/events/raw?clef");
        httpPost.setEntity(new ByteArrayEntity(eventJson, ContentType.APPLICATION_JSON));

        try {
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int status = response.getStatusLine().getStatusCode();
                if (status != 201) {
                    addStatus(new ErrorStatus(String.format("Seq responded with status: %d", status), this));
                }
            }
        } catch (IOException e) {
            addStatus(new ErrorStatus(String.format("Error while talking to Seq: %s", e.getMessage()), this));
        }
    }
}
