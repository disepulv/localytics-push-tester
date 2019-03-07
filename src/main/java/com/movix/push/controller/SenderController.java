package com.movix.push.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.movix.localytics.LocalyticsRestClient;
import com.movix.localytics.conf.LocalyticsConfigurator;
import com.movix.localytics.dto.PushResponse;

/**
 *
 * @author dsepulveda
 *
 */
@RestController
@RequestMapping("/api/test")
public class SenderController {

    private static final int MAX_TEXT_LENGHT = 60;
    private static final int MAX_MSISDN_LENGHT = 15;
    // FOX INT
    private static final String URL_IOS = "https://messaging.localytics.com/v2/push/23752exxxxxxxxxx"; // ios
    private static final String URL_ANDROID = "https://messaging.localytics.com/v2/push/c3203a4xxxxxxxxxx"; // android

    // PROD
    private static final String USERNAME = "2f62adxxxxxxxxxx";
    private static final String PASSWORD = "33b8ccxxxxxxxxxx";

    private static final Logger logger = LoggerFactory.getLogger(SenderController.class);

    private LocalyticsConfigurator configuratorAndroid;
    private LocalyticsConfigurator configuratorIOS;

    @Autowired
    public SenderController() {
        super();

        configuratorAndroid = new LocalyticsConfigurator(URL_ANDROID, USERNAME, PASSWORD);
        configuratorIOS = new LocalyticsConfigurator(URL_IOS, USERNAME, PASSWORD);

    }

    @GetMapping(value = "/broadcastPush", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> broadcastPush(@RequestParam("text") String text) {
        logger.info("broadcastPush| " + text);

        PushResponse response = new LocalyticsRestClient(configuratorAndroid).sendBroadcastPush(validateText(text));
        if (response.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        PushResponse response2 = new LocalyticsRestClient(configuratorIOS).sendBroadcastPush(validateText(text));
        if (response2.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value = "/profilePush", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> profilePush(@RequestParam("text") String text) {
        logger.info("profilePush| " + text);

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String campaignKeyPostfix = formatter.format(today);
        String campaignKey = "TEST_MOVIX_CAMPAIGN_" + campaignKeyPostfix;
        campaignKey = campaignKey.replaceAll("\\s", "");

        String key = "team";
        String[] values = { "ARG_Banfield", "GLO_CONCACAF Champions League" };

        Map<String, Object> extra = new HashMap<String, Object>();
        {
            extra.put("value", "875624003689");
            extra.put("key", "videoId");
        }

        PushResponse response = new LocalyticsRestClient(configuratorAndroid).sendProfiledPush(validateText(text), key,
                Arrays.asList(values), campaignKey, extra);
        if (response.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        PushResponse response2 = new LocalyticsRestClient(configuratorIOS).sendProfiledPush(validateText(text), key,
                Arrays.asList(values), campaignKey, extra);
        if (response2.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value = "/customerPush", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> customerPush(@RequestParam("msisdn") String msisdn,
            @RequestParam("text") Optional<String> text, @RequestParam("pushOn") boolean pushOn) {
        logger.info("customerPush| " + text);

        String deliveryType = pushOn ? "push" : "sms";
        Map<String, Object> extra = new HashMap<String, Object>();
        {
            extra.put("value", deliveryType);
            extra.put("key", "delivery_type");
        }

        PushResponse response = new LocalyticsRestClient(configuratorAndroid).sendCustomerPush(validateOptionalText(text),
                validateMsisdn(msisdn), extra);
        if (response.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        PushResponse response2 = new LocalyticsRestClient(configuratorIOS).sendCustomerPush(validateOptionalText(text),
                validateMsisdn(msisdn), extra);
        if (response2.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value = "/webcontentPush", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> webcontentPush(@RequestParam("msisdn") String msisdn,
            @RequestParam("text") Optional<String> text, @RequestParam("contentId") String contentId) throws JSONException {
        logger.info("webcontentPush| " + text);

        Map<String, Object> extra = new HashMap<String, Object>();
        {
            extra.put("value", contentId);
            extra.put("key", "contentId");
        }

        PushResponse response = new LocalyticsRestClient(configuratorAndroid).sendCustomerPush(validateOptionalText(text),
                validateMsisdn(msisdn), extra);
        if (response.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        PushResponse response2 = new LocalyticsRestClient(configuratorIOS).sendCustomerPush(validateOptionalText(text),
                validateMsisdn(msisdn), extra);
        if (response2.getMessage() == null) {
            return ResponseEntity.ok(HttpStatus.CONFLICT);
        }

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value = "/echo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> echo(@RequestParam("text") String text) {
        logger.info("echo| " + text);

        return ResponseEntity.ok(validateText(text));
    }

    private String validateMsisdn(String msisdn) {
        if (msisdn == null) {
            return "";
        }

        String newMsisdn = msisdn.trim();
        if (newMsisdn.length() > MAX_MSISDN_LENGHT) {
            newMsisdn = newMsisdn.substring(0, MAX_MSISDN_LENGHT);
        }

        return newMsisdn;
    }

    private String validateText(String source) {
        if (source == null) {
            return "";
        }

        String newText = source.trim();
        if (newText.length() > MAX_TEXT_LENGHT) {
            newText = newText.substring(0, MAX_TEXT_LENGHT);
        }

        return newText;
    }

    private String validateOptionalText(Optional<String> opSource) {
        String source = opSource.orElse(null);

        if (source == null) {
            return "";
        }

        String newText = source.trim();
        if (newText.length() > MAX_TEXT_LENGHT) {
            newText = newText.substring(0, MAX_TEXT_LENGHT);
        }

        return newText;
    }

}
