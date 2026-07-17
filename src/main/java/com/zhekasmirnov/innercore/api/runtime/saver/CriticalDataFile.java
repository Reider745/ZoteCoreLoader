package com.zhekasmirnov.innercore.api.runtime.saver;

import com.zhekasmirnov.innercore.api.log.ICLog;

import java.io.File;

public class CriticalDataFile {
    private final File mainFile;
    private final File tmpFile;
    private final File backupFile;
    private enum State { IDLE, READING, READING_BACKUP, WRITING }
    State state = State.IDLE;

    public CriticalDataFile(File mainFile) {
        this.mainFile = mainFile;
        this.tmpFile = new File(mainFile.getAbsolutePath() + ".tmp");
        this.backupFile = new File(mainFile.getAbsolutePath() + ".bak");
    }

    public synchronized File beginRead() {
        if (state != State.IDLE)
            throw new IllegalStateException("beginRead(): not in correct state " + state);
        if (mainFile.exists()) {
            state = State.READING;
            return mainFile;
        }
        state = State.READING_BACKUP;
        return backupFile;
    }

    public synchronized File readBackupAfterMainFailed() {
        if (state == State.READING_BACKUP)
            return null;
        if (state != State.READING)
            throw new IllegalStateException("readBackupAfterMainFailed(): not in correct state " + state);
        // quarantine the corrupt main file for manual recovery - it must not stay in place, or the
        // next successful write would rotate it into the backup
        File corruptFile = new File(mainFile.getAbsolutePath() + ".corrupt");
        corruptFile.delete();
        if (!mainFile.renameTo(corruptFile)) {
            mainFile.delete();
        }

        state = State.READING_BACKUP;
        return backupFile;
    }

    public synchronized void endRead() {
        if (state != State.READING && state != State.READING_BACKUP)
            return;
        state = State.IDLE;
    }

    public synchronized File beginWrite() {
        if (state != State.IDLE)
            throw new IllegalStateException("beginWrite(): not in correct state " + state);
        state = State.WRITING;
        return tmpFile;
    }

    // NOTE: on failure the state remains WRITING and the caller must call abortWrite()
    public synchronized boolean endSuccessfulWrite(boolean writeToLog) {
        if (state != State.WRITING)
            throw new IllegalStateException("endSuccessfulWrite(): not in correct state " + state);
        if (mainFile.isFile()) { // main file is valid
            backupFile.delete();
            if (!mainFile.renameTo(backupFile)) {
                if (writeToLog) ICLog.d("SAVER", "failed to rename " + mainFile.getAbsolutePath() + " to " + backupFile.getName());
                return false;
            }
        } else {
            if (mainFile.exists() && !mainFile.delete())
                return false;
        }
        if (tmpFile.renameTo(mainFile)) {
            state = State.IDLE;
            if (writeToLog) ICLog.d("SAVER", "renamed saves file " + tmpFile.getAbsolutePath() + " to " + mainFile.getName());
            return true;
        } else {
            if (writeToLog) ICLog.d("SAVER", "failed to rename " + tmpFile.getAbsolutePath() + " to " + mainFile.getName());
            return false;
        }
    }

    public synchronized void abortWrite() {
        if (state != State.WRITING)
            return;
        state = State.IDLE;
        tmpFile.delete();
    }

    @Override
    public String toString() {
        return "CriticalDataFile{path=" + mainFile.getAbsolutePath() + "}";
    }
}
