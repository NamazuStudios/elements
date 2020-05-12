package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

class UnixFSJournalHeader extends Struct {

    public final UTF8String magic = new UTF8String(4);

    public final Signed32 major = new Signed32();

    public final Signed32 minor = new Signed32();

    public final Unsigned32 txnBufferSize = new Unsigned32();

    public final Unsigned32 txnBufferCount = new Unsigned32();

}
