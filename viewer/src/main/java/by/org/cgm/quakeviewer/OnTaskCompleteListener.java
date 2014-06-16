package by.org.cgm.quakeviewer;

import by.org.cgm.jdbf.JdbfTask;

public interface OnTaskCompleteListener {
    // Notifies about task completeness
    void onTaskComplete(JdbfTask task);
}
