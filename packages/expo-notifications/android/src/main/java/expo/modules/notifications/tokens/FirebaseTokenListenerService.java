package expo.modules.notifications.tokens;

import com.google.firebase.messaging.FirebaseMessagingService;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.WeakHashMap;

import androidx.annotation.NonNull;

/**
 * Subclass of FirebaseMessagingService responsible for dispatching new tokens.
 */
public class FirebaseTokenListenerService extends FirebaseMessagingService {
  // Unfortunately we cannot save state between instances of a service other way
  // than by static properties. Fortunately, using weak references we can
  // be somehow sure instances of PushTokenListeners won't be leaked by this component.

  /**
   * We store this value to be able to inform new listeners of last known token.
   */
  private static String LAST_TOKEN = null;

  /**
   * A collection of weak references to listeners. Used to iterate over, on new token.
   */
  private static Collection<WeakReference<PushTokenManager>> LISTENERS = new HashSet<>();

  /**
   * A weak map of listeners -> reference. Lets us check quickly whether given listener
   * is already registered.
   */
  private static WeakHashMap<PushTokenManager, WeakReference<PushTokenManager>> LISTENERS_REFERENCES = new WeakHashMap<>();

  /**
   * Used only by {@link PushTokenManager} instances. If you look for a place to register
   * your listener, use {@link PushTokenManager} singleton module.
   * <p>
   * Purposefully the argument is expected to be a {@link PushTokenManager} and just a listener.
   * <p>
   * This class doesn't hold strong references to listeners, so you need to own your listeners.
   *
   * @param listener A listener instance to be informed of new push device tokens.
   */
  static void addListener(PushTokenManager listener) {
    // Checks whether this listener has already been registered
    if (!LISTENERS_REFERENCES.containsKey(listener)) {
      WeakReference<PushTokenManager> listenerReference = new WeakReference<>(listener);
      LISTENERS_REFERENCES.put(listener, listenerReference);
      LISTENERS.add(listenerReference);
      // Since it's a new listener and we know of a last valid token, let's let them know.
      if (LAST_TOKEN != null) {
        listener.onNewToken(LAST_TOKEN);
      }
    }
  }

  /**
   * Called on new token, dispatches it to {@link FirebaseTokenListenerService#LISTENERS}.
   *
   * @param token New device push token.
   */
  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);

    Collection<WeakReference<PushTokenManager>> emptyReferences = new HashSet<>();

    for (WeakReference<PushTokenManager> listenerReference : LISTENERS) {
      PushTokenManager listener = listenerReference.get();
      if (listener != null) {
        listener.onNewToken(token);
      } else {
        emptyReferences.add(listenerReference);
      }
    }

    LISTENERS.removeAll(emptyReferences);
  }
}
