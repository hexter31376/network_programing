package dev.wonyoung.domin.port.in;

import dev.wonyoung.domin.model.Message;

public interface EchoUseCase {
    Message echo(Message message);
}
