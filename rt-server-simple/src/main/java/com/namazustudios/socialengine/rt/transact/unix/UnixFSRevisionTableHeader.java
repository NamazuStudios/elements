package com.namazustudios.socialengine.rt.transact.unix;

import javolution.context.LocalContext;
import javolution.io.Struct;

class UnixFSRevisionTableHeader extends Struct {

    final UTF8String magic = new UTF8String(4);

    final Signed32 major = new Signed32();

    final Signed32 minor = new Signed32();

    final Signed32 revisionTableCount = new Signed32();

    final UnixFSAtomicLongData atomicLongData = inner(new UnixFSAtomicLongData());

}
