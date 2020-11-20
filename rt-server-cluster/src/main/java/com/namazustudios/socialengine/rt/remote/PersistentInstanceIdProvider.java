package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.InvalidInstanceIdException;
import com.namazustudios.socialengine.rt.id.InstanceId;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.*;

import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static java.io.File.createTempFile;

public class PersistentInstanceIdProvider implements Provider<InstanceId> {

    public static final String INSTANCE_ID_FILE = "com.namazustudios.socialengine.rt.id.instance.id.file";

    private String instanceIdFilePath;

    @Override
    public InstanceId get() {
        try (final FileInputStream fis = new FileInputStream(getInstanceIdFilePath());
             final BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            return read(reader);
        } catch (InvalidInstanceIdException ex) {
            return generateAndWrite();
        } catch (FileNotFoundException ex) {
            return generateAndWrite();
        } catch (IOException ex) {
            throw new InternalException("Unable to read InstanceId from disk.", ex);
        }
    }

    private InstanceId read(final BufferedReader reader) throws IOException {
        final String stringRepresentation = reader.readLine();
        return new InstanceId(stringRepresentation);
    }

    private InstanceId generateAndWrite() {

        final File file = new File(".", getInstanceIdFilePath());
        file.getParentFile().mkdirs();

        final File temp;

        try {
             temp = createTempFile("instance-id", "txt");
             temp.deleteOnExit();
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

        final InstanceId instanceId = randomInstanceId();

        try (final OutputStream os = new FileOutputStream(temp);
             final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
            writer.write(instanceId.asString());
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

        if (!temp.renameTo(file)) throw new InternalException("Unable to set file location to " + file);

        return instanceId;

    }

    public String getInstanceIdFilePath() {
        return instanceIdFilePath;
    }

    @Inject
    public void setInstanceIdFilePath(@Named(INSTANCE_ID_FILE) String instanceIdFilePath) {
        this.instanceIdFilePath = instanceIdFilePath;
    }

}
