package dev.wonyoung.infrastructure.adapter.subject1;

import dev.wonyoung.domin.model.Message;
import dev.wonyoung.domin.port.in.EchoUseCase;
import dev.wonyoung.infrastructure.container.di.Component;

@Component
public class EchoService implements EchoUseCase {

    @Override
    public Message echo(Message message) {
        return message.echo();
    }
}
