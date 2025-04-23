package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReader {
    private String name;
    private String surname;
    private String phone;
    private boolean subscribed;
    private List<Book> favoriteBooks;
    private List<SmsMessage> messages = new ArrayList<>();

    public void appendMessage(SmsMessage msg) {
        this.messages.add(0, msg);
    }
    public String getMessages() {
        String msgs = "";
        for(SmsMessage msg: this.messages)
            msgs += msg.getMessage();
        return msgs;
    }
}
