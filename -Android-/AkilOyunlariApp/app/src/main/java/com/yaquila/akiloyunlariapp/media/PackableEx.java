package com.yaquila.akiloyunlariapp.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
