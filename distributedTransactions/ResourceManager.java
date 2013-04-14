
package distributedTransactions;

import java.util.HashMap;
import java.util.Vector;
import java.lang.System;


final class ResourceState {
    public static final ResourceState Idle = new ResourceState();
    public static final ResourceState Open = new ResourceState();
    public static final ResourceState Started = new ResourceState();
    public static final ResourceState Prepared = new ResourceState();
    public static final ResourceState Committed = new ResourceState();
    
    private ResourceState() {}
}


class Resource {
    byte[] data = new byte[8192];

    public void write(byte[] change) {
        append(change);
    }
    public byte[] read() {
        return null;
    }

    private void append(byte[] change) {
        int has = data.length;
        int added = change.length;
        byte[] modified = new byte[has+added];
        System.arraycopy(data, 0, modified, 0, has);
        System.arraycopy(change, 0, modified, has, added);
        data = modified;
    }
}

class LogManager {
    Vector<byte[]> pagelog = new Vector<byte[]>(512, 128);
    public void write(byte[] entry) {
        pagelog.add(entry);
    }
    public void invalidate(byte[] entry) {
        pagelog.remove(entry);
    }
    public void recover(TransactionId tid) {
    }
}

class LockManager {
    Resource r = null;
    HashMap<Resource,Byte> locktable = new HashMap<Resource,Byte>();
    public LockManager(Resource resource) {
        this.r = resource;
    }
    public boolean lock() {
        if (locktable.containsKey(r)) {
            return false;
        }
        locktable.put(r,null);
        return true;
    }
    public void release() {
        locktable.remove(r);
    }
}


public class ResourceManager {
    private LockManager lockm;
    private LogManager logm;
    private TransactionManager tm;
    private Resource resource;
    private byte[] beforeImage;
    private ResourceState currentState;
    
    public ResourceManager(
        Resource resource,
        TransactionManager tm,
        LockManager lockm,
        LogManager logm)
    {
        this.resource = resource;
        this.tm = tm;
        this.lockm = lockm;
        this.logm = logm;
    }

    public boolean isReady() {
        tm.ready(this);
        currentState = ResourceState.Open;
        return true;
    }
    public void prepare() {
        lockm.lock();
        beforeImage = resource.read();
        logm.write(beforeImage);
        tm.readyToCommit(this);
        currentState = ResourceState.Prepared;
    }
    public void commit() {
        byte[] page = new byte[] {34,};
        resource.write(page);
        logm.invalidate(beforeImage);
        lockm.release();
        tm.commitSuccess(this);
        currentState = ResourceState.Committed;
    }

    public ResourceState state() {
        return currentState;
    }
}

