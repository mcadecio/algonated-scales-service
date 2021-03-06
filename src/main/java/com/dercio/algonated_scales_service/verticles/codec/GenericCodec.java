package com.dercio.algonated_scales_service.verticles.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class GenericCodec<T> implements MessageCodec<T, T> {
    private final Class<T> cls;

    public GenericCodec(Class<T> cls) {
        super();
        this.cls = cls;
    }

    @Override
    public void encodeToWire(Buffer buffer, T s) {
        try (var bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(s);
            out.flush();
            byte[] yourBytes = bos.toByteArray();
            buffer.appendInt(yourBytes.length);
            buffer.appendBytes(yourBytes);
            out.close();
        } catch (IOException exception) {
            log.error(exception.getMessage());
        }
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        return (T) Json.decodeValue(buffer);
    }

    @Override
    public T transform(T customMessage) {
        // If a message is sent *locally* across the event bus.
        // This example sends message just as is
        return customMessage;
    }

    @Override
    public String name() {
        // Each codec must have a unique name.
        // This is used to identify a codec when sending a message and for unregistering
        // codecs.
        return cls.getSimpleName() + "Codec";
    }

    @Override
    public byte systemCodecID() {
        // Always -1
        return -1;
    }
}
