package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {
    private String phoneNumber;
    private String message;
    public static String getMessageForAnyCategory(int bookCount, double bookMeanCount) {
        String sms = "";
        if (bookCount == Math.round(bookMeanCount))
            sms = "fine";
        else if (bookCount > bookMeanCount)
            sms = "you are a bookworm";
        else
            sms = "read more";
        return sms;
    }
}
