package cc.blynk.common.handlers.decoders;

import cc.blynk.common.enums.Command;
import cc.blynk.common.model.messages.MessageBase;
import cc.blynk.common.stats.GlobalStats;
import cc.blynk.common.utils.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static cc.blynk.common.model.messages.MessageFactory.produce;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 */
//todo could be optimized with checkpoints if needed
public class ReplayingMessageDecoder extends ReplayingDecoder<Void> {

    private static final Logger log = LogManager.getLogger(ReplayingMessageDecoder.class);

    private final GlobalStats stats;

    public ReplayingMessageDecoder(GlobalStats stats) {
        this.stats = stats;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        short command = in.readUnsignedByte();
        int messageId = in.readUnsignedShort();

        MessageBase message;
        if (command == Command.RESPONSE) {
            int responseCode = in.readUnsignedShort();
            message = produce(messageId, responseCode);
        } else {
            int length = in.readUnsignedShort();
            String messageBody = (length == 0 ? "" : in.readBytes(length).toString(Config.DEFAULT_CHARSET));
            message = produce(messageId, command, messageBody);
        }

        log.trace("Incomming {}", message);

        if (stats != null) {
            stats.incomeMessages.mark();
        }

        out.add(message);
    }

}
