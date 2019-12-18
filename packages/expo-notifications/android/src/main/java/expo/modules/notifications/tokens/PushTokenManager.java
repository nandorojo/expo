package expo.modules.notifications.tokens;

import org.unimodules.core.interfaces.SingletonModule;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.WeakHashMap;

import expo.modules.notifications.tokens.interfaces.PushTokenListener;

public class PushTokenManager implements SingletonModule, expo.modules.notifications.tokens.interfaces.PushTokenManager {
  private static final String SINGLETON_NAME = "PushTokenManager";

  /**
   * We store this value to be able to inform new listeners of last known token.
   */
  private String mLastToken;

  /**
   * A collection of weak references to listeners. Used to iterate over, on new token.
   */
  private Collection<WeakReference<PushTokenListener>> mListeners;

  /**
   * A weak map of listeners -> reference. Lets us check quickly whether given listener
   * is already registered.
   */
  private WeakHashMap<PushTokenListener, WeakReference<PushTokenListener>> mListenerReferenceMap;

  public PushTokenManager() {
    mListeners = new HashSet<>();
    mListenerReferenceMap = new WeakHashMap<>();

    // Registers this singleton instance in static FirebaseTokenListenerService listeners collection.
    // Since it doesn't hold strong reference to the object this should be safe.
    FirebaseTokenListenerService.addListener(this);
  }

  @Override
  public String getName() {
    return SINGLETON_NAME;
  }

  /**
   * Registers a {@link PushTokenListener} by adding a {@link WeakReference} to
   * the {@link PushTokenManager#mListeners} collection
   * and the {@link PushTokenManager#mListenerReferenceMap} map.
   *
   * @param listener Listener to be notified of new device push tokens.
   */
  @Override
  public void addListener(PushTokenListener listener) {
    // Check if the listener is already registered
    if (!mListenerReferenceMap.containsKey(listener)) {
      WeakReference<PushTokenListener> listenerReference = new WeakReference<>(listener);
      mListenerReferenceMap.put(listener, listenerReference);
      mListeners.add(listenerReference);
      // Since it's a new listener and we know of a last valid value, let's let them know.
      if (mLastToken != null) {
        listener.onNewToken(mLastToken);
      }
    }
  }

  /**
   * Unregisters a {@link PushTokenListener} by removing the {@link WeakReference} to the listener
   * from the {@link PushTokenManager#mListeners} collection
   * and the {@link PushTokenManager#mListenerReferenceMap} map.
   *
   * @param listener Listener previously registered with {@link PushTokenManager#addListener(PushTokenListener)}.
   */
  @Override
  public void removeListener(PushTokenListener listener) {
    WeakReference<PushTokenListener> listenerReference = mListenerReferenceMap.get(listener);
    mListeners.remove(listenerReference);
    mListenerReferenceMap.remove(listener);
  }

  /**
   * Used by {@link FirebaseTokenListenerService} to notify of new tokens.
   * Calls {@link PushTokenListener#onNewToken(String)} on all {@link PushTokenManager#mListeners}.
   * <p>
   * Gets rid of empty weak references from {@link PushTokenManager#mListeners} collection.
   *
   * @param token New device push token.
   */
  void onNewToken(String token) {
    Collection<WeakReference<PushTokenListener>> emptyReferences = new HashSet<>();

    for (WeakReference<PushTokenListener> listenerReference : mListeners) {
      PushTokenListener listener = listenerReference.get();
      if (listener != null) {
        listener.onNewToken(token);
      } else {
        emptyReferences.add(listenerReference);
      }
    }

    mListeners.removeAll(emptyReferences);

    mLastToken = token;
  }
}
