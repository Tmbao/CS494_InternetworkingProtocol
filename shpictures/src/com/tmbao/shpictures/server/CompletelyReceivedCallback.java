package com.tmbao.shpictures.server;

/**
 * Created by Tmbao on 12/4/16.
 */
public interface CompletelyReceivedCallback {
  Package onCompletelyReceived(ClientState clientState, Package.PackageType type);
}
