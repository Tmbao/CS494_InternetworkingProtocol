package com.tmbao.shpictures.server;


import com.tmbao.shpictures.utils.SessionIdentifierGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Tmbao on 12/3/16.
 */
public class ClientManagement {
  private Map<String, ClientState> clientStateDictionary;

  public ClientManagement() {
    clientStateDictionary = new ConcurrentHashMap<>();
  }

  public ClientIdentifier addClient(String clientName) {
    ClientIdentifier clientIdentifier = new ClientIdentifier(clientName, SessionIdentifierGenerator.nextSessionId());
    ClientState clientState = new ClientState(clientIdentifier);
    clientState.setActive();
    clientStateDictionary.put(clientState.getIdentifier().getClientId(), clientState);

    return clientIdentifier;
  }

  public ClientState getState(String clientId) {
    if (clientStateDictionary.containsKey(clientId)) {
      ClientState clientState = clientStateDictionary.get(clientId);
      if (clientState.isActive()) {
        return clientState;
      } else {
        removeState(clientId);
      }
    }
    return null;
  }

  public void removeState(String clientId) {
    clientStateDictionary.remove(clientId);
  }

  public void updateState(String clientId, ClientState clientState) {
    if (clientStateDictionary.containsKey(clientId)) {
      clientStateDictionary.replace(clientId, clientState);
    }
  }
}
