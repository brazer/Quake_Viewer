package by.org.cgm.quakeviewer;

public interface IProgressTracker {
    void onProgress(String message);
    void onComplete();
}
